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
import static org.junit.jupiter.engine.extension.MutableExtensionRegistry.createRegistryFrom;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateInvocationTestDescriptor extends JupiterTestDescriptor
		implements TestClassAware, ResourceLockAware {

	public static final String SEGMENT_TYPE = "container-template-invocation";

	private final ContainerTemplateTestDescriptor parent;
	private ContainerTemplateInvocationContext invocationContext;
	private final int index;

	public ContainerTemplateInvocationTestDescriptor(UniqueId uniqueId, ContainerTemplateTestDescriptor parent,
			ContainerTemplateInvocationContext invocationContext, int index, TestSource source,
			JupiterConfiguration configuration) {
		super(uniqueId, invocationContext.getDisplayName(index), source, configuration);
		this.parent = parent;
		this.invocationContext = invocationContext;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected ContainerTemplateInvocationTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new ContainerTemplateInvocationTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), parent,
			this.invocationContext, this.index, getSource().orElse(null), this.configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public String getLegacyReportingName() {
		return getTestClass().getName() + "[" + index + "]";
	}

	// --- TestClassAware ------------------------------------------------------

	@Override
	public Class<?> getTestClass() {
		return parent.getTestClass();
	}

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return parent.getEnclosingTestClasses();
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public ExclusiveResourceCollector getExclusiveResourceCollector() {
		return parent.getExclusiveResourceCollector();
	}

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return parent.getResourceLocksProviderEvaluator();
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) {
		return SkipResult.doNotSkip();
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		MutableExtensionRegistry registry = context.getExtensionRegistry();
		List<Extension> additionalExtensions = this.invocationContext.getAdditionalExtensions();
		if (!additionalExtensions.isEmpty()) {
			MutableExtensionRegistry childRegistry = createRegistryFrom(registry, Stream.empty());
			additionalExtensions.forEach(
				extension -> childRegistry.registerExtension(extension, this.invocationContext));
			registry = childRegistry;
		}
		ExtensionContext extensionContext = new ContainerTemplateInvocationExtensionContext(
			context.getExtensionContext(), context.getExecutionListener(), this, context.getConfiguration(), registry);
		return context.extend() //
				.withExtensionContext(extensionContext).withExtensionRegistry(registry) //
				.build();
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		Visitor visitor = context.getExecutionListener()::dynamicTestRegistered;
		getChildren().forEach(child -> child.accept(visitor));
		return context;
	}

	@Override
	public void cleanUp(JupiterEngineExecutionContext context) {
		// forget invocationContext so it can be garbage collected
		this.invocationContext = null;
	}
}
