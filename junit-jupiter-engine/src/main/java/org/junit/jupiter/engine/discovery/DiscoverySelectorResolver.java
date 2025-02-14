/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver.InitializationContext;

/**
 * {@code DiscoverySelectorResolver} resolves {@link TestDescriptor TestDescriptors}
 * for containers and tests selected by {@link org.junit.platform.engine.DiscoverySelector
 * DiscoverySelectors}, with the help of an {@link EngineDiscoveryRequestResolver}.
 *
 * <p>This is an internal utility which is only {@code public} in order to provide
 * the {@link org.junit.jupiter.engine.JupiterTestEngine JupiterTestEngine} access
 * to the functionality of the {@code discovery} package.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class DiscoverySelectorResolver {

	private static final EngineDiscoveryRequestResolver<JupiterEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<JupiterEngineDescriptor> builder() //
			.addClassContainerSelectorResolver(new IsTestClassWithTests()) //
			.addSelectorResolver(ctx -> new ClassSelectorResolver(ctx.getClassNameFilter(), getConfiguration(ctx))) //
			.addSelectorResolver(ctx -> new MethodSelectorResolver(getConfiguration(ctx))) //
			.addTestDescriptorVisitor(ctx -> new ClassOrderingVisitor(getConfiguration(ctx))) //
			.addTestDescriptorVisitor(ctx -> new MethodOrderingVisitor(getConfiguration(ctx))) //
			.addTestDescriptorVisitor(ctx -> descriptor -> {
				if (descriptor instanceof JupiterTestDescriptor) {
					((JupiterTestDescriptor) descriptor).prunePriorToFiltering();
				}
			}) //
			.build();

	private static JupiterConfiguration getConfiguration(InitializationContext<JupiterEngineDescriptor> context) {
		return context.getEngineDescriptor().getConfiguration();
	}

	public void resolveSelectors(EngineDiscoveryRequest request, JupiterEngineDescriptor engineDescriptor) {
		resolver.resolve(request, engineDescriptor);
	}

}
