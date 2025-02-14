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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * {@code @Nested} is used to signal that the annotated class is a nested,
 * non-static test class (i.e., an <em>inner class</em>) that can share
 * setup and state with an instance of its {@linkplain Class#getEnclosingClass()
 * enclosing class}. The enclosing class may be a top-level test class or
 * another {@code @Nested} test class, and nesting can be arbitrarily deep.
 *
 * <p>{@code @Nested} test classes may be ordered via
 * {@link TestClassOrder @TestClassOrder} or a global {@link ClassOrderer}.
 *
 * <p>{@code @Nested} may be combined with
 * {@link ContainerTemplate @ContainerTemplate}.
 *
 * <h2>Test Instance Lifecycle</h2>
 *
 * <ul>
 * <li>A {@code @Nested} test class <em>can</em> be configured with its own
 * {@link Lifecycle} mode which may differ from that of an enclosing test
 * class.</li>
 * <li>A {@code @Nested} test class <em>cannot</em> change the {@link Lifecycle}
 * mode of an enclosing test class.</li>
 * </ul>
 *
 * @since 5.0
 * @see ContainerTemplate
 * @see Test
 * @see TestInstance
 * @see TestClassOrder
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.0")
public @interface Nested {
}
