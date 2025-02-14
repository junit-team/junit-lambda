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
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.createDisplayNameSupplierForNestedClass;
import static org.junit.jupiter.engine.descriptor.ResourceLockAware.enclosingInstanceTypesDependentResourceLocksProviderEvaluator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
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
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <h2>Default Display Names</h2>
 *
 * <p>The default display name for a non-static nested test class is the simple
 * name of the class.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class NestedClassTestDescriptor extends ClassBasedTestDescriptor {

	public static final String SEGMENT_TYPE = "nested-class";

	public NestedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass,
			Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration) {
		super(uniqueId, testClass,
			createDisplayNameSupplierForNestedClass(enclosingInstanceTypes, testClass, configuration), configuration);
	}

	private NestedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, String displayName,
			JupiterConfiguration configuration) {
		super(uniqueId, testClass, displayName, configuration);
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected NestedClassTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new NestedClassTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), getTestClass(), getDisplayName(),
			configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		// return modifiable copy
		Set<TestTag> allTags = new LinkedHashSet<>(this.tags);
		getParent().ifPresent(parentDescriptor -> allTags.addAll(parentDescriptor.getTags()));
		return allTags;
	}

	// --- TestClassAware ------------------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return getEnclosingTestClasses(getParent().orElse(null));
	}

	@API(status = INTERNAL, since = "5.12")
	public static List<Class<?>> getEnclosingTestClasses(TestDescriptor parent) {
		if (parent instanceof TestClassAware) {
			TestClassAware parentClassDescriptor = (TestClassAware) parent;
			List<Class<?>> result = new ArrayList<>(parentClassDescriptor.getEnclosingTestClasses());
			result.add(parentClassDescriptor.getTestClass());
			return result;
		}
		return emptyList();
	}

	// --- ClassBasedTestDescriptor --------------------------------------------

	@Override
	protected TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {

		// Extensions registered for nested classes and below are not to be used for instantiating and initializing outer classes
		ExtensionRegistry extensionRegistryForOuterInstanceCreation = parentExecutionContext.getExtensionRegistry();
		TestInstances outerInstances = parentExecutionContext.getTestInstancesProvider().getTestInstances(
			extensionRegistryForOuterInstanceCreation, context);
		return instantiateTestClass(Optional.of(outerInstances), registry, extensionContext);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return enclosingInstanceTypesDependentResourceLocksProviderEvaluator(this::getEnclosingTestClasses, (provider,
				enclosingInstanceTypes) -> provider.provideForNestedClass(enclosingInstanceTypes, getTestClass()));
	}

}
