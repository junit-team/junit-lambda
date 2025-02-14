/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.MethodSourceSupport.METHOD_SCHEME;
import static org.junit.platform.engine.support.descriptor.ClassSource.CLASS_SCHEME;
import static org.junit.platform.engine.support.descriptor.ClasspathResourceSource.CLASSPATH_SCHEME;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.ReflectiveInterceptorCall;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.UriSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor TestDescriptor} for
 * {@link org.junit.jupiter.api.TestFactory @TestFactory} methods.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class TestFactoryTestDescriptor extends TestMethodTestDescriptor implements Filterable {

	public static final String SEGMENT_TYPE = "test-factory";
	public static final String DYNAMIC_CONTAINER_SEGMENT_TYPE = "dynamic-container";
	public static final String DYNAMIC_TEST_SEGMENT_TYPE = "dynamic-test";

	private static final ReflectiveInterceptorCall<Method, Object> interceptorCall = InvocationInterceptor::interceptTestFactoryMethod;
	private static final InterceptingExecutableInvoker executableInvoker = new InterceptingExecutableInvoker();

	private final DynamicDescendantFilter dynamicDescendantFilter = new DynamicDescendantFilter();

	public TestFactoryTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod,
			Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration) {
		super(uniqueId, testClass, testMethod, enclosingInstanceTypes, configuration);
	}

	// --- Filterable ----------------------------------------------------------

	@Override
	public DynamicDescendantFilter getDynamicDescendantFilter() {
		return dynamicDescendantFilter;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	protected void invokeTestMethod(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
		ExtensionContext extensionContext = context.getExtensionContext();

		context.getThrowableCollector().execute(() -> {
			Object instance = extensionContext.getRequiredTestInstance();
			Object testFactoryMethodResult = executableInvoker.invoke(getTestMethod(), instance, extensionContext,
				context.getExtensionRegistry(), interceptorCall);
			TestSource defaultTestSource = getSource().orElseThrow(
				() -> new JUnitException("Illegal state: TestSource must be present"));
			try (Stream<DynamicNode> dynamicNodeStream = toDynamicNodeStream(testFactoryMethodResult)) {
				int index = 1;
				Iterator<DynamicNode> iterator = dynamicNodeStream.iterator();
				while (iterator.hasNext()) {
					DynamicNode dynamicNode = iterator.next();
					Optional<JupiterTestDescriptor> descriptor = createDynamicDescriptor(this, dynamicNode, index,
						defaultTestSource, getDynamicDescendantFilter(), configuration);
					descriptor.ifPresent(dynamicTestExecutor::execute);
					index++;
				}
			}
			catch (ClassCastException ex) {
				throw invalidReturnTypeException(ex);
			}
			dynamicTestExecutor.awaitFinished();
		});
	}

	@SuppressWarnings("unchecked")
	private Stream<DynamicNode> toDynamicNodeStream(Object testFactoryMethodResult) {
		if (testFactoryMethodResult instanceof DynamicNode) {
			return Stream.of((DynamicNode) testFactoryMethodResult);
		}
		try {
			return (Stream<DynamicNode>) CollectionUtils.toStream(testFactoryMethodResult);
		}
		catch (PreconditionViolationException ex) {
			throw invalidReturnTypeException(ex);
		}
	}

	private JUnitException invalidReturnTypeException(Throwable cause) {
		String message = String.format(
			"@TestFactory method [%s] must return a single %2$s or a Stream, Collection, Iterable, Iterator, Iterator-source or array of %2$s.",
			getTestMethod().toGenericString(), DynamicNode.class.getName());
		return new JUnitException(message, cause);
	}

	static Optional<JupiterTestDescriptor> createDynamicDescriptor(JupiterTestDescriptor parent, DynamicNode node,
			int index, TestSource defaultTestSource, DynamicDescendantFilter dynamicDescendantFilter,
			JupiterConfiguration configuration) {

		UniqueId uniqueId;
		Supplier<JupiterTestDescriptor> descriptorCreator;
		Optional<TestSource> customTestSource = node.getTestSourceUri().map(TestFactoryTestDescriptor::fromUri);
		TestSource source = customTestSource.orElse(defaultTestSource);

		if (node instanceof DynamicTest) {
			DynamicTest test = (DynamicTest) node;
			uniqueId = parent.getUniqueId().append(DYNAMIC_TEST_SEGMENT_TYPE, "#" + index);
			descriptorCreator = () -> new DynamicTestTestDescriptor(uniqueId, index, test, source, configuration);
		}
		else {
			DynamicContainer container = (DynamicContainer) node;
			uniqueId = parent.getUniqueId().append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#" + index);
			descriptorCreator = () -> new DynamicContainerTestDescriptor(uniqueId, index, container, source,
				dynamicDescendantFilter.withoutIndexFiltering(), configuration);
		}
		if (dynamicDescendantFilter.test(uniqueId, index - 1)) {
			JupiterTestDescriptor descriptor = descriptorCreator.get();
			descriptor.setParent(parent);
			return Optional.of(descriptor);
		}
		return Optional.empty();
	}

	/**
	 * @since 5.3
	 */
	static TestSource fromUri(URI uri) {
		Preconditions.notNull(uri, "URI must not be null");
		if (CLASSPATH_SCHEME.equals(uri.getScheme())) {
			return ClasspathResourceSource.from(uri);
		}
		if (CLASS_SCHEME.equals(uri.getScheme())) {
			return ClassSource.from(uri);
		}
		if (METHOD_SCHEME.equals(uri.getScheme())) {
			return MethodSourceSupport.from(uri);
		}
		return UriSource.from(uri);
	}

	/**
	 * Override {@link TestMethodTestDescriptor#nodeSkipped} as a no-op, since
	 * the {@code TestWatcher} API is not supported for {@code @TestFactory}
	 * containers.
	 *
	 * @since 5.4
	 */
	@Override
	public void nodeSkipped(JupiterEngineExecutionContext context, TestDescriptor descriptor, SkipResult result) {
		/* no-op */
	}

	/**
	 * Override {@link TestMethodTestDescriptor#nodeFinished} as a no-op, since
	 * the {@code TestWatcher} API is not supported for {@code @TestFactory}
	 * containers.
	 *
	 * @since 5.4
	 */
	@Override
	public void nodeFinished(JupiterEngineExecutionContext context, TestDescriptor descriptor,
			TestExecutionResult result) {

		/* no-op */
	}

}
