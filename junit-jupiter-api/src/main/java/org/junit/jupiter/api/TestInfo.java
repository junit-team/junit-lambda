/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * {@code TestInfo} is used to inject information about the current test or
 * container into to {@code @Test}, {@code @RepeatedTest},
 * {@code @ParameterizedTest}, {@code @TestFactory}, {@code @BeforeEach},
 * {@code @AfterEach}, {@code @BeforeAll}, and {@code @AfterAll} methods.
 *
 * <p>If a method parameter is of type {@link TestInfo}, JUnit will supply
 * an instance of {@code TestInfo} corresponding to the current test or
 * container as the value for the parameter.
 *
 * @since 5.0
 * @see Test
 * @see RepeatedTest
 * @see TestFactory
 * @see BeforeEach
 * @see AfterEach
 * @see BeforeAll
 * @see AfterAll
 * @see DisplayName
 * @see Tag
 */
@API(status = STABLE, since = "5.0")
public interface TestInfo {

	/**
	 * Get the display name of the current test or container.
	 *
	 * <p>The display name is either a default name or a custom name configured
	 * via {@link DisplayName @DisplayName}.
	 *
	 * <h4>Default Display Names</h4>
	 *
	 * <p>If the context in which {@code TestInfo} is used is at the container
	 * level, the default display name is generated based on the name of the
	 * test class. For top-level and {@link Nested @Nested} test classes, the
	 * default display name is the {@linkplain Class#getSimpleName simple name}
	 * of the class. For {@code static} nested test classes, the default display
	 * name is the default display name for the enclosing class concatenated with
	 * the {@linkplain Class#getSimpleName simple name} of the {@code static}
	 * nested class, separated by a dollar sign ({@code $}). For example, the
	 * default display names for the following test classes are
	 * {@code TopLevelTests}, {@code NestedTests}, and {@code TopLevelTests$StaticTests}.
	 *
	 * <pre class="code">
	 *   class TopLevelTests {
	 *
	 *      {@literal @}Nested
	 *      class NestedTests {}
	 *
	 *      static class StaticTests {}
	 *   }</pre>
	 *
	 * <p>If the context in which {@code TestInfo} is used is at the test level,
	 * the default display name is the name of the test method concatenated with
	 * a comma-separated list of {@linkplain Class#getSimpleName simple names}
	 * of the parameter types in parentheses. For example, the default display
	 * name for the following test method is {@code testUser(TestInfo, User)}.
	 *
	 * <pre class="code">
	 *   {@literal @}Test
	 *   void testUser(TestInfo testInfo, {@literal @}Mock User user) {}</pre>
	 *
	 * <p>Note that display names are typically used for test reporting in IDEs
	 * and build tools and may contain spaces, special characters, and even emoji.
	 *
	 * @return the display name of the test or container; never {@code null} or blank
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags for the current test or container.
	 *
	 * <p>Tags may be declared directly on the test element or <em>inherited</em>
	 * from an outer context.
	 */
	Set<String> getTags();

	/**
	 * Get the {@link Class} associated with the current test or container, if available.
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the {@link Method} associated with the current test or container, if available.
	 */
	Optional<Method> getTestMethod();

}
