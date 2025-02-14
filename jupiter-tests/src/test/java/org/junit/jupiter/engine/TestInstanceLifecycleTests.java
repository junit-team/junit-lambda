/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.execution.DefaultTestInstances;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Integration tests for {@link TestInstance @TestInstance} lifecycle support.
 *
 * @since 5.0
 * @see TestInstanceLifecycleConfigurationTests
 * @see TestInstanceLifecycleKotlinTests
 */
class TestInstanceLifecycleTests extends AbstractJupiterTestEngineTests {

	private static final Map<Class<?>, List<Lifecycle>> lifecyclesMap = new LinkedHashMap<>();
	private static final Map<String, TestInstances> instanceMap = new LinkedHashMap<>();
	private static final List<String> testsInvoked = new ArrayList<>();
	private static final Map<Class<?>, Integer> instanceCount = new LinkedHashMap<>();

	private static int beforeAllCount;
	private static int afterAllCount;
	private static int beforeEachCount;
	private static int afterEachCount;

	@BeforeEach
	void init() {
		lifecyclesMap.clear();
		instanceMap.clear();
		testsInvoked.clear();
		instanceCount.clear();
		beforeAllCount = 0;
		afterAllCount = 0;
		beforeEachCount = 0;
		afterEachCount = 0;
	}

