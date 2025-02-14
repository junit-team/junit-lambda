/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.emptyList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.createDisplayNameSupplierForClass;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.ExtensionContextSupplier;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <h2>Default Display Names</h2>
 *
 * <p>The default display name for a top-level or nested static test class is
 * the fully qualified name of the class with the package name and leading dot
 * (".") removed.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class ClassTestDescriptor extends ClassBasedTestDescriptor {

	public static final String SEGMENT_TYPE = "class";

	public ClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
		super(uniqueId, testClass, createDisplayNameSupplierForClass(testClass, configuration), configuration);
	}

	private ClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, String displayName,
			JupiterConfiguration configuration) {
		super(uniqueId, testClass, displayName, configuration);
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected ClassTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new ClassTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), getTestClass(), getDisplayName(),
			configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	// --- TestClassAware ------------------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return emptyList();
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public ExecutionMode getExecutionMode() {
		return getExplicitExecutionMode().orElseGet(
			() -> JupiterTestDescriptor.toExecutionMode(configuration.getDefaultClassesExecutionMode()));
	}

	// --- ClassBasedTestDescriptor --------------------------------------------

	@Override
	protected TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {
		return instantiateTestClass(Optional.empty(), registry, extensionContext);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return provider -> provider.provideForClass(getTestClass());
	}

}
