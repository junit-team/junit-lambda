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

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.api.parallel.ResourceLockTarget.CHILDREN;
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.determineDisplayNameForMethod;
import static org.junit.jupiter.engine.descriptor.ResourceLockAware.enclosingInstanceTypesDependentResourceLocksProviderEvaluator;
import static org.junit.platform.commons.util.CollectionUtils.forEachInReverseOrder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Base class for {@link TestDescriptor TestDescriptors} based on Java methods.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public abstract class MethodBasedTestDescriptor extends JupiterTestDescriptor implements ResourceLockAware {

	private static final Logger logger = LoggerFactory.getLogger(MethodBasedTestDescriptor.class);

	private final Class<?> testClass;
	private final Method testMethod;

	/**
	 * Set of method-level tags; does not contain tags from parent.
	 */
	private final Set<TestTag> tags;

	MethodBasedTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod,
			Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration) {
		this(uniqueId, determineDisplayNameForMethod(enclosingInstanceTypes, testClass, testMethod, configuration),
			testClass, testMethod, configuration);
	}

	MethodBasedTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Method testMethod,
			JupiterConfiguration configuration) {
		super(uniqueId, displayName, MethodSource.from(testClass, testMethod), configuration);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = testMethod;
		this.tags = getTags(testMethod);
	}

	@Override
	public final Set<TestTag> getTags() {
		// return modifiable copy
		Set<TestTag> allTags = new LinkedHashSet<>(this.tags);
		getParent().ifPresent(parentDescriptor -> allTags.addAll(parentDescriptor.getTags()));
		return allTags;
	}

	@Override
	public ExclusiveResourceCollector getExclusiveResourceCollector() {
		// There's no need to cache this as this method should only be called once
		ExclusiveResourceCollector collector = ExclusiveResourceCollector.from(getTestMethod());

		if (collector.getStaticResourcesFor(CHILDREN).findAny().isPresent()) {
			String message = "'ResourceLockTarget.CHILDREN' is not supported for methods." + //
					" Invalid method: " + getTestMethod();
			throw new JUnitException(message);
		}

		return collector;
	}

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return enclosingInstanceTypesDependentResourceLocksProviderEvaluator(this::getEnclosingTestClasses,
			(provider, enclosingInstanceTypes) -> provider.provideForMethod(enclosingInstanceTypes, getTestClass(),
				getTestMethod()));
	}

	private List<Class<?>> getEnclosingTestClasses() {
		return getParent() //
				.filter(TestClassAware.class::isInstance) //
				.map(TestClassAware.class::cast) //
				.map(TestClassAware::getEnclosingTestClasses) //
				.orElseGet(Collections::emptyList);
	}

	@Override
	protected Optional<ExecutionMode> getExplicitExecutionMode() {
		return getExecutionModeFromAnnotation(getTestMethod());
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public String getLegacyReportingName() {
		return String.format("%s(%s)", testMethod.getName(),
			ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()));
	}

	/**
	 * Invoke {@link TestWatcher#testDisabled(ExtensionContext, Optional)} on each
	 * registered {@link TestWatcher}, in registration order.
	 *
	 * @since 5.4
	 */
	@Override
	public void nodeSkipped(JupiterEngineExecutionContext context, TestDescriptor descriptor, SkipResult result) {
		if (context != null) {
			invokeTestWatchers(context, false,
				watcher -> watcher.testDisabled(context.getExtensionContext(), result.getReason()));
		}
	}

	/**
	 * @since 5.4
	 */
	protected void invokeTestWatchers(JupiterEngineExecutionContext context, boolean reverseOrder,
			Consumer<TestWatcher> callback) {

		List<TestWatcher> watchers = context.getExtensionRegistry().getExtensions(TestWatcher.class);

		Consumer<TestWatcher> action = watcher -> {
			try {
				callback.accept(watcher);
			}
			catch (Throwable throwable) {
				UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
				ExtensionContext extensionContext = context.getExtensionContext();
				logger.warn(throwable,
					() -> String.format("Failed to invoke TestWatcher [%s] for method [%s] with display name [%s]",
						watcher.getClass().getName(),
						ReflectionUtils.getFullyQualifiedMethodName(extensionContext.getRequiredTestClass(),
							extensionContext.getRequiredTestMethod()),
						getDisplayName()));
			}
		};
		if (reverseOrder) {
			forEachInReverseOrder(watchers, action);
		}
		else {
			watchers.forEach(action);
		}
	}

}
