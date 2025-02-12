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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.parallel.Execution;

/**
 * {@code @TestMethodOrder} is a type-level annotation that is used to configure
 * a {@link #value MethodOrderer} for the <em>test methods</em> of the annotated
 * test class or test interface.
 *
 * <p>In this context, the term "test method" refers to any method annotated with
 * {@code @Test}, {@code @RepeatedTest}, {@code @ParameterizedTest},
 * {@code @TestFactory}, or {@code @TestTemplate}.
 *
 * <p>If {@code @TestMethodOrder} is not explicitly declared on a test class,
 * inherited from a parent class, or declared on a test interface implemented by
 * a test class, test methods will be ordered using a default algorithm that is
 * deterministic but intentionally nonobvious.
 *
 * <p>As an alternative to {@code @TestMethodOrder}, a global {@link MethodOrderer}
 * can be configured for the entire test suite via the
 * {@value MethodOrderer#DEFAULT_ORDER_PROPERTY_NAME} configuration parameter. See
 * the User Guide for details. Note, however, that a {@code @TestClassOrder}
 * declaration always overrides a global {@code ClassOrderer}.
 *
 * <h2>Example Usage</h2>
 *
 * <p>The following demonstrates how to guarantee that test methods are executed
 * in the order specified via the {@link Order @Order} annotation.
 *
 * <pre class="code">
 * {@literal @}TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 * class OrderedTests {
 *
 *     {@literal @}Test
 *     {@literal @}Order(1)
 *     void nullValues() {}
 *
 *     {@literal @}Test
 *     {@literal @}Order(2)
 *     void emptyValues() {}
 *
 *     {@literal @}Test
 *     {@literal @}Order(3)
 *     void validValues() {}
 * }</pre>
 *
 * <h2>Parallel Execution</h2>
 * <p>Using a {@link MethodOrderer} disables parallel execution unless the test
 * class or test method is annotated with
 * {@link Execution @Execution(CONCURRENT)}.
 *
 * @since 5.4
 * @see MethodOrderer
 * @see TestClassOrder
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = STABLE, since = "5.7")
public @interface TestMethodOrder {

	/**
	 * The {@link MethodOrderer} to use.
	 *
	 * @see MethodOrderer
	 * @see MethodOrderer.MethodName
	 * @see MethodOrderer.DisplayName
	 * @see MethodOrderer.OrderAnnotation
	 * @see MethodOrderer.Random
	 */
	Class<? extends MethodOrderer> value();

}
