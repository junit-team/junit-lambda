/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;

/**
 * Represents the result of test discovery of the configured
 * {@linkplain TestEngine test engines}.
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7", consumers = { "org.junit.platform.testkit", "org.junit.platform.suite.engine" })
public class LauncherDiscoveryResult {

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors;
	private final ConfigurationParameters configurationParameters;
	private final OutputDirectoryProvider outputDirectoryProvider;

	LauncherDiscoveryResult(Map<TestEngine, TestDescriptor> testEngineDescriptors,
			ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider) {
		this.testEngineDescriptors = unmodifiableMap(new LinkedHashMap<>(testEngineDescriptors));
		this.configurationParameters = configurationParameters;
		this.outputDirectoryProvider = outputDirectoryProvider;
	}

	public TestDescriptor getEngineTestDescriptor(TestEngine testEngine) {
		return this.testEngineDescriptors.get(testEngine);
	}

	ConfigurationParameters getConfigurationParameters() {
		return this.configurationParameters;
	}

	OutputDirectoryProvider getOutputDirectoryProvider() {
		return this.outputDirectoryProvider;
	}

	public Collection<TestEngine> getTestEngines() {
		return this.testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineTestDescriptors() {
		return this.testEngineDescriptors.values();
	}

	public LauncherDiscoveryResult withRetainedEngines(Predicate<? super TestDescriptor> predicate) {
		Map<TestEngine, TestDescriptor> prunedTestEngineDescriptors = retainEngines(predicate);
		if (prunedTestEngineDescriptors.size() < this.testEngineDescriptors.size()) {
			return new LauncherDiscoveryResult(prunedTestEngineDescriptors, this.configurationParameters,
				this.outputDirectoryProvider);
		}
		return this;
	}

	private Map<TestEngine, TestDescriptor> retainEngines(Predicate<? super TestDescriptor> predicate) {
		// @formatter:off
		return this.testEngineDescriptors.entrySet()
				.stream()
				.filter(entry -> predicate.test(entry.getValue()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		// @formatter:on
	}

}
