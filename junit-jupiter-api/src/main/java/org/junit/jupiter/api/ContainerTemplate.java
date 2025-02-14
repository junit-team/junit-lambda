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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.platform.commons.annotation.Testable;

/**
 * {@code @ContainerTemplate} is used to signal that the annotated class is a
 * <em>container template</em>.
 *
 * <p>In contrast to regular test classes, a container template is not directly
 * a test class but rather a template for a set of test cases. As such, it is
 * designed to be invoked multiple times depending on the number of {@linkplain
 * org.junit.jupiter.api.extension.ContainerTemplateInvocationContext invocation
 * contexts} returned by the registered {@linkplain
 * org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider
 * providers}. Must be used together with at least one provider. Otherwise,
 * execution will fail.
 *
 * <p>Each invocation of a container template method behaves like the execution
 * of a regular test class with full support for the same lifecycle callbacks
 * and extensions.
 *
 * <p>{@code @ContainerTemplate} may be combined with {@link Nested @Nested} and
 * a container template may contain regular nested test classes or nested
 * container templates.
 *
 * <p>{@code @ContainerTemplate} may also be used as a meta-annotation in order
 * to create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @ContainerTemplate}.
 *
 * <h2>Inheritance</h2>
 *
 * <p>The {@code @ContainerTemplate} annotation is not inherited to subclasses
 * but needs to be declared on each container template class individually.
 *
 * @since 5.13
 * @see TestTemplate
 * @see org.junit.jupiter.api.extension.ContainerTemplateInvocationContext
 * @see org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.13")
@Testable
public @interface ContainerTemplate {
}
