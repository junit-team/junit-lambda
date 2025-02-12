/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.findMethod;
import static org.junit.platform.commons.support.ReflectionSupport.findMethods;
import static org.junit.platform.commons.support.ReflectionSupport.newInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Abstract base class for unit testing a concrete {@link ExecutionCondition}
 * implementation.
 *
 * <p><strong>WARNING</strong>: this abstract base class currently does not
 * support tests in {@code @Nested} test classes within the
 * {@linkplain #getTestClass() test class}, since {@link #beforeEach(TestInfo)}
 * instantiates the test class using the no-args default constructor.
 *
 * @since 5.1
 */
@TestInstance(Lifecycle.PER_CLASS)
abstract class AbstractExecutionConditionTests {

	private final ExtensionContext context = mock();

	private ConditionEvaluationResult result;

	@BeforeAll
	void ensureAllTestMethodsAreCovered() {
		Predicate<Method> isTestMethod = method -> method.isAnnotationPresent(Test.class);

		List<String> methodsToTest = findMethods(getTestClass(), isTestMethod, TOP_DOWN).stream()//
				.map(Method::getName).sorted().toList();

		List<String> localTestMethods = findMethods(getClass(), isTestMethod, TOP_DOWN).stream()//
				.map(Method::getName).sorted().toList();

		assertThat(localTestMethods).containsExactlyElementsOf(methodsToTest);
	}

	@BeforeEach
	void beforeEach(TestInfo testInfo) {
		when(this.context.getElement()).thenReturn(method(testInfo));
		when(this.context.getTestInstance()).thenReturn(Optional.of(newInstance(getTestClass())));
		doReturn(getTestClass()).when(this.context).getRequiredTestClass();
	}

	protected abstract ExecutionCondition getExecutionCondition();

	protected abstract Class<?> getTestClass();

	protected void evaluateCondition() {
		this.result = getExecutionCondition().evaluateExecutionCondition(this.context);
	}

	protected void assertEnabled() {
		assertFalse(this.result.isDisabled(), "Should be enabled");
	}

	protected void assertDisabled() {
		assertTrue(this.result.isDisabled(), "Should be disabled");
	}

	protected void assertReasonContains(String text) {
		assertThat(this.result.getReason()).hasValueSatisfying(reason -> assertThat(reason).contains(text));
	}

	protected void assertCustomDisabledReasonIs(String text) {
		if (this.result.isDisabled()) {
			assertThat(this.result.getReason()).hasValueSatisfying(
				reason -> assertThat(reason).contains(" ==> " + text));
		}
	}

	private Optional<AnnotatedElement> method(TestInfo testInfo) {
		return method(getTestClass(), testInfo.getTestMethod().get().getName());
	}

	private Optional<AnnotatedElement> method(Class<?> testClass, String methodName) {
		return Optional.of(findMethod(testClass, methodName).get());
	}

}
