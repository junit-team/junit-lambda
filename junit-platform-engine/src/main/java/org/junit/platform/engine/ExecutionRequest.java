/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Provides a single {@link TestEngine} access to the information necessary to
 * execute its tests.
 *
 * <p>A request contains an engine's root {@link TestDescriptor}, the
 * {@link EngineExecutionListener} to be notified of test execution events, the
 * {@link ConfigurationParameters} that the engine may use to influence test
 * execution, and an {@link OutputDirectoryProvider} for writing reports and
 * other output files.
 *
 * @since 1.0
 * @see TestEngine
 */
@API(status = STABLE, since = "1.0")
public class ExecutionRequest {

	private final TestDescriptor rootTestDescriptor;
	private final EngineExecutionListener engineExecutionListener;
	private final ConfigurationParameters configurationParameters;
	private final OutputDirectoryProvider outputDirectoryProvider;
	// TODO[#4252] get store from constructor
	private final NamespacedHierarchicalStore<Namespace> store = new NamespacedHierarchicalStore<>(null);

	@Deprecated
	@API(status = DEPRECATED, since = "1.11")
	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this(rootTestDescriptor, engineExecutionListener, configurationParameters, null);
	}

	private ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider) {
		this.rootTestDescriptor = Preconditions.notNull(rootTestDescriptor, "rootTestDescriptor must not be null");
		this.engineExecutionListener = Preconditions.notNull(engineExecutionListener,
			"engineExecutionListener must not be null");
		this.configurationParameters = Preconditions.notNull(configurationParameters,
			"configurationParameters must not be null");
		this.outputDirectoryProvider = outputDirectoryProvider;
	}

	/**
	 * Factory for creating an execution request.
	 *
	 * @param rootTestDescriptor the engine's root {@link TestDescriptor}
	 * @param engineExecutionListener the {@link EngineExecutionListener} to be
	 * notified of test execution events
	 * @param configurationParameters {@link ConfigurationParameters} that the
	 * engine may use to influence test execution
	 * @return a new {@code ExecutionRequest}; never {@code null}
	 * @since 1.9
	 * @deprecated Use {@link #create(TestDescriptor, EngineExecutionListener, ConfigurationParameters, OutputDirectoryProvider)}
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.11")
	public static ExecutionRequest create(TestDescriptor rootTestDescriptor,
			EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters) {
		return new ExecutionRequest(rootTestDescriptor, engineExecutionListener, configurationParameters);
	}

	/**
	 * Factory for creating an execution request.
	 *
	 * @param rootTestDescriptor the engine's root {@link TestDescriptor}; never
	 * {@code null}
	 * @param engineExecutionListener the {@link EngineExecutionListener} to be
	 * notified of test execution events; never {@code null}
	 * @param configurationParameters {@link ConfigurationParameters} that the
	 * engine may use to influence test execution; never {@code null}
	 * @param outputDirectoryProvider {@link OutputDirectoryProvider} for
	 * writing reports and other output files; never {@code null}
	 * @return a new {@code ExecutionRequest}; never {@code null}
	 * @since 1.12
	 */
	@API(status = INTERNAL, since = "1.12")
	public static ExecutionRequest create(TestDescriptor rootTestDescriptor,
			EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters,
			OutputDirectoryProvider outputDirectoryProvider) {

		return new ExecutionRequest(rootTestDescriptor, engineExecutionListener, configurationParameters,
			Preconditions.notNull(outputDirectoryProvider, "outputDirectoryProvider must not be null"));
	}

	/**
	 * {@return the root {@link TestDescriptor} of the engine that processes this
	 * request}
	 *
	 * <p><strong>Note</strong>: the <em>root</em> descriptor is the
	 * {@code TestDescriptor} returned by
	 * {@link TestEngine#discover(EngineDiscoveryRequest, UniqueId)}.
	 */
	public TestDescriptor getRootTestDescriptor() {
		return this.rootTestDescriptor;
	}

	/**
	 * {@return the {@link EngineExecutionListener} to be notified of test execution
	 * events}
	 */
	public EngineExecutionListener getEngineExecutionListener() {
		return this.engineExecutionListener;
	}

	/**
	 * {@return the {@link ConfigurationParameters} that the engine may use to
	 * influence test execution}
	 */
	public ConfigurationParameters getConfigurationParameters() {
		return this.configurationParameters;
	}

	/**
	 * {@return the {@link OutputDirectoryProvider} for this request for writing
	 * reports and other output files}
	 *
	 * @throws PreconditionViolationException if the output directory provider
	 * is not available
	 * @since 1.12
	 */
	@API(status = EXPERIMENTAL, since = "1.12")
	public OutputDirectoryProvider getOutputDirectoryProvider() {
		return Preconditions.notNull(outputDirectoryProvider,
				"No OutputDirectoryProvider was configured for this request");
	}

	public NamespacedHierarchicalStore<Namespace> getStore() {
		return this.store;
	}

}
