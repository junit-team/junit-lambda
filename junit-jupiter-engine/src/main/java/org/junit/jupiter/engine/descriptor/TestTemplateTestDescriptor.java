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
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for {@link org.junit.jupiter.api.TestTemplate @TestTemplate}
 * methods.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class TestTemplateTestDescriptor extends MethodBasedTestDescriptor implements Filterable {

	public static final String SEGMENT_TYPE = "test-template";
	private final DynamicDescendantFilter dynamicDescendantFilter;

	public TestTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method templateMethod,
			Supplier<List<Class<?>>> enclosingInstanceTypes, JupiterConfiguration configuration) {
		super(uniqueId, testClass, templateMethod, enclosingInstanceTypes, configuration);
		this.dynamicDescendantFilter = new DynamicDescendantFilter();
	}

	private TestTemplateTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Method templateMethod,
			JupiterConfiguration configuration, DynamicDescendantFilter dynamicDescendantFilter) {
		super(uniqueId, displayName, testClass, templateMethod, configuration);
		this.dynamicDescendantFilter = dynamicDescendantFilter;
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected TestTemplateTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new TestTemplateTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), getDisplayName(),
			getTestClass(), getTestMethod(), this.configuration,
			this.dynamicDescendantFilter.copy(uniqueIdTransformer));
	}

	// --- Filterable ----------------------------------------------------------

	@Override
	public DynamicDescendantFilter getDynamicDescendantFilter() {
		return dynamicDescendantFilter;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		MutableExtensionRegistry registry = populateNewExtensionRegistryFromExtendWithAnnotation(
			context.getExtensionRegistry(), getTestMethod());

		// The test instance should be properly maintained by the enclosing class's ExtensionContext.
		TestInstances testInstances = context.getExtensionContext().getTestInstances().orElse(null);

		ExtensionContext extensionContext = new TestTemplateExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, context.getConfiguration(), registry, testInstances);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(registry)
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {

		new TestTemplateExecutor().execute(context, dynamicTestExecutor);
		return context;
	}

	private class TestTemplateExecutor
			extends TemplateExecutor<TestTemplateInvocationContextProvider, TestTemplateInvocationContext> {

		TestTemplateExecutor() {
			super(TestTemplateTestDescriptor.this, TestTemplateInvocationContextProvider.class);
		}

		@Override
		boolean supports(TestTemplateInvocationContextProvider provider, ExtensionContext extensionContext) {
			return provider.supportsTestTemplate(extensionContext);
		}

		@Override
		protected String getNoRegisteredProviderErrorMessage() {
			return String.format("You must register at least one %s that supports @%s method [%s]",
				TestTemplateInvocationContextProvider.class.getSimpleName(), TestTemplate.class.getSimpleName(),
				getTestMethod());
		}

		@Override
		Stream<? extends TestTemplateInvocationContext> provideContexts(TestTemplateInvocationContextProvider provider,
				ExtensionContext extensionContext) {
			return provider.provideTestTemplateInvocationContexts(extensionContext);
		}

		@Override
		boolean mayReturnZeroContexts(TestTemplateInvocationContextProvider provider,
				ExtensionContext extensionContext) {
			return provider.mayReturnZeroTestTemplateInvocationContexts(extensionContext);
		}

		@Override
		protected String getZeroContextsProvidedErrorMessage(TestTemplateInvocationContextProvider provider) {
			return String.format(
				"Provider [%s] did not provide any invocation contexts, but was expected to do so. "
						+ "You may override mayReturnZeroTestTemplateInvocationContexts() to allow this.",
				provider.getClass().getSimpleName());
		}

		@Override
		UniqueId createInvocationUniqueId(UniqueId parentUniqueId, int index) {
			return parentUniqueId.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		}

		@Override
		TestDescriptor createInvocationTestDescriptor(UniqueId uniqueId,
				TestTemplateInvocationContext invocationContext, int index) {
			return new TestTemplateInvocationTestDescriptor(uniqueId, getTestClass(), getTestMethod(),
				invocationContext, index, TestTemplateTestDescriptor.this.configuration);
		}
	}
}
