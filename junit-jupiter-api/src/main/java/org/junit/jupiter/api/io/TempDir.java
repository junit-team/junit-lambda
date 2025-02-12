/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.io;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * {@code @TempDir} can be used to annotate a field in a test class or a parameter
 * in a test class constructor, lifecycle method, or test method of type
 * {@link Path} or {@link File} that should be resolved into a temporary directory.
 *
 * <h2>Creation</h2>
 *
 * <p>The temporary directory is only created if a field in a test class or a
 * parameter in a test class constructor, lifecycle method, or test method is
 * annotated with {@code @TempDir}. An {@link ExtensionConfigurationException} or
 * a {@link ParameterResolutionException} will be thrown in one of the following
 * cases:
 *
 * <ul>
 * <li>If the field type or parameter type is neither {@code Path} nor {@code File}.</li>
 * <li>If a field is declared as {@code final}.</li>
 * <li>If the temporary directory cannot be created.</li>
 * <li>If the field type or parameter type is {@code File} and a custom
 *     {@link TempDir#factory() factory} is used, which creates a temporary
 *     directory that does not belong to the
 *     {@linkplain java.nio.file.FileSystems#getDefault() default file system}.
 * </li>
 * </ul>
 *
 * <h2>Scope</h2>
 *
 * <p>By default, a separate temporary directory is created for every declaration
 * of the {@code @TempDir} annotation. For better isolation when using
 * {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_METHOD @TestInstance(Lifecycle.PER_METHOD)}
 * semantics, you can annotate an instance field or a parameter in the test class
 * constructor with {@code @TempDir} so that each test method uses a separate
 * temporary directory. Alternatively, if you want to share a temporary directory
 * across all tests in a test class, you should declare the annotation on a
 * {@code static} field or on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method.
 *
 * <h3>Old behavior</h3>
 *
 * <p>You can revert to the old behavior of using a single temporary directory
 * by setting the {@value #SCOPE_PROPERTY_NAME} configuration parameter to
 * {@code per_context}. In that case, the scope of the temporary directory
 * depends on where the first {@code @TempDir} annotation is encountered when
 * executing a test class. The temporary directory will be shared by all tests
 * in a class when the annotation is present on a {@code static} field or on a
 * parameter of a {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method.
 * Otherwise &mdash; for example, when {@code @TempDir} is only used on instance
 * fields or on parameters in test,
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach}, or
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods &mdash; each test
 * will use its own temporary directory.
 *
 * <h2>Clean Up</h2>
 *
 * <p>By default, when the end of the scope of a temporary directory is reached,
 * &mdash; when the test method or class has finished execution &mdash; JUnit will
 * attempt to clean up the temporary directory by recursively deleting all files
 * and directories in the temporary directory and, finally, the temporary directory
 * itself. Symbolic and other types of links, such as junctions on Windows, are
 * not followed. A warning is logged when deleting a link that targets a
 * location outside the temporary directory. In case deletion of a file or
 * directory fails, an {@link IOException} will be thrown that will cause the
 * test or test class to fail.
 *
 * <p>The {@link #cleanup} attribute allows you to configure the {@link CleanupMode}.
 * If the cleanup mode is set to {@link CleanupMode#NEVER NEVER}, the temporary
 * directory will not be cleaned up after the test completes. If the cleanup mode is
 * set to {@link CleanupMode#ON_SUCCESS ON_SUCCESS}, the temporary directory will
 * only be cleaned up if the test completes successfully. By default, the
 * {@link CleanupMode#ALWAYS ALWAYS} clean up mode will be used, but this can be
 * configured globally by setting the {@value #DEFAULT_CLEANUP_MODE_PROPERTY_NAME}
 * configuration parameter.
 *
 * @since 5.4
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.10")
public @interface TempDir {

	/**
	 * Property name used to set the default temporary directory factory class name:
	 * {@value}
	 *
	 * <h4>Supported Values</h4>
	 *
	 * <p>Supported values include fully qualified class names for types that
	 * implement {@link TempDirFactory}.
	 *
	 * <p>If not specified, the default is {@link TempDirFactory.Standard}.
	 *
	 * @since 5.10
	 */
	@API(status = EXPERIMENTAL, since = "5.10")
	String DEFAULT_FACTORY_PROPERTY_NAME = "junit.jupiter.tempdir.factory.default";

	/**
	 * Factory for the temporary directory.
	 *
	 * <p>If the {@value #SCOPE_PROPERTY_NAME} configuration parameter is set to
	 * {@code per_context}, no custom factory is allowed.
	 *
	 * <p>Defaults to {@link TempDirFactory.Standard}.
	 *
	 * <p>As an alternative to setting this attribute, a global
	 * {@link TempDirFactory} can be configured for the entire test suite via
	 * the {@value #DEFAULT_FACTORY_PROPERTY_NAME} configuration parameter.
	 * See the User Guide for details. Note, however, that a {@code @TempDir}
	 * declaration with a custom {@code factory} always overrides a global
	 * {@code TempDirFactory}.
	 *
	 * @return the type of {@code TempDirFactory} to use
	 * @since 5.10
	 * @see TempDirFactory
	 */
	@API(status = EXPERIMENTAL, since = "5.10")
	Class<? extends TempDirFactory> factory() default TempDirFactory.class;

	/**
	 * Property name used to set the scope of temporary directories created via
	 * the {@link TempDir @TempDir} annotation: {@value}
	 *
	 * <h4>Supported Values</h4>
	 * <ul>
	 * <li>{@code per_context}: creates a single temporary directory for the
	 * entire test class or method, depending on where {@code @TempDir} is first
	 * declared</li>
	 * <li>{@code per_declaration}: creates separate temporary directories for
	 * each declaration site of the {@code @TempDir} annotation</li>
	 * </ul>
	 *
	 * <p>If not specified, the default is {@code per_declaration}.
	 *
	 * @since 5.8
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	@API(status = DEPRECATED, since = "5.9")
	String SCOPE_PROPERTY_NAME = "junit.jupiter.tempdir.scope";

	/**
	 * The name of the configuration parameter that is used to configure the
	 * default {@link CleanupMode}.
	 *
	 * <p>If this configuration parameter is not set, {@link CleanupMode#ALWAYS}
	 * will be used as the default.
	 *
	 * @since 5.9
	 */
	@API(status = EXPERIMENTAL, since = "5.9")
	String DEFAULT_CLEANUP_MODE_PROPERTY_NAME = "junit.jupiter.tempdir.cleanup.mode.default";

	/**
	 * How the temporary directory gets cleaned up after the test completes.
	 *
	 * @since 5.9
	 */
	@API(status = STABLE, since = "5.11")
	CleanupMode cleanup() default CleanupMode.DEFAULT;

}
