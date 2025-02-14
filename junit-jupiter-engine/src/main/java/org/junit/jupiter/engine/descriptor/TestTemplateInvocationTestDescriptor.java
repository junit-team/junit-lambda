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

import static java.util.Collections.emptySet;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.ReflectiveInterceptorCall;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * {@link TestDescriptor} for a {@link org.junit.jupiter.api.TestTemplate @TestTemplate}
 * invocation.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class TestTemplateInvocationTestDescriptor extends TestMethodTestDescriptor {

	public static final String SEGMENT_TYPE = "test-template-invocation";
	private static final ReflectiveInterceptorCall<Method, Void> interceptorCall = ReflectiveInterceptorCall.ofVoidMethod(
		InvocationInterceptor::interceptTestTemplateMethod);

	private TestTemplateInvocationContext invocationContext;
	private final int index;

	TestTemplateInvocationTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method templateMethod,
			TestTemplateInvocationContext invocationContext, int index, JupiterConfiguration configuration) {
		super(uniqueId, invocationContext.getDisplayName(index), testClass, templateMethod, configuration,
			interceptorCall);
		this.invocationContext = invocationContext;
		this.index = index;
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected TestTemplateInvocationTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new TestTemplateInvocationTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), getTestClass(),
			getTestMethod(), this.invocationContext, this.index, this.configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<ExclusiveResource> getExclusiveResources() {
		// Resources are already collected and returned by the enclosing container
		return emptySet();
	}

	@Override
	public String getLegacyReportingName() {
		return super.getLegacyReportingName() + "[" + index + "]";
	}

	@Override
	protected MutableExtensionRegistry populateNewExtensionRegistry(JupiterEngineExecutionContext context) {
		MutableExtensionRegistry registry = super.populateNewExtensionRegistry(context);
		invocationContext.getAdditionalExtensions().forEach(
			extension -> registry.registerExtension(extension, invocationContext));
		return registry;
	}

	@Override
	public void after(JupiterEngineExecutionContext context) {
		// forget invocationContext so it can be garbage collected
		invocationContext = null;
	}

}
