/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Throwables.getRootCause;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectIteration;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.testkit.engine.Event;

/**
 * Integration tests for {@link ResourceLock} and {@link ResourceLocksProvider}.
 *
 * @since 5.12
 */
class ResourceLockAnnotationTests extends AbstractJupiterTestEngineTests {

	private static final UniqueId uniqueId = UniqueId.root("enigma", "foo");

	private final JupiterConfiguration configuration = mock();

	@BeforeEach
	void setUp() {
		when(configuration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());
		when(configuration.getDefaultExecutionMode()).thenReturn(ExecutionMode.SAME_THREAD);
	}

	@Test
	void noSharedResources() {
		// @formatter:off
		var classResources = getClassResources(
				NoSharedResourcesTestCase.class
		);
		assertThat(classResources).isEmpty();

		var methodResources = getMethodResources(
				NoSharedResourcesTestCase.class
		);
		assertThat(methodResources).isEmpty();

		var nestedClassResources = getNestedClassResources(
				NoSharedResourcesTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).isEmpty();
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationValue() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationValueTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ_WRITE),
				new ExclusiveResource("a2", LockMode.READ_WRITE)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationValueTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a3", LockMode.READ_WRITE),
				new ExclusiveResource("b1", LockMode.READ),
				new ExclusiveResource("b2", LockMode.READ_WRITE)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationValueTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a3", LockMode.READ_WRITE),
				new ExclusiveResource("c1", LockMode.READ)
		);

		var nestedClassMethodResources = getMethodResources(
				SharedResourcesViaAnnotationValueTestCase.NestedClass.class
		);
		assertThat(nestedClassMethodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("c2", LockMode.READ)
		);
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationProviders() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationProvidersTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ),
				new ExclusiveResource("a2", LockMode.READ)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationProvidersTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("b1", LockMode.READ_WRITE),
				new ExclusiveResource("b2", LockMode.READ_WRITE)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationProvidersTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("c1", LockMode.READ_WRITE),
				new ExclusiveResource("c2", LockMode.READ)
		);
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationValueAndProviders() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ_WRITE),
				new ExclusiveResource("a3", LockMode.READ)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a2", LockMode.READ_WRITE),
				new ExclusiveResource("b1", LockMode.READ),
				new ExclusiveResource("b2", LockMode.READ)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a2", LockMode.READ_WRITE),
				new ExclusiveResource("c1", LockMode.READ_WRITE),
				new ExclusiveResource("c2", LockMode.READ_WRITE),
				new ExclusiveResource("c3", LockMode.READ_WRITE)
		);
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationValueAndProvidersForContainerTemplate() {
		var engineDescriptor = discoverTests(
			selectClass(SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase.class));
		engineDescriptor.accept(TestDescriptor::prune);

		var containerTemplateTestDescriptor = (JupiterTestDescriptor) getOnlyElement(engineDescriptor.getChildren());

		var expectedResources = List.of( //
			new ExclusiveResource("a1", LockMode.READ_WRITE), //
			new ExclusiveResource("a2", LockMode.READ_WRITE), //
			new ExclusiveResource("a3", LockMode.READ), //
			new ExclusiveResource("b1", LockMode.READ), //
			new ExclusiveResource("b2", LockMode.READ), //
			new ExclusiveResource("c1", LockMode.READ_WRITE), //
			new ExclusiveResource("c2", LockMode.READ_WRITE), //
			new ExclusiveResource("c3", LockMode.READ_WRITE), //
			new ExclusiveResource("d1", LockMode.READ_WRITE), //
			new ExclusiveResource("d2", LockMode.READ) //
		);

		assertThat(containerTemplateTestDescriptor.getExclusiveResources()) //
				.containsExactlyInAnyOrderElementsOf(expectedResources);
	}

	@Test
	void addSharedResourcesViaAnnotationValueAndProvidersForContainerTemplateInvocation() {
		var engineDescriptor = discoverTests(selectIteration(
			selectClass(SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase.class), 0));
		engineDescriptor.accept(TestDescriptor::prune);

		var containerTemplateTestDescriptor = (JupiterTestDescriptor) getOnlyElement(engineDescriptor.getChildren());

		var expectedResources = List.of( //
			new ExclusiveResource("a1", LockMode.READ_WRITE), //
			new ExclusiveResource("a2", LockMode.READ_WRITE), //
			new ExclusiveResource("a3", LockMode.READ), //
			new ExclusiveResource("b1", LockMode.READ), //
			new ExclusiveResource("b2", LockMode.READ), //
			new ExclusiveResource("c1", LockMode.READ_WRITE), //
			new ExclusiveResource("c2", LockMode.READ_WRITE), //
			new ExclusiveResource("c3", LockMode.READ_WRITE), //
			new ExclusiveResource("d1", LockMode.READ_WRITE), //
			new ExclusiveResource("d2", LockMode.READ) //
		);

		assertThat(containerTemplateTestDescriptor.getExclusiveResources()) //
				.containsExactlyInAnyOrderElementsOf(expectedResources);
	}

	@Test
	void addSharedResourcesViaAnnotationValueAndProvidersForMethodInContainerTemplate() {
		var engineDescriptor = discoverTests(
			selectMethod(SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase.class, "test"));
		engineDescriptor.accept(TestDescriptor::prune);

		var containerTemplateTestDescriptor = (JupiterTestDescriptor) getOnlyElement(engineDescriptor.getChildren());

		var expectedResources = List.of( //
			new ExclusiveResource("a1", LockMode.READ_WRITE), //
			new ExclusiveResource("a2", LockMode.READ_WRITE), //
			new ExclusiveResource("a3", LockMode.READ), //
			new ExclusiveResource("b1", LockMode.READ), //
			new ExclusiveResource("b2", LockMode.READ) //
		);

		assertThat(containerTemplateTestDescriptor.getExclusiveResources()) //
				.containsExactlyInAnyOrderElementsOf(expectedResources);
	}

	@Test
	void sharedResourcesHavingTheSameValueAndModeAreDeduplicated() {
		// @formatter:off
		var methodResources = getMethodResources(
				SharedResourcesHavingTheSameValueAndModeAreDeduplicatedTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ_WRITE)
		);
		// @formatter:on
	}

	@Test
	void sharedResourcesHavingTheSameValueButDifferentModeAreNotDeduplicated() {
		// @formatter:off
		var methodResources = getMethodResources(
				SharedResourcesHavingTheSameValueButDifferentModeAreNotDeduplicatedTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ),
				new ExclusiveResource("a1", LockMode.READ_WRITE)
		);
		// @formatter:on
	}

	static Stream<Class<?>> testMethodsCanNotDeclareSharedResourcesForChildrenArguments() {
		// @formatter:off
		return Stream.of(
				TestCanNotDeclareSharedResourcesForChildrenTestCase.class,
				ParameterizedTestCanNotDeclareSharedResourcesForChildrenTestCase.class,
				RepeatedTestCanNotDeclareSharedResourcesForChildrenTestCase.class,
				TestFactoryCanNotDeclareSharedResourcesForChildrenTestCase.class
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("testMethodsCanNotDeclareSharedResourcesForChildrenArguments")
	void testMethodsCanNotDeclareSharedResourcesForChildren(Class<?> testClass) {
		var messageTemplate = "'ResourceLockTarget.CHILDREN' is not supported for methods. Invalid method: %s";
		assertThrowsJunitExceptionWithMessage( //
			testClass, //
			messageTemplate.formatted(getDeclaredTestMethod(testClass)) //
		);
	}

	@Test
	void emptyAnnotation() {
		// @formatter:off
		var classResources = getClassResources(
				EmptyAnnotationTestCase.class
		);
		assertThat(classResources).isEmpty();

		var methodResources = getMethodResources(
				EmptyAnnotationTestCase.class
		);
		assertThat(methodResources).isEmpty();

		var nestedClassResources = getNestedClassResources(
				EmptyAnnotationTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).isEmpty();
		// @formatter:on
	}

	private Set<ExclusiveResource> getClassResources(Class<?> testClass) {
		return getClassTestDescriptor(testClass).getExclusiveResources();
	}

	private ClassTestDescriptor getClassTestDescriptor(Class<?> testClass) {
		return new ClassTestDescriptor(uniqueId, testClass, configuration);
	}

	private Set<ExclusiveResource> getMethodResources(Class<?> testClass) {
		var descriptor = new TestMethodTestDescriptor( //
			uniqueId, testClass, getDeclaredTestMethod(testClass), List::of, configuration //
		);
		descriptor.setParent(getClassTestDescriptor(testClass));
		return descriptor.getExclusiveResources();
	}

	private static Method getDeclaredTestMethod(Class<?> testClass) {
		try {
			return testClass.getDeclaredMethod("test");
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<ExclusiveResource> getNestedClassResources(Class<?> testClass) {
		var descriptor = new NestedClassTestDescriptor(uniqueId, testClass, List::of, configuration);
		descriptor.setParent(getClassTestDescriptor(testClass.getEnclosingClass()));
		return descriptor.getExclusiveResources();
	}

	private void assertThrowsJunitExceptionWithMessage(Class<?> testClass, String message) {
		// @formatter:off
		var events = executeTestsForClass(testClass).allEvents();
		assertThat(events.filter(finishedWithFailure(instanceOf(JUnitException.class))::matches))
				.hasSize(1)
				.map(Event::getPayload)
				.map(payload -> (TestExecutionResult) payload.orElseThrow())
				.map(payload -> getRootCause(payload.getThrowable().orElseThrow()))
				.first()
				.is(instanceOf(JUnitException.class))
				.has(message(message));
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class NoSharedResourcesTestCase {

		@Test
		void test() {
		}

		@Nested
		class NestedClass {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock("a1")
	@ResourceLock(value = "a2", mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = "a3", mode = ResourceAccessMode.READ_WRITE, target = ResourceLockTarget.CHILDREN)
	static class SharedResourcesViaAnnotationValueTestCase {

		@Test
		@ResourceLock(value = "b1", mode = ResourceAccessMode.READ)
		@ResourceLock(value = "b2", target = ResourceLockTarget.SELF)
		void test() {
		}

		@Nested
		@ResourceLock(value = "c1", mode = ResourceAccessMode.READ)
		@ResourceLock(value = "c2", mode = ResourceAccessMode.READ, target = ResourceLockTarget.CHILDREN)
		class NestedClass {

			@Test
			void test() {
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock(providers = { //
			SharedResourcesViaAnnotationProvidersTestCase.FirstClassLevelProvider.class, //
			SharedResourcesViaAnnotationProvidersTestCase.SecondClassLevelProvider.class //
	})
	static class SharedResourcesViaAnnotationProvidersTestCase {

		@Test
		@ResourceLock(providers = MethodLevelProvider.class)
		void test() {
		}

		@Nested
		@ResourceLock(providers = NestedClassLevelProvider.class)
		class NestedClass {
		}

		static class FirstClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a1", ResourceAccessMode.READ));
			}
		}

		static class SecondClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a2", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
					Method testMethod) {
				return Set.of(new Lock("b1"));
			}
		}

		static class MethodLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
					Method testMethod) {
				return Set.of(new Lock("b2"));
			}
		}

		static class NestedClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
				return Set.of(new Lock("c1"), new Lock("c2", ResourceAccessMode.READ));
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock( //
			value = "a1", //
			providers = SharedResourcesViaAnnotationValueAndProvidersTestCase.FirstClassLevelProvider.class //
	)
	@ResourceLock( //
			value = "a2", //
			target = ResourceLockTarget.CHILDREN, //
			providers = SharedResourcesViaAnnotationValueAndProvidersTestCase.SecondClassLevelProvider.class //
	)
	static class SharedResourcesViaAnnotationValueAndProvidersTestCase {

		@Test
		@ResourceLock(value = "b1", mode = ResourceAccessMode.READ)
		void test() {
		}

		@Nested
		@ResourceLock("c1")
		@ResourceLock(providers = NestedClassLevelProvider.class)
		class NestedClass {
		}

		static class FirstClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a3", ResourceAccessMode.READ));
			}
		}

		static class SecondClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
					Method testMethod) {
				return Set.of(new Lock("b2", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
				return Set.of(new Lock("c2"));
			}
		}

		static class NestedClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
				return Set.of(new Lock("c3"));
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock( //
			value = "a1", //
			target = ResourceLockTarget.CHILDREN, //
			providers = SharedResourcesHavingTheSameValueAndModeAreDeduplicatedTestCase.Provider.class //
	)
	static class SharedResourcesHavingTheSameValueAndModeAreDeduplicatedTestCase {

		@Test
		@ResourceLock(value = "a1")
		void test() {
		}

		static class Provider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
					Method testMethod) {
				return Set.of(new Lock("a1"));
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock(value = "a1", mode = ResourceAccessMode.READ_WRITE, target = ResourceLockTarget.CHILDREN)
	static class SharedResourcesHavingTheSameValueButDifferentModeAreNotDeduplicatedTestCase {

		@Test
		@ResourceLock(value = "a1", mode = ResourceAccessMode.READ)
		void test() {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class TestCanNotDeclareSharedResourcesForChildrenTestCase {

		@Test
		@ResourceLock(value = "a1", target = ResourceLockTarget.CHILDREN)
		void test() {
		}
	}

	static class ParameterizedTestCanNotDeclareSharedResourcesForChildrenTestCase {

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		@ResourceLock(value = "a1", target = ResourceLockTarget.CHILDREN)
		void test() {
		}
	}

	static class RepeatedTestCanNotDeclareSharedResourcesForChildrenTestCase {

		@RepeatedTest(5)
		@ResourceLock(value = "a1", target = ResourceLockTarget.CHILDREN)
		void test() {
		}
	}

	static class TestFactoryCanNotDeclareSharedResourcesForChildrenTestCase {

		@TestFactory
		@ResourceLock(value = "a1", target = ResourceLockTarget.CHILDREN)
		Stream<DynamicTest> test() {
			return Stream.of(DynamicTest.dynamicTest("Dynamic test", () -> {
			}));
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock
	static class EmptyAnnotationTestCase {

		@Test
		@ResourceLock
		void test() {
		}

		@Nested
		@ResourceLock
		class NestedClass {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ResourceLock( //
			value = "a1", //
			providers = SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase.FirstClassLevelProvider.class //
	)
	@ResourceLock( //
			value = "a2", //
			target = ResourceLockTarget.CHILDREN, //
			providers = SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase.SecondClassLevelProvider.class //
	)
	static class SharedResourcesViaAnnotationValueAndProvidersContainerTemplateTestCase {

		@Test
		@ResourceLock(value = "b1", mode = ResourceAccessMode.READ)
		void test() {
		}

		@Nested
		@ResourceLock(providers = NestedClassLevelProvider.class)
		class NestedClass {
			@Test
			@ResourceLock("c1")
			void test() {
			}
		}

		@Nested
		@ContainerTemplate
		@ResourceLock(value = "d1", target = ResourceLockTarget.CHILDREN)
		class NestedContainerTemplate {
			@Test
			@ResourceLock(value = "d2", mode = ResourceAccessMode.READ)
			void test() {
			}
		}

		static class FirstClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a3", ResourceAccessMode.READ));
			}
		}

		static class SecondClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
					Method testMethod) {
				return Set.of(new Lock("b2", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
				return Set.of(new Lock("c2"));
			}
		}

		static class NestedClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
				return Set.of(new Lock("c3"));
			}
		}
	}
}
