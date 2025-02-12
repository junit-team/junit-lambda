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

/**
 * {@code @TestClassOrder} is a type-level annotation that is used to configure
 * a {@link #value ClassOrderer} for the {@link Nested @Nested} test classes of
 * the annotated test class.
 *
 * <p>If {@code @TestClassOrder} is not explicitly declared on a test class,
 * inherited from a parent class, declared on a test interface implemented by
 * a test class, or inherited from an {@linkplain Class#getEnclosingClass() enclosing
 * class}, {@code @Nested} test classes will be executed in arbitrary order.
 *
 * <p>As an alternative to {@code @TestClassOrder}, a global {@link ClassOrderer}
 * can be configured for the entire test suite via the
 * {@value ClassOrderer#DEFAULT_ORDER_PROPERTY_NAME} configuration parameter. See
 * the User Guide for details. Note, however, that a {@code @TestClassOrder}
 * declaration always overrides a global {@code ClassOrderer}.
 *
 * <h2>Example Usage</h2>
 *
 * <p>The following demonstrates how to guarantee that {@code @Nested} test classes
 * are executed in the order specified via the {@link Order @Order} annotation.
 *
 * <pre class="code">
 * {@literal @}TestClassOrder(ClassOrderer.OrderAnnotation.class)
 * class OrderedNestedTests {
 *
 *     {@literal @}Nested
 *     {@literal @}Order(1)
 *     class PrimaryTests {
 *         // {@literal @}Test methods ...
 *     }
 *
 *     {@literal @}Nested
 *     {@literal @}Order(2)
 *     class SecondaryTests {
 *         // {@literal @}Test methods ...
 *     }
 * }</pre>
 *
 * @since 5.8
 * @see ClassOrderer
 * @see TestMethodOrder
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = STABLE, since = "5.10")
public @interface TestClassOrder {

	/**
	 * The {@link ClassOrderer} to use.
	 *
	 * @see ClassOrderer
	 * @see ClassOrderer.ClassName
	 * @see ClassOrderer.DisplayName
	 * @see ClassOrderer.OrderAnnotation
	 * @see ClassOrderer.Random
	 */
	Class<? extends ClassOrderer> value();

}