	@Test
	void instancePerMethod() {
		Class<?> testClass = InstancePerMethodTestCase.class;
		int containers = 3;
		int tests = 3;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(InstancePerMethodTestCase.class, 3));
		int allMethods = 1;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String preDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String testTemplateKey = testTemplateKey(testClass, "singletonTest");
		String testExecutionConditionKey1 = executionConditionKey(testClass, testsInvoked.get(0));
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, testsInvoked.get(0));
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, testsInvoked.get(0));
		String testExecutionConditionKey2 = executionConditionKey(testClass, testsInvoked.get(1));
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, testsInvoked.get(1));
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, testsInvoked.get(1));
		String testExecutionConditionKey3 = executionConditionKey(testClass, testsInvoked.get(2));
		String beforeEachCallbackKey3 = beforeEachCallbackKey(testClass, testsInvoked.get(2));
		String afterEachCallbackKey3 = afterEachCallbackKey(testClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				postProcessTestInstanceKey,
				preDestroyCallbackTestInstanceKey,
				testTemplateKey,
				testExecutionConditionKey1,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				testExecutionConditionKey2,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				testExecutionConditionKey3,
				beforeEachCallbackKey3,
				afterEachCallbackKey3,
				afterAllCallbackKey
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		TestInstances testInstances = instanceMap.get(beforeEachCallbackKey1);
		assertNotNull(testInstances.getInnermostInstance());
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey1));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey1));

		testInstances = instanceMap.get(beforeEachCallbackKey2);
		assertNotNull(testInstances.getInnermostInstance());
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey2));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey2));

		testInstances = instanceMap.get(beforeEachCallbackKey3);
		assertNotNull(testInstances.getInnermostInstance());
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey3));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey3));
		assertSame(testInstances.getInnermostInstance(),
			instanceMap.get(postProcessTestInstanceKey).getInnermostInstance());
		assertSame(testInstances.getInnermostInstance(),
			instanceMap.get(preDestroyCallbackTestInstanceKey).getInnermostInstance());

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
	}

	@Test
	void instancePerClass() {
		instancePerClass(InstancePerClassTestCase.class, instanceCounts(entry(InstancePerClassTestCase.class, 1)));
	}

	@Test
	void instancePerClassWithInheritedLifecycleMode() {
		instancePerClass(SubInstancePerClassTestCase.class,
			instanceCounts(entry(SubInstancePerClassTestCase.class, 1)));
	}

	private void instancePerClass(Class<?> testClass, Map.Entry<Class<?>, Integer>[] instances) {
		int containers = 3;
		int tests = 3;
		int allMethods = 2;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String testTemplateKey = testTemplateKey(testClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String preDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String testExecutionConditionKey1 = executionConditionKey(testClass, testsInvoked.get(0));
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, testsInvoked.get(0));
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, testsInvoked.get(0));
		String testExecutionConditionKey2 = executionConditionKey(testClass, testsInvoked.get(1));
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, testsInvoked.get(1));
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, testsInvoked.get(1));
		String testExecutionConditionKey3 = executionConditionKey(testClass, testsInvoked.get(2));
		String beforeEachCallbackKey3 = beforeEachCallbackKey(testClass, testsInvoked.get(2));
		String afterEachCallbackKey3 = afterEachCallbackKey(testClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				postProcessTestInstanceKey,
				preDestroyCallbackTestInstanceKey,
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				testTemplateKey,
				testExecutionConditionKey1,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				testExecutionConditionKey2,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				testExecutionConditionKey3,
				beforeEachCallbackKey3,
				afterEachCallbackKey3,
				afterAllCallbackKey
		);
		// @formatter:on

		TestInstances testInstances = instanceMap.get(beforeAllCallbackKey);
		assertNotNull(testInstances.getInnermostInstance());
		assertSame(testInstances, instanceMap.get(afterAllCallbackKey));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey1));
		assertSame(testInstances, instanceMap.get(beforeEachCallbackKey1));
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey1));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey2));
		assertSame(testInstances, instanceMap.get(beforeEachCallbackKey2));
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey2));
		assertSame(testInstances, instanceMap.get(testExecutionConditionKey3));
		assertSame(testInstances, instanceMap.get(beforeEachCallbackKey3));
		assertSame(testInstances, instanceMap.get(afterEachCallbackKey3));
		assertSame(testInstances.getInnermostInstance(),
			instanceMap.get(postProcessTestInstanceKey).getInnermostInstance());
		assertSame(testInstances.getInnermostInstance(),
			instanceMap.get(preDestroyCallbackTestInstanceKey).getInnermostInstance());

		assertNull(instanceMap.get(containerExecutionConditionKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	@Test
	void instancePerMethodWithNestedTestClass() {
		Class<?> testClass = InstancePerMethodOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerMethodOuterTestCase.NestedInstancePerMethodTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 4), entry(nestedTestClass, 3));
		int allMethods = 1;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String preDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(testClass);
		String nestedPreDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				preDestroyCallbackTestInstanceKey,
				nestedPreDestroyCallbackTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertNull(instanceMap.get(nestedBeforeAllCallbackKey));
		assertNull(instanceMap.get(nestedAfterAllCallbackKey));

		TestInstances outerInstances = instanceMap.get(beforeEachCallbackKey);
		assertNotNull(outerInstances.getInnermostInstance());
		assertSame(outerInstances, instanceMap.get(afterEachCallbackKey));
		assertSame(outerInstances, instanceMap.get(outerTestExecutionConditionKey));

		TestInstances nestedInstances1 = instanceMap.get(nestedBeforeEachCallbackKey1);
		assertNotNull(nestedInstances1.getInnermostInstance());
		assertNotSame(outerInstances.getInnermostInstance(), nestedInstances1.getInnermostInstance());
		assertSame(nestedInstances1, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstances1, instanceMap.get(nestedExecutionConditionKey1));

		TestInstances nestedInstances2 = instanceMap.get(nestedBeforeEachCallbackKey2);
		assertNotNull(nestedInstances2.getInnermostInstance());
		assertNotSame(outerInstances.getInnermostInstance(), nestedInstances2.getInnermostInstance());
		assertNotSame(nestedInstances1.getInnermostInstance(), nestedInstances2.getInnermostInstance());
		assertSame(nestedInstances2, instanceMap.get(nestedAfterEachCallbackKey2));
		assertSame(nestedInstances2, instanceMap.get(nestedExecutionConditionKey2));

		TestInstances nestedInstances3 = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstances3.getInnermostInstance());
		assertNotSame(outerInstances.getInnermostInstance(), nestedInstances3.getInnermostInstance());
		assertNotSame(nestedInstances1.getInnermostInstance(), nestedInstances3.getInnermostInstance());
		assertSame(nestedInstances3.getInnermostInstance(),
			instanceMap.get(nestedAfterEachCallbackKey3).getInnermostInstance());
		assertSame(nestedInstances3.getInnermostInstance(),
			instanceMap.get(nestedExecutionConditionKey3).getInnermostInstance());
		assertSame(nestedInstances3.getInnermostInstance(),
			instanceMap.get(nestedPreDestroyCallbackTestInstanceKey).getInnermostInstance());

		Object outerInstance1 = instanceMap.get(nestedExecutionConditionKey1).findInstance(testClass).get();
		Object outerInstance2 = instanceMap.get(nestedExecutionConditionKey2).findInstance(testClass).get();
		Object outerInstance3 = instanceMap.get(nestedExecutionConditionKey3).findInstance(testClass).get();
		assertNotSame(outerInstance1, outerInstance2);
		assertNotSame(outerInstance1, outerInstance3);
		assertThat(instanceMap.get(nestedExecutionConditionKey1).getAllInstances()).containsExactly(outerInstance1,
			nestedInstances1.getInnermostInstance());
		assertThat(instanceMap.get(nestedExecutionConditionKey2).getAllInstances()).containsExactly(outerInstance2,
			nestedInstances2.getInnermostInstance());
		assertThat(instanceMap.get(nestedExecutionConditionKey3).getAllInstances()).containsExactly(outerInstance3,
			nestedInstances3.getInnermostInstance());

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class for
		// test2().
		assertSame(outerInstance3, instanceMap.get(postProcessTestInstanceKey).getInnermostInstance());

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
	}

	@Test
	void instancePerClassWithNestedTestClass() {
		Class<?> testClass = InstancePerClassOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerClassOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 1), entry(nestedTestClass, 1));
		int allMethods = 2;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String preDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(testClass);
		String nestedPreDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				preDestroyCallbackTestInstanceKey,
				nestedPreDestroyCallbackTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
		);
		// @formatter:on

		Object instance = instanceMap.get(postProcessTestInstanceKey).getInnermostInstance();
		assertNotNull(instance);
		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertSame(instance, instanceMap.get(beforeAllCallbackKey).getInnermostInstance());
		assertSame(instance, instanceMap.get(afterAllCallbackKey).getInnermostInstance());
		assertSame(instance, instanceMap.get(outerTestExecutionConditionKey).getInnermostInstance());
		assertSame(instance, instanceMap.get(beforeEachCallbackKey).getInnermostInstance());
		assertSame(instance, instanceMap.get(afterEachCallbackKey).getInnermostInstance());
		assertSame(instance, instanceMap.get(preDestroyCallbackTestInstanceKey).getInnermostInstance());

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey).getInnermostInstance();
		assertNotNull(nestedInstance);
		assertNotSame(instance, nestedInstance);
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedPreDestroyCallbackTestInstanceKey).getInnermostInstance());

		Object outerInstance = instanceMap.get(nestedExecutionConditionKey1).findInstance(testClass).get();
		assertSame(outerInstance, instance);
		assertSame(outerInstance, instanceMap.get(postProcessTestInstanceKey).getInnermostInstance());
		assertSame(outerInstance, instanceMap.get(preDestroyCallbackTestInstanceKey).getInnermostInstance());

		assertThat(instanceMap.get(nestedExecutionConditionKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedExecutionConditionKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedExecutionConditionKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	@Test
	void instancePerMethodOnOuterTestClassWithInstancePerClassOnNestedTestClass() {
		Class<?> testClass = MixedLifecyclesOuterTestCase.class;
		Class<?> nestedTestClass = MixedLifecyclesOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 2), entry(nestedTestClass, 1));
		int allMethods = 1;
		int eachMethods = 7;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String preDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(testClass);
		String nestedPreDestroyCallbackTestInstanceKey = preDestroyCallbackTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				preDestroyCallbackTestInstanceKey,
				nestedPreDestroyCallbackTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		TestInstances outerInstances = instanceMap.get(beforeEachCallbackKey);
		assertSame(outerInstances, instanceMap.get(afterEachCallbackKey));
		assertSame(outerInstances, instanceMap.get(outerTestExecutionConditionKey));

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey).getInnermostInstance();
		assertNotNull(nestedInstance);
		assertNotSame(outerInstances.getInnermostInstance(), nestedInstance);
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey3).getInnermostInstance());
		assertSame(nestedInstance, instanceMap.get(nestedPreDestroyCallbackTestInstanceKey).getInnermostInstance());

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class.
		Object outerInstance = instanceMap.get(nestedExecutionConditionKey1).findInstance(testClass).get();
		assertEquals(outerInstances.getInnermostInstance().getClass(), outerInstance.getClass());
		assertNotSame(outerInstances.getInnermostInstance(), outerInstance);
		assertThat(instanceMap.get(nestedExecutionConditionKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey1).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedExecutionConditionKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey2).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedExecutionConditionKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedBeforeEachCallbackKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);
		assertThat(instanceMap.get(nestedAfterEachCallbackKey3).getAllInstances()).containsExactly(outerInstance,
			nestedInstance);

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	@ParameterizedTest
	@EnumSource(Lifecycle.class)
	void containerTemplate(Lifecycle lifecycle) {
		var containerTemplate = ContainerTemplateWithDefaultLifecycleTestCase.class;

		var results = executeTests(r -> r //
				.selectors(selectClass(containerTemplate)) //
				.configurationParameter(Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME, lifecycle.name()));

		results.allEvents().assertStatistics(stats -> stats.failed(0));
		results.testEvents().assertStatistics(stats -> stats.succeeded(4));

		assertThat(instanceCount).containsExactly(entry(containerTemplate, lifecycle == Lifecycle.PER_CLASS ? 1 : 4));
		assertThat(lifecyclesMap.keySet()).containsExactly(containerTemplate);
		assertThat(lifecyclesMap.get(containerTemplate)).filteredOn(Objects::nonNull).containsOnly(lifecycle);
	}

	@ParameterizedTest
	@EnumSource(Lifecycle.class)
	void containerTemplateWithNestedClass(Lifecycle lifecycle) {
		var containerTemplate = ContainerTemplateWithDefaultLifecycleAndNestedClassTestCase.class;
		var nestedClass = ContainerTemplateWithDefaultLifecycleAndNestedClassTestCase.InnerTestCase.class;

		var results = executeTests(r -> r //
				.selectors(selectClass(containerTemplate)) //
				.configurationParameter(Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME, lifecycle.name()));

		results.allEvents().assertStatistics(stats -> stats.failed(0));
		results.testEvents().assertStatistics(stats -> stats.succeeded(4));

		assertThat(instanceCount).containsExactly( //
			entry(containerTemplate, lifecycle == Lifecycle.PER_CLASS ? 1 : 4), //
			entry(nestedClass, lifecycle == Lifecycle.PER_CLASS ? 2 : 4));
		assertThat(lifecyclesMap.keySet()).containsExactlyInAnyOrder(containerTemplate, nestedClass);
		assertThat(lifecyclesMap.get(containerTemplate)).filteredOn(Objects::nonNull).containsOnly(lifecycle);
		assertThat(lifecyclesMap.get(nestedClass)).filteredOn(Objects::nonNull).containsOnly(lifecycle);
	}

	private void performAssertions(Class<?> testClass, int numContainers, int numTests,
			Map.Entry<Class<?>, Integer>[] instanceCountEntries, int allMethods, int eachMethods) {

		EngineExecutionResults executionResults = executeTestsForClass(testClass);

		executionResults.containerEvents().assertStatistics(
			stats -> stats.started(numContainers).finished(numContainers));
		executionResults.testEvents().assertStatistics(stats -> stats.started(numTests).finished(numTests));

		// @formatter:off
		assertAll(
			() -> assertThat(instanceCount).describedAs("instance count").contains(instanceCountEntries),
			() -> assertEquals(allMethods, beforeAllCount, "@BeforeAll count"),
			() -> assertEquals(allMethods, afterAllCount, "@AfterAll count"),
			() -> assertEquals(eachMethods, beforeEachCount, "@BeforeEach count"),
			() -> assertEquals(eachMethods, afterEachCount, "@AfterEach count")
		);
		// @formatter:on
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private Map.Entry<Class<?>, Integer>[] instanceCounts(Map.Entry<Class<?>, Integer>... entries) {
		return entries;
	}

	private static void incrementInstanceCount(Class<?> testClass) {
		instanceCount.compute(testClass, (key, value) -> value == null ? 1 : value + 1);
	}

	private static String executionConditionKey(Class<?> testClass, String testMethod) {
		return concat(ExecutionCondition.class, testClass, testMethod);
	}

	private static String postProcessTestInstanceKey(Class<?> testClass) {
		return concat(TestInstancePostProcessor.class, testClass);
	}

	private static String preDestroyCallbackTestInstanceKey(Class<?> testClass) {
		return concat(TestInstancePreDestroyCallback.class, testClass);
	}

	private static String beforeAllCallbackKey(Class<?> testClass) {
		return concat(BeforeAllCallback.class, testClass);
	}

	private static String afterAllCallbackKey(Class<?> testClass) {
		return concat(AfterAllCallback.class, testClass);
	}

	private static String beforeEachCallbackKey(Class<?> testClass, String testMethod) {
		return concat(BeforeEachCallback.class, testClass, testMethod);
	}

	private static String afterEachCallbackKey(Class<?> testClass, String testMethod) {
		return concat(AfterEachCallback.class, testClass, testMethod);
	}

	private static String testTemplateKey(Class<?> testClass, String testMethod) {
		return concat(TestTemplateInvocationContextProvider.class, testClass, testMethod);
	}

	private static String concat(Class<?> c1, Class<?> c2, String str) {
		return concat(c1.getSimpleName(), c2.getSimpleName(), str);
	}

	private static String concat(Class<?> c1, Class<?> c2) {
		return concat(c1.getSimpleName(), c2.getSimpleName());
	}

	private static String concat(String... args) {
		return join(".", args);
	}

	// -------------------------------------------------------------------------

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	static class InstancePerMethodTestCase {

		InstancePerMethodTestCase() {
			incrementInstanceCount(InstancePerMethodTestCase.class);
		}

		@BeforeAll
		static void beforeAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void test1(TestInfo testInfo) {
			testsInvoked.add(testInfo.getTestMethod().get().getName());
		}

		@Test
		void test2(TestInfo testInfo) {
			testsInvoked.add(testInfo.getTestMethod().get().getName());
		}

		@SingletonTest
		void singletonTest(TestInfo testInfo) {
			testsInvoked.add(testInfo.getTestMethod().get().getName());
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@AfterAll
		static void afterAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	static class InstancePerClassTestCase extends InstancePerMethodTestCase {

		InstancePerClassTestCase() {
			incrementInstanceCount(InstancePerClassTestCase.class);
		}

		@BeforeAll
		void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@AfterAll
		void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	static class SubInstancePerClassTestCase extends InstancePerClassTestCase {
		SubInstancePerClassTestCase() {
			incrementInstanceCount(SubInstancePerClassTestCase.class);
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	static class InstancePerMethodOuterTestCase {

		InstancePerMethodOuterTestCase() {
			incrementInstanceCount(InstancePerMethodOuterTestCase.class);
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		// The following is commented out b/c it's the default.
		// @TestInstance(Lifecycle.PER_METHOD)
		class NestedInstancePerMethodTestCase {

			NestedInstancePerMethodTestCase() {
				incrementInstanceCount(NestedInstancePerMethodTestCase.class);
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(InstanceTrackingExtension.class)
	@TestInstance(Lifecycle.PER_CLASS)
	static class InstancePerClassOuterTestCase {

		InstancePerClassOuterTestCase() {
			incrementInstanceCount(InstancePerClassOuterTestCase.class);
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			NestedInstancePerClassTestCase() {
				incrementInstanceCount(NestedInstancePerClassTestCase.class);
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	static class MixedLifecyclesOuterTestCase {

		MixedLifecyclesOuterTestCase() {
			incrementInstanceCount(MixedLifecyclesOuterTestCase.class);
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			NestedInstancePerClassTestCase() {
				incrementInstanceCount(NestedInstancePerClassTestCase.class);
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())).getInnermostInstance());
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

	// Intentionally not implementing BeforeTestExecutionCallback, AfterTestExecutionCallback,
	// and TestExecutionExceptionHandler, since they are analogous to BeforeEachCallback and
	// AfterEachCallback with regard to instance scope and Lifecycle.
	static class InstanceTrackingExtension
			implements ExecutionCondition, TestInstancePostProcessor, TestInstancePreDestroyCallback, BeforeAllCallback,
			AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestTemplateInvocationContextProvider {

		@Override
		public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
			trackLifecycle(context);
			String testMethod = context.getTestMethod().map(Method::getName).orElse(null);
			if (testMethod == null) {
				assertThat(context.getTestInstance()).isNotPresent();
				if (!isAnnotated(context.getRequiredTestClass().getEnclosingClass(), ContainerTemplate.class)) {
					assertThat(instanceCount.getOrDefault(context.getRequiredTestClass(), 0)).isEqualTo(0);
				}
			}
			instanceMap.put(executionConditionKey(context.getRequiredTestClass(), testMethod),
				context.getTestInstances().orElse(null));

			return ConditionEvaluationResult.enabled("enigma");
		}

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			trackLifecycle(context);
			assertThat(context.getTestInstance()).isNotPresent();
			assertNotNull(testInstance);
			instanceMap.put(postProcessTestInstanceKey(testInstance.getClass()), DefaultTestInstances.of(testInstance));
		}

		@Override
		public void preDestroyTestInstance(ExtensionContext context) {
			trackLifecycle(context);
			assertThat(context.getTestInstance()).isPresent();
			instanceMap.put(preDestroyCallbackTestInstanceKey(context.getRequiredTestClass()),
				DefaultTestInstances.of(context.getTestInstance().get()));
		}

		@Override
		public void beforeAll(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(beforeAllCallbackKey(context.getRequiredTestClass()),
				context.getTestInstances().orElse(null));
		}

		@Override
		public void afterAll(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(afterAllCallbackKey(context.getRequiredTestClass()),
				context.getTestInstances().orElse(null));
		}

		@Override
		public void beforeEach(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(
				beforeEachCallbackKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getRequiredTestInstances());
		}

		@Override
		public void afterEach(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(
				afterEachCallbackKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getRequiredTestInstances());
		}

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			trackLifecycle(context);
			return isAnnotated(context.getTestMethod(), SingletonTest.class);
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(testTemplateKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getTestInstances().orElse(null));

			return Stream.of(new TestTemplateInvocationContext() {
			});
		}

		private static void trackLifecycle(ExtensionContext context) {
			assertThat(context.getRoot().getTestInstanceLifecycle()).isEmpty();
			lifecyclesMap.computeIfAbsent(context.getRequiredTestClass(), clazz -> new ArrayList<>()).add(
				context.getTestInstanceLifecycle().orElse(null));
		}

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@TestTemplate
	@interface SingletonTest {
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(Twice.class)
	@ExtendWith(InstanceTrackingExtension.class)
	static class ContainerTemplateWithDefaultLifecycleTestCase {

		ContainerTemplateWithDefaultLifecycleTestCase() {
			incrementInstanceCount(ContainerTemplateWithDefaultLifecycleTestCase.class);
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}
	}

	@ContainerTemplate
	@ExtendWith(Twice.class)
	@ExtendWith(InstanceTrackingExtension.class)
	static class ContainerTemplateWithDefaultLifecycleAndNestedClassTestCase {

		ContainerTemplateWithDefaultLifecycleAndNestedClassTestCase() {
			incrementInstanceCount(ContainerTemplateWithDefaultLifecycleAndNestedClassTestCase.class);
		}

		@Nested
		class InnerTestCase {

			public InnerTestCase() {
				incrementInstanceCount(InnerTestCase.class);
			}

			@Test
			void test1() {
			}

			@Test
			void test2() {
			}
		}
	}

	private static class Twice implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
				ExtensionContext context) {
			return Stream.of(new Ctx(), new Ctx());
		}

		private record Ctx() implements ContainerTemplateInvocationContext {
		}
	}

}
