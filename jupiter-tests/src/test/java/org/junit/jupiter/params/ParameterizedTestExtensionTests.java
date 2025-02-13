/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.ParameterizedTestExtension.METHOD_CONTEXT_KEY;
import static org.junit.jupiter.params.ParameterizedTestExtension.arguments;

import java.io.FileNotFoundException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Unit tests for {@link ParameterizedTestExtension}.
 *
 * @since 5.0
 */
class ParameterizedTestExtensionTests {

	private final ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	static boolean streamWasClosed = false;

	@Test
	void supportsReturnsFalseForMissingTestMethod() {
		var extensionContextWithoutTestMethod = getExtensionContextReturningSingleMethod(new TestCaseWithoutMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithoutTestMethod));
	}

	@Test
	void supportsReturnsFalseForTestMethodWithoutParameterizedTestAnnotation() {
		var extensionContextWithUnAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithUnAnnotatedTestMethod));
	}

	@Test
	void supportsReturnsTrueForTestMethodWithParameterizedTestAnnotation() {
		var extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithAnnotatedMethod());
		assertTrue(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithAnnotatedTestMethod));
	}

	@Test
	void streamsReturnedByProvidersAreClosedWhenCallingProvide() {
		var extensionContext = getExtensionContextReturningSingleMethod(
			new ArgumentsProviderWithCloseHandlerTestCase());
		// we need to call supportsTestTemplate() first, because it creates and
		// puts the ParameterizedTestMethodContext into the Store
		this.parameterizedTestExtension.supportsTestTemplate(extensionContext);

		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext);

		assertFalse(streamWasClosed);
		// cause the stream to be evaluated
		stream.count();
		assertTrue(streamWasClosed);
	}

	@Test
	void emptyDisplayNameIsIllegal() {
		var extensionContext = getExtensionContextReturningSingleMethod(new EmptyDisplayNameProviderTestCase());
		assertThrows(PreconditionViolationException.class,
			() -> this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext));
	}

	@Test
	void defaultDisplayNameWithEmptyStringInConfigurationIsIllegal() {
		AtomicInteger invocations = new AtomicInteger();
		Function<String, Optional<String>> configurationSupplier = key -> {
			if (key.equals(ParameterizedTestExtension.DISPLAY_NAME_PATTERN_KEY)) {
				invocations.incrementAndGet();
				return Optional.of("");
			}
			else {
				return Optional.empty();
			}
		};
		var extensionContext = getExtensionContextReturningSingleMethod(new DefaultDisplayNameProviderTestCase(),
			configurationSupplier);
		assertThrows(PreconditionViolationException.class,
			() -> this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext));
		assertEquals(1, invocations.get());
	}

	@Test
	void argumentsRethrowsOriginalExceptionFromProviderAsUncheckedException() {
		ArgumentsProvider failingProvider = (context) -> {
			throw new FileNotFoundException("a message");
		};

		var exception = assertThrows(FileNotFoundException.class, () -> arguments(failingProvider, null));
		assertEquals("a message", exception.getMessage());
	}

	@Test
	void throwsExceptionWhenParameterizedTestIsNotInvokedAtLeastOnce() {
		var extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithAnnotatedMethod());

		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(
			extensionContextWithAnnotatedTestMethod);
		// cause the stream to be evaluated
		stream.toArray();
		var exception = assertThrows(JUnitException.class, stream::close);

		assertThat(exception).hasMessage(
			"Configuration error: You must configure at least one set of arguments for this @ParameterizedTest");
	}

	@Test
	void doesNotThrowExceptionWhenParametrizedTestDoesNotRequireArguments() {
		var extensionContext = getExtensionContextReturningSingleMethod(new TestCaseAllowNoArgumentsMethod());

		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext);
		// cause the stream to be evaluated
		stream.toArray();
		stream.close();
	}

	@Test
	void throwsExceptionWhenParameterizedTestHasNoArgumentsSource() {
		var extensionContext = getExtensionContextReturningSingleMethod(new TestCaseWithNoArgumentsSource());

		assertThrows(PreconditionViolationException.class,
			() -> this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext),
			"Configuration error: You must configure at least one arguments source for this @ParameterizedTest");
	}

	@Test
	void throwsExceptionWhenArgumentsProviderIsNotStatic() {
		var extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new NonStaticArgumentsProviderTestCase());

		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(
			extensionContextWithAnnotatedTestMethod);

		var exception = assertThrows(JUnitException.class, stream::toArray);

		assertThat(exception) //
				.hasMessage(String.format(
					"The ArgumentsProvider [%s] must be either a top-level class or a static nested class",
					NonStaticArgumentsProvider.class.getName()));
	}

	@Test
	void throwsExceptionWhenArgumentsProviderDoesNotContainUnambiguousConstructor() {
		var extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new MissingNoArgumentsConstructorArgumentsProviderTestCase());

		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(
			extensionContextWithAnnotatedTestMethod);

		var exception = assertThrows(JUnitException.class, stream::toArray);

		String className = AmbiguousConstructorArgumentsProvider.class.getName();
		assertThat(exception) //
				.hasMessage(String.format("Failed to find constructor for ArgumentsProvider [%s]. "
						+ "Please ensure that a no-argument or a single constructor exists.",
					className));
	}

	private ExtensionContext getExtensionContextReturningSingleMethod(Object testCase) {
		return getExtensionContextReturningSingleMethod(testCase, ignored -> Optional.empty());
	}

	private ExtensionContext getExtensionContextReturningSingleMethod(Object testCase,
			Function<String, Optional<String>> configurationSupplier) {

		var method = ReflectionUtils.findMethods(testCase.getClass(),
			it -> "method".equals(it.getName())).stream().findFirst();

		return new ExtensionContext() {

			private final NamespacedHierarchicalStore<Namespace> store = new NamespacedHierarchicalStore<>(null);

			@Override
			public Optional<Method> getTestMethod() {
				return method;
			}

			@Override
			public Optional<ExtensionContext> getParent() {
				return Optional.empty();
			}

			@Override
			public ExtensionContext getRoot() {
				return this;
			}

			@Override
			public String getUniqueId() {
				return null;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public Set<String> getTags() {
				return null;
			}

			@Override
			public Optional<AnnotatedElement> getElement() {
				return Optional.empty();
			}

			@Override
			public Optional<Class<?>> getTestClass() {
				return Optional.empty();
			}

			@Override
			public Optional<Lifecycle> getTestInstanceLifecycle() {
				return Optional.empty();
			}

			@Override
			public java.util.Optional<Object> getTestInstance() {
				return Optional.empty();
			}

			@Override
			public Optional<TestInstances> getTestInstances() {
				return Optional.empty();
			}

			@Override
			public Optional<Throwable> getExecutionException() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getConfigurationParameter(String key) {
				return configurationSupplier.apply(key);
			}

			@Override
			public <T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer) {
				return configurationSupplier.apply(key).map(transformer);
			}

			@Override
			public void publishReportEntry(Map<String, String> map) {
			}

			@Override
			public void publishFile(String fileName, MediaType mediaType, ThrowingConsumer<Path> action) {
			}

			@Override
			public void publishDirectory(String name, ThrowingConsumer<Path> action) {
			}

			@Override
			public Store getStore(Namespace namespace) {
				var store = new NamespaceAwareStore(this.store, namespace);
				method //
						.map(it -> new ParameterizedTestMethodContext(it, it.getAnnotation(ParameterizedTest.class))) //
						.ifPresent(ctx -> store.put(METHOD_CONTEXT_KEY, ctx));
				return store;
			}

			@Override
			public Store getSessionLevelStore(Namespace namespace) {
				return getStore(namespace);
			}

			@Override
			public Store getRequestLevelStore(Namespace namespace) {
				return getStore(namespace);
			}

			@Override
			public ExecutionMode getExecutionMode() {
				return ExecutionMode.SAME_THREAD;
			}

			@Override
			public ExecutableInvoker getExecutableInvoker() {
				return new ExecutableInvoker() {
					@Override
					public Object invoke(Method method, Object target) {
						return null;
					}

					@Override
					public <T> T invoke(Constructor<T> constructor, Object outerInstance) {
						return ReflectionUtils.newInstance(constructor);
					}
				};
			}
		};
	}

	static class TestCaseWithoutMethod {
	}

	static class TestCaseWithMethod {

		void method() {
		}
	}

	static class TestCaseWithAnnotatedMethod {

		@ParameterizedTest
		@ArgumentsSource(ZeroArgumentsProvider.class)
		void method() {
		}
	}

	static class TestCaseAllowNoArgumentsMethod {

		@ParameterizedTest(allowZeroInvocations = true)
		@ArgumentsSource(ZeroArgumentsProvider.class)
		void method() {
		}
	}

	static class TestCaseWithNoArgumentsSource {

		@ParameterizedTest(allowZeroInvocations = true)
		@SuppressWarnings("JUnitMalformedDeclaration")
		void method() {
		}
	}

	static class ZeroArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.empty();
		}
	}

	static class ArgumentsProviderWithCloseHandlerTestCase {

		@ParameterizedTest
		@ArgumentsSource(ArgumentsProviderWithCloseHandler.class)
		void method(String parameter) {
		}
	}

	static class ArgumentsProviderWithCloseHandler implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			var argumentsStream = Stream.of("foo", "bar").map(Arguments::of);
			return argumentsStream.onClose(() -> streamWasClosed = true);
		}
	}

	static class NonStaticArgumentsProviderTestCase {

		@ParameterizedTest
		@ArgumentsSource(NonStaticArgumentsProvider.class)
		void method() {
		}
	}

	class NonStaticArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return null;
		}
	}

	static class MissingNoArgumentsConstructorArgumentsProviderTestCase {

		@ParameterizedTest
		@ArgumentsSource(AmbiguousConstructorArgumentsProvider.class)
		void method() {
		}
	}

	static class EmptyDisplayNameProviderTestCase {

		@ParameterizedTest(name = "")
		@ArgumentsSource(AmbiguousConstructorArgumentsProvider.class)
		void method() {
		}
	}

	static class DefaultDisplayNameProviderTestCase {

		@ParameterizedTest
		@ArgumentsSource(AmbiguousConstructorArgumentsProvider.class)
		void method() {
		}
	}

	static class AmbiguousConstructorArgumentsProvider implements ArgumentsProvider {

		AmbiguousConstructorArgumentsProvider(String parameter) {
		}

		AmbiguousConstructorArgumentsProvider(int parameter) {
		}

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return null;
		}
	}

}
