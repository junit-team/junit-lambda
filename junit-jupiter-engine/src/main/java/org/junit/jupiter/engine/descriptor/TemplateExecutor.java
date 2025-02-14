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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

abstract class TemplateExecutor<P extends Extension, C> {

	private final TestDescriptor parent;
	private final Class<P> providerType;
	private final DynamicDescendantFilter dynamicDescendantFilter;

	<T extends TestDescriptor & Filterable> TemplateExecutor(T parent, Class<P> providerType) {
		this.parent = parent;
		this.providerType = providerType;
		this.dynamicDescendantFilter = parent.getDynamicDescendantFilter();
	}

	void execute(JupiterEngineExecutionContext context, Node.DynamicTestExecutor dynamicTestExecutor) {
		ExtensionContext extensionContext = context.getExtensionContext();
		List<P> providers = validateProviders(extensionContext, context.getExtensionRegistry());
		AtomicInteger invocationIndex = new AtomicInteger();
		for (P provider : providers) {
			executeForProvider(provider, invocationIndex, dynamicTestExecutor, extensionContext);
		}
	}

	private void executeForProvider(P provider, AtomicInteger invocationIndex,
			Node.DynamicTestExecutor dynamicTestExecutor, ExtensionContext extensionContext) {

		int initialValue = invocationIndex.get();

		try (Stream<? extends C> stream = provideContexts(provider, extensionContext)) {
			stream.forEach(invocationContext -> createInvocationTestDescriptor(invocationContext,
				invocationIndex.incrementAndGet()) //
						.ifPresent(testDescriptor -> execute(dynamicTestExecutor, testDescriptor)));
		}

		Preconditions.condition(
			invocationIndex.get() != initialValue || mayReturnZeroContexts(provider, extensionContext),
			getZeroContextsProvidedErrorMessage(provider));
	}

	private List<P> validateProviders(ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {
		List<P> providers = extensionRegistry.stream(providerType) //
				.filter(provider -> supports(provider, extensionContext)) //
				.collect(toList());
		return Preconditions.notEmpty(providers, this::getNoRegisteredProviderErrorMessage);
	}

	private Optional<TestDescriptor> createInvocationTestDescriptor(C invocationContext, int index) {
		UniqueId invocationUniqueId = createInvocationUniqueId(parent.getUniqueId(), index);
		if (this.dynamicDescendantFilter.test(invocationUniqueId, index - 1)) {
			return Optional.of(createInvocationTestDescriptor(invocationUniqueId, invocationContext, index));
		}
		return Optional.empty();
	}

	private void execute(Node.DynamicTestExecutor dynamicTestExecutor, TestDescriptor testDescriptor) {
		testDescriptor.setParent(parent);
		dynamicTestExecutor.execute(testDescriptor);
	}

	abstract boolean supports(P provider, ExtensionContext extensionContext);

	protected abstract String getNoRegisteredProviderErrorMessage();

	abstract Stream<? extends C> provideContexts(P provider, ExtensionContext extensionContext);

	abstract boolean mayReturnZeroContexts(P provider, ExtensionContext extensionContext);

	protected abstract String getZeroContextsProvidedErrorMessage(P provider);

	abstract UniqueId createInvocationUniqueId(UniqueId parentUniqueId, int index);

	abstract TestDescriptor createInvocationTestDescriptor(UniqueId uniqueId, C invocationContext, int index);

}
