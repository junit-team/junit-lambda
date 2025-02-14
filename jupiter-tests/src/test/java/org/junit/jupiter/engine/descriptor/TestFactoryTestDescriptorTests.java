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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.DirectorySource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.descriptor.UriSource;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;

/**
 * Unit tests for {@link TestFactoryTestDescriptor}.
 *
 * @since 5.0
 */
class TestFactoryTestDescriptorTests {

	@Test
	void copyIncludesTransformedDynamicDescendantFilter() throws Exception {
		var rootUniqueId = UniqueId.forEngine("engine");
		var parentUniqueId = rootUniqueId.append("class", "myClass");
		var originalUniqueId = parentUniqueId.append("old", "testFactory()");

		var configuration = mock(JupiterConfiguration.class);
		when(configuration.getDefaultDisplayNameGenerator()).thenReturn(new CustomDisplayNameGenerator());
		Method testMethod = CustomStreamTestCase.class.getDeclaredMethod("customStream");
		var original = new TestFactoryTestDescriptor(originalUniqueId, CustomStreamTestCase.class, testMethod, List::of,
			configuration);

		original.getDynamicDescendantFilter().allowUniqueIdPrefix(originalUniqueId.append("foo", "bar"));
		original.getDynamicDescendantFilter().allowIndex(42);

		var newUniqueId = parentUniqueId.append("new", "testFactory()");

		var copy = original.withUniqueId(new UniqueIdPrefixTransformer(originalUniqueId, newUniqueId));

		assertThat(copy.getUniqueId()).isEqualTo(newUniqueId);
		assertThat(copy.getDynamicDescendantFilter().test(newUniqueId, 0)).isTrue();
		assertThat(copy.getDynamicDescendantFilter().test(newUniqueId, 42)).isTrue();
		assertThat(copy.getDynamicDescendantFilter().test(originalUniqueId, 1)).isFalse();
	}

	/**
	 * @since 5.3
	 */
	@Nested
	class TestSources {

		@Test
		void classpathResourceSourceFromUriWithFilePosition() {
			FilePosition position = FilePosition.from(42, 21);
			URI uri = URI.create("classpath:/test.js?line=42&column=21");
			TestSource testSource = TestFactoryTestDescriptor.fromUri(uri);

			assertThat(testSource).isInstanceOf(ClasspathResourceSource.class);
			ClasspathResourceSource source = (ClasspathResourceSource) testSource;
			assertThat(source.getClasspathResourceName()).isEqualTo("test.js");
			assertThat(source.getPosition()).hasValue(position);
		}

		@Test
		void fileSourceFromUriWithFilePosition() {
			File file = new File("src/test/resources/log4j2-test.xml");
			assertThat(file).isFile();

			FilePosition position = FilePosition.from(42, 21);
			URI uri = URI.create(file.toURI() + "?line=42&column=21");
			TestSource testSource = TestFactoryTestDescriptor.fromUri(uri);

			assertThat(testSource).isInstanceOf(FileSource.class);
			FileSource source = (FileSource) testSource;
			assertThat(source.getFile().getAbsolutePath()).isEqualTo(file.getAbsolutePath());
			assertThat(source.getPosition()).hasValue(position);
		}

		@Test
		void directorySourceFromUri() {
			File file = new File("src/test/resources");
			assertThat(file).isDirectory();

			URI uri = file.toURI();
			TestSource testSource = TestFactoryTestDescriptor.fromUri(uri);

			assertThat(testSource).isInstanceOf(DirectorySource.class);
			DirectorySource source = (DirectorySource) testSource;
			assertThat(source.getFile().getAbsolutePath()).isEqualTo(file.getAbsolutePath());
		}

		@Test
		void defaultUriSourceFromUri() {
			File file = new File("src/test/resources");
			assertThat(file).isDirectory();

			URI uri = URI.create("https://example.com?foo=bar&line=42");
			TestSource testSource = TestFactoryTestDescriptor.fromUri(uri);

			assertThat(testSource).isInstanceOf(UriSource.class);
			assertThat(testSource.getClass().getSimpleName()).isEqualTo("DefaultUriSource");
			UriSource source = (UriSource) testSource;
			assertThat(source.getUri()).isEqualTo(uri);
		}

		@Test
		void methodSourceFromUri() {
			URI uri = URI.create("method:org.junit.Foo#bar(java.lang.String,%20java.lang.String[])");
			TestSource testSource = TestFactoryTestDescriptor.fromUri(uri);

			assertThat(testSource).isInstanceOf(MethodSource.class);
			assertThat(testSource.getClass().getSimpleName()).isEqualTo("MethodSource");
			MethodSource source = (MethodSource) testSource;
			assertThat(source.getClassName()).isEqualTo("org.junit.Foo");
			assertThat(source.getMethodName()).isEqualTo("bar");
			assertThat(source.getMethodParameterTypes()).isEqualTo("java.lang.String, java.lang.String[]");
		}
	}

	@Nested
	class Streams {

		private JupiterEngineExecutionContext context;
		private ExtensionContext extensionContext;
		private TestFactoryTestDescriptor descriptor;
		private boolean isClosed;
		private JupiterConfiguration jupiterConfiguration;

		@BeforeEach
		void before() throws Exception {
			jupiterConfiguration = mock();
			when(jupiterConfiguration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());

			extensionContext = mock();
			isClosed = false;

			context = new JupiterEngineExecutionContext(null, null) //
					.extend() //
					.withThrowableCollector(new OpenTest4JAwareThrowableCollector()) //
					.withExtensionContext(extensionContext) //
					.withExtensionRegistry(mock()) //
					.build();

			Method testMethod = CustomStreamTestCase.class.getDeclaredMethod("customStream");
			descriptor = new TestFactoryTestDescriptor(UniqueId.forEngine("engine"), CustomStreamTestCase.class,
				testMethod, List::of, jupiterConfiguration);
			when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
		}

		@Test
		void streamsFromTestFactoriesShouldBeClosed() {
			Stream<DynamicTest> dynamicTestStream = Stream.empty();
			prepareMockForTestInstanceWithCustomStream(dynamicTestStream);

			descriptor.invokeTestMethod(context, mock());

			assertTrue(isClosed);
		}

		@Test
		void streamsFromTestFactoriesShouldBeClosedWhenTheyThrow() {
			Stream<Integer> integerStream = Stream.of(1, 2);
			prepareMockForTestInstanceWithCustomStream(integerStream);

			descriptor.invokeTestMethod(context, mock());

			assertTrue(isClosed);
		}

		private void prepareMockForTestInstanceWithCustomStream(Stream<?> stream) {
			Stream<?> mockStream = stream.onClose(() -> isClosed = true);
			when(extensionContext.getRequiredTestInstance()).thenReturn(new CustomStreamTestCase(mockStream));
		}

	}

	private static class CustomStreamTestCase {

		private final Stream<?> mockStream;

		CustomStreamTestCase(Stream<?> mockStream) {
			this.mockStream = mockStream;
		}

		@TestFactory
		Stream<?> customStream() {
			return mockStream;
		}
	}

}
