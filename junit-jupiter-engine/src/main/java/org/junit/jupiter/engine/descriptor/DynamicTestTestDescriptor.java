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

import java.util.function.UnaryOperator;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.InvocationInterceptorChain;
import org.junit.jupiter.engine.execution.InvocationInterceptorChain.InterceptorCall;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestTestDescriptor extends DynamicNodeTestDescriptor {

	private static final InvocationInterceptorChain interceptorChain = new InvocationInterceptorChain();

	private DynamicTest dynamicTest;

	DynamicTestTestDescriptor(UniqueId uniqueId, int index, DynamicTest dynamicTest, TestSource source,
			JupiterConfiguration configuration) {
		super(uniqueId, index, dynamicTest, source, configuration);
		this.dynamicTest = dynamicTest;
	}

	@Override
	protected DynamicTestTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new DynamicTestTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), this.index, this.dynamicTest,
			this.getSource().orElse(null), this.configuration);
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) {
		InvocationInterceptor.Invocation<Void> invocation = () -> {
			dynamicTest.getExecutable().execute();
			return null;
		};
		DynamicTestInvocationContext dynamicTestInvocationContext = new DefaultDynamicTestInvocationContext(
			dynamicTest.getExecutable());
		ExtensionContext extensionContext = context.getExtensionContext();
		ExtensionRegistry extensionRegistry = context.getExtensionRegistry();
		interceptorChain.invoke(invocation, extensionRegistry, InterceptorCall.ofVoid(
			(interceptor, wrappedInvocation) -> interceptor.interceptDynamicTest(wrappedInvocation,
				dynamicTestInvocationContext, extensionContext)));
		return context;
	}

	/**
	 * Avoid an {@link OutOfMemoryError} by releasing the reference to this
	 * descriptor's {@link DynamicTest} which holds a reference to the user-supplied
	 * {@link Executable} which may potentially consume large amounts of memory
	 * on the heap.
	 *
	 * @since 5.5
	 * @see <a href="https://github.com/junit-team/junit5/issues/1865">Issue 1865</a>
	 */
	@Override
	public void after(JupiterEngineExecutionContext context) throws Exception {
		super.after(context);
		this.dynamicTest = null;
	}

}
