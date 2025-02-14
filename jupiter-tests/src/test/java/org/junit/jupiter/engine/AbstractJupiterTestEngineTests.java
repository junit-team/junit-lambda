/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.LauncherConstants.STACKTRACE_PRUNING_ENABLED_PROPERTY_NAME;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.OutputDirectoryProviders.dummyOutputDirectoryProvider;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Abstract base class for tests involving the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public abstract class AbstractJupiterTestEngineTests {

	private final JupiterTestEngine engine = new JupiterTestEngine();

	protected EngineExecutionResults executeTestsForClass(Class<?> testClass) {
		return executeTests(selectClass(testClass));
	}

	protected EngineExecutionResults executeTests(DiscoverySelector... selectors) {
		return executeTests(request -> request.selectors(selectors));
	}

	protected EngineExecutionResults executeTests(Consumer<LauncherDiscoveryRequestBuilder> configurer) {
		var builder = defaultRequest();
		configurer.accept(builder);
		return executeTests(builder);
	}

	protected EngineExecutionResults executeTests(LauncherDiscoveryRequestBuilder builder) {
		return executeTests(builder.build());
	}

	protected EngineExecutionResults executeTests(LauncherDiscoveryRequest request) {
		return EngineTestKit.execute(this.engine, request);
	}

	protected TestDescriptor discoverTests(DiscoverySelector... selectors) {
		return discoverTests(defaultRequest().selectors(selectors).build());
	}

	private static LauncherDiscoveryRequestBuilder defaultRequest() {
		return request() //
				.outputDirectoryProvider(dummyOutputDirectoryProvider()) //
				.configurationParameter(STACKTRACE_PRUNING_ENABLED_PROPERTY_NAME, String.valueOf(false));
	}

	protected TestDescriptor discoverTests(LauncherDiscoveryRequest request) {
		return engine.discover(request, UniqueId.forEngine(engine.getId()));
	}

	protected UniqueId discoverUniqueId(Class<?> clazz, String methodName) {
		TestDescriptor engineDescriptor = discoverTests(selectMethod(clazz, methodName));
		Set<? extends TestDescriptor> descendants = engineDescriptor.getDescendants();
		// @formatter:off
		TestDescriptor testDescriptor = descendants.stream()
				.skip(descendants.size() - 1)
				.findFirst()
				.orElseGet(() -> fail("no descendants"));
		// @formatter:on
		return testDescriptor.getUniqueId();
	}

}
