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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.ContainerTemplate;

/**
 * {@code @ResourceLock} is used to declare that the annotated test class or test
 * method requires access to a shared resource identified by a key.
 *
 * <p>The resource key is specified via {@link #value}. In addition, {@link #mode}
 * allows one to specify whether the annotated test class or test method requires
 * {@link ResourceAccessMode#READ_WRITE READ_WRITE} or
 * {@link ResourceAccessMode#READ READ} access to the resource. In the former case,
 * execution of the annotated element will occur while no other test class or
 * test method that uses the shared resource is being executed. In the latter case,
 * the annotated element may be executed concurrently with other test classes or
 * methods that also require {@code READ} access but not at the same time as any
 * other test that requires {@code READ_WRITE} access.
 *
 * <p>This guarantee extends to lifecycle methods of a test class or method. For
 * example, if a test method is annotated with {@code @ResourceLock} the lock
 * will be acquired before any {@link org.junit.jupiter.api.BeforeEach @BeforeEach}
 * methods are executed and released after all
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods have been executed.
 *
 * <p>This annotation can be repeated to declare the use of multiple shared resources.
 *
 * <p>Uniqueness of a shared resource is determined by both the {@link #value()}
 * and the {@link #mode()}. Duplicated shared resources do not cause errors.
 *
 * <p>Since JUnit Jupiter 5.4, this annotation is {@linkplain Inherited inherited}
 * within class hierarchies.
 *
 * <p>Since JUnit Jupiter 5.12, this annotation supports adding shared resources
 * dynamically at runtime via {@link #providers}. Resources declared "statically"
 * using {@link #value()} and {@link #mode()} are combined with "dynamic" resources
 * added via {@link #providers()}. For example, declaring resource "A" via
 * {@code @ResourceLock("A")} and resource "B" via a provider returning
 * {@code new Lock("B")} will result in two shared resources "A" and "B".
 *
 * <p>Since JUnit Jupiter 5.12, this annotation supports declaring "static"
 * shared resources for <em>direct</em> child nodes via the {@link #target()}
 * attribute. Using {@link ResourceLockTarget#CHILDREN} in a class-level annotation
 * has the same semantics as adding an annotation with the same {@link #value()}
 * and {@link #mode()} to each test method and nested test class declared in the
 * annotated class. This may improve parallelization when a test class declares a
 * {@link ResourceAccessMode#READ READ} lock, but only a few methods hold
 * {@link ResourceAccessMode#READ_WRITE READ_WRITE} lock. Note that
 * {@code target = CHILDREN} means that {@link #value()} and {@link #mode()} no
 * longer apply to a node declaring the annotation. However, the {@link #providers()}
 * attribute remains applicable, and the target of "dynamic" shared resources added
 * via implementations of {@link ResourceLocksProvider} is not changed.
 *
 * <p>Shared resources declared on or provided for methods or nested test
 * classes in a {@link ContainerTemplate @ContainerTemplate} are propagated as
 * if they were declared on the outermost enclosing {@code @ContainerTemplate}
 * class itself.
 *
 * @see Isolated
 * @see Resources
 * @see ResourceAccessMode
 * @see ResourceLockTarget
 * @see ResourceLocks
 * @see ResourceLocksProvider
 * @since 5.3
 */
@API(status = STABLE, since = "5.10")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@Repeatable(ResourceLocks.class)
public @interface ResourceLock {

	/**
	 * The resource key.
	 *
	 * <p>Defaults to an empty string.
	 *
	 * @see Resources
	 * @see ResourceLocksProvider.Lock#getKey()
	 */
	String value() default "";

	/**
	 * The resource access mode.
	 *
	 * <p>Defaults to {@link ResourceAccessMode#READ_WRITE READ_WRITE}.
	 *
	 * @see ResourceAccessMode
	 * @see ResourceLocksProvider.Lock#getAccessMode()
	 */
	ResourceAccessMode mode() default ResourceAccessMode.READ_WRITE;

	/**
	 * An array of one or more classes implementing {@link ResourceLocksProvider}.
	 *
	 * <p>Defaults to an empty array.
	 *
	 * @see ResourceLocksProvider.Lock
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	Class<? extends ResourceLocksProvider>[] providers() default {};

	/**
	 * The target of a resource created from {@link #value()} and {@link #mode()}.
	 *
	 * <p>Defaults to {@link ResourceLockTarget#SELF SELF}.
	 *
	 * <p>Note that using {@link ResourceLockTarget#CHILDREN} in a method-level
	 * annotation results in an exception.
	 *
	 * @see ResourceLockTarget
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	ResourceLockTarget target() default ResourceLockTarget.SELF;

}
