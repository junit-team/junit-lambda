/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.launcher.core.EngineDiscoveryOrchestrator.Phase.EXECUTION;

import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.EngineDiscoveryOrchestrator;
import org.junit.platform.launcher.core.EngineExecutionOrchestrator;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.launcher.core.ServiceLoaderTestEngineRegistry;

/**
 * {@code EngineTestKit} provides support for executing a test plan for a given
 * {@link TestEngine} and then accessing the results via
 * {@linkplain EngineExecutionResults a fluent API} to verify the expected results.
 *
 * @since 1.4
 * @see #engine(String)
 * @see #engine(TestEngine)
 * @see #execute(String, LauncherDiscoveryRequest)
 * @see #execute(TestEngine, LauncherDiscoveryRequest)
 * @see EngineExecutionResults
 */
@API(status = MAINTAINED, since = "1.7")
public final class EngineTestKit {

	/**
	 * Create an execution {@link Builder} for the {@link TestEngine} with the
	 * supplied ID.
	 *
	 * <p>The {@code TestEngine} will be loaded via Java's {@link ServiceLoader}
	 * mechanism, analogous to the manner in which test engines are loaded in
	 * the JUnit Platform Launcher API.
	 *
	 * <h4>Example Usage</h4>
	 *
	 * <pre class="code">
	 * EngineTestKit
	 *     .engine("junit-jupiter")
	 *     .selectors(selectClass(MyTests.class))
	 *     .execute()
	 *     .testEvents()
	 *     .assertStatistics(stats -&gt; stats.started(2).finished(2));
	 * </pre>
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @return the engine execution {@code Builder}
	 * @throws PreconditionViolationException if the supplied ID is {@code null}
	 * or <em>blank</em>, or if the {@code TestEngine} with the supplied ID
	 * cannot be loaded
	 * @see #engine(TestEngine)
	 * @see #execute(String, LauncherDiscoveryRequest)
	 * @see #execute(TestEngine, LauncherDiscoveryRequest)
	 */
	public static Builder engine(String engineId) {
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");
		return engine(loadTestEngine(engineId.trim()));
	}

	/**
	 * Create an execution {@link Builder} for the supplied {@link TestEngine}.
	 *
	 * <h4>Example Usage</h4>
	 *
	 * <pre class="code">
	 * EngineTestKit
	 *     .engine(new MyTestEngine())
	 *     .selectors(selectClass(MyTests.class))
	 *     .execute()
	 *     .testEvents()
	 *     .assertStatistics(stats -&gt; stats.started(2).finished(2));
	 * </pre>
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @return the engine execution {@code Builder}
	 * @throws PreconditionViolationException if the {@code TestEngine} is
	 * {@code null}
	 * @see #engine(String)
	 * @see #execute(String, LauncherDiscoveryRequest)
	 * @see #execute(TestEngine, LauncherDiscoveryRequest)
	 */
	public static Builder engine(TestEngine testEngine) {
		Preconditions.notNull(testEngine, "TestEngine must not be null");
		return new Builder(testEngine);
	}

	/**
	 * Execute tests for the given {@link EngineDiscoveryRequest} using the
	 * {@link TestEngine} with the supplied ID.
	 *
	 * <p>The {@code TestEngine} will be loaded via Java's {@link ServiceLoader}
	 * mechanism, analogous to the manner in which test engines are loaded in
	 * the JUnit Platform Launcher API.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method. As
	 * an alternative, consider using {@link #engine(String)} for a more fluent
	 * API.
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use
	 * @return the results of the execution
	 * @throws PreconditionViolationException for invalid arguments or if the
	 * {@code TestEngine} with the supplied ID cannot be loaded
	 * @see #execute(String, LauncherDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 * @deprecated Please use {@link #execute(String, LauncherDiscoveryRequest)}
	 * instead.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.7")
	public static EngineExecutionResults execute(String engineId, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");
		return execute(loadTestEngine(engineId.trim()), discoveryRequest);
	}

	/**
	 * Execute tests for the given {@link LauncherDiscoveryRequest} using the
	 * {@link TestEngine} with the supplied ID.
	 *
	 * <p>The {@code TestEngine} will be loaded via Java's {@link ServiceLoader}
	 * mechanism, analogous to the manner in which test engines are loaded in
	 * the JUnit Platform Launcher API.
	 *
	 * <p>{@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * provides a convenient way to build an appropriate discovery request to
	 * supply to this method. As an alternative, consider using
	 * {@link #engine(TestEngine)} for a more fluent API.
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @param discoveryRequest the {@code LauncherDiscoveryRequest} to use
	 * @return the results of the execution
	 * @throws PreconditionViolationException for invalid arguments or if the
	 * {@code TestEngine} with the supplied ID cannot be loaded
	 * @since 1.7
	 * @see #execute(TestEngine, LauncherDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 */
	public static EngineExecutionResults execute(String engineId, LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");
		return execute(loadTestEngine(engineId.trim()), discoveryRequest);
	}

	/**
	 * Execute tests for the given {@link EngineDiscoveryRequest} using the
	 * supplied {@link TestEngine}.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method. As
	 * an alternative, consider using {@link #engine(TestEngine)} for a more fluent
	 * API.
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use; must
	 * not be {@code null}
	 * @return the recorded {@code EngineExecutionResults}
	 * @throws PreconditionViolationException for invalid arguments
	 * @see #execute(TestEngine, LauncherDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 * @deprecated Please use {@link #execute(TestEngine, LauncherDiscoveryRequest)}
	 * instead.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.7")
	public static EngineExecutionResults execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(testEngine, "TestEngine must not be null");
		Preconditions.notNull(discoveryRequest, "EngineDiscoveryRequest must not be null");

		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		executeDirectly(testEngine, discoveryRequest, executionRecorder);
		return executionRecorder.getExecutionResults();
	}

	/**
	 * Execute tests for the given {@link LauncherDiscoveryRequest} using the
	 * supplied {@link TestEngine}.
	 *
	 * <p>{@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * provides a convenient way to build an appropriate discovery request to
	 * supply to this method. As an alternative, consider using
	 * {@link #engine(TestEngine)} for a more fluent API.
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @param discoveryRequest the {@code LauncherDiscoveryRequest} to use; must
	 * not be {@code null}
	 * @return the recorded {@code EngineExecutionResults}
	 * @throws PreconditionViolationException for invalid arguments
	 * @since 1.7
	 * @see #execute(String, LauncherDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 */
	public static EngineExecutionResults execute(TestEngine testEngine, LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(testEngine, "TestEngine must not be null");
		Preconditions.notNull(discoveryRequest, "EngineDiscoveryRequest must not be null");

		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		executeUsingLauncherOrchestration(testEngine, discoveryRequest, executionRecorder);
		return executionRecorder.getExecutionResults();
	}

	private static void executeDirectly(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {
		UniqueId engineUniqueId = UniqueId.forEngine(testEngine.getId());
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest, engineUniqueId);
		ExecutionRequest request = ExecutionRequest.create(engineTestDescriptor, listener,
			discoveryRequest.getConfigurationParameters(), discoveryRequest.getOutputDirectoryProvider());
		testEngine.execute(request);
	}

	private static void executeUsingLauncherOrchestration(TestEngine testEngine,
			LauncherDiscoveryRequest discoveryRequest, EngineExecutionListener listener) {
		LauncherDiscoveryResult discoveryResult = new EngineDiscoveryOrchestrator(singleton(testEngine),
			emptySet()).discover(discoveryRequest, EXECUTION);
		TestDescriptor engineTestDescriptor = discoveryResult.getEngineTestDescriptor(testEngine);
		Preconditions.notNull(engineTestDescriptor, "TestEngine did not yield a TestDescriptor");
		new EngineExecutionOrchestrator().execute(discoveryResult, listener);
	}

	@SuppressWarnings("unchecked")
	private static TestEngine loadTestEngine(String engineId) {
		Iterable<TestEngine> testEngines = new ServiceLoaderTestEngineRegistry().loadTestEngines();
		return ((Stream<TestEngine>) CollectionUtils.toStream(testEngines)) //
				.filter((TestEngine engine) -> engineId.equals(engine.getId()))//
				.findFirst()//
				.orElseThrow(() -> new PreconditionViolationException(
					String.format("Failed to load TestEngine with ID [%s]", engineId)));
	}

	private EngineTestKit() {
		/* no-op */
	}

	// -------------------------------------------------------------------------

	/**
	 * {@link TestEngine} execution builder.
	 *
	 * <p>See {@link EngineTestKit#engine(String)} and
	 * {@link EngineTestKit#engine(TestEngine)} for example usage.
	 *
	 * @since 1.4
	 * @see #selectors(DiscoverySelector...)
	 * @see #filters(Filter...)
	 * @see #configurationParameter(String, String)
	 * @see #configurationParameters(Map)
	 * @see #execute()
	 */
	public static final class Builder {

		private final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request() //
				.enableImplicitConfigurationParameters(false) //
				.outputDirectoryProvider(DisabledOutputDirectoryProvider.INSTANCE);
		private final TestEngine testEngine;

		private Builder(TestEngine testEngine) {
			this.testEngine = testEngine;
		}

		/**
		 * Add all of the supplied {@linkplain DiscoverySelector discovery selectors}.
		 *
		 * <p>Built-in discovery selectors can be created via the static factory
		 * methods in {@link org.junit.platform.engine.discovery.DiscoverySelectors}.
		 *
		 * @param selectors the discovery selectors to add; never {@code null}
		 * @return this builder for method chaining
		 * @see #filters(Filter...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 */
		public Builder selectors(DiscoverySelector... selectors) {
			this.requestBuilder.selectors(selectors);
			return this;
		}

		/**
		 * Add all of the supplied {@linkplain DiscoveryFilter discovery filters}.
		 *
		 * <p>Built-in discovery filters can be created via the static factory
		 * methods in {@link org.junit.platform.engine.discovery.ClassNameFilter}
		 * and {@link org.junit.platform.engine.discovery.PackageNameFilter}.
		 *
		 * @param filters the discovery filters to add; never {@code null}
		 * @return this builder for method chaining
		 * @see #filters(Filter...)
		 * @see #selectors(DiscoverySelector...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 * @deprecated Please use {@link #filters(Filter...)} instead.
		 */
		@Deprecated
		@API(status = DEPRECATED, since = "1.7")
		public Builder filters(DiscoveryFilter<?>... filters) {
			this.requestBuilder.filters(filters);
			return this;
		}

		/**
		 * Add all of the supplied {@linkplain Filter filters}.
		 *
		 * <p>Built-in discovery filters can be created via the static factory
		 * methods in {@link org.junit.platform.engine.discovery.ClassNameFilter}
		 * and {@link org.junit.platform.engine.discovery.PackageNameFilter}.
		 *
		 * <p>Built-in post-discovery filters can be created via the static
		 * factory methods in {@link org.junit.platform.launcher.TagFilter}.
		 *
		 * @param filters the filters to add; never {@code null}
		 * @return this builder for method chaining
		 * @since 1.7
		 * @see #selectors(DiscoverySelector...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 */
		@API(status = STABLE, since = "1.10")
		public Builder filters(Filter<?>... filters) {
			this.requestBuilder.filters(filters);
			return this;
		}

		/**
		 * Add the supplied <em>configuration parameter</em>.
		 *
		 * @param key the configuration parameter key under which to store the
		 * value; never {@code null} or blank
		 * @param value the value to store
		 * @return this builder for method chaining
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(Filter...)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 * @see org.junit.platform.engine.ConfigurationParameters
		 */
		public Builder configurationParameter(String key, String value) {
			this.requestBuilder.configurationParameter(key, value);
			return this;
		}

		/**
		 * Add all of the supplied <em>configuration parameters</em>.
		 *
		 * @param configurationParameters the map of configuration parameters to add;
		 * never {@code null}
		 * @return this builder for method chaining
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(Filter...)
		 * @see #configurationParameter(String, String)
		 * @see #execute()
		 * @see org.junit.platform.engine.ConfigurationParameters
		 */
		public Builder configurationParameters(Map<String, String> configurationParameters) {
			this.requestBuilder.configurationParameters(configurationParameters);
			return this;
		}

		/**
		 * Configure whether implicit configuration parameters should be
		 * considered.
		 *
		 * <p>By default, only configuration parameters that are passed
		 * explicitly to this builder are taken into account. Passing
		 * {@code true} to this method, enables additionally reading
		 * configuration parameters from implicit sources, i.e. system
		 * properties and the {@code junit-platform.properties} classpath
		 * resource.
		 *
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 */
		@API(status = STABLE, since = "1.10")
		public Builder enableImplicitConfigurationParameters(boolean enabled) {
			this.requestBuilder.enableImplicitConfigurationParameters(enabled);
			return this;
		}

		/**
		 * Set the {@link OutputDirectoryProvider} to use.
		 *
		 * <p>If not specified, a default provider will be used that throws an
		 * exception when attempting to create output directories. This is done
		 * to avoid accidentally writing output files to the file system.
		 *
		 * @param outputDirectoryProvider the output directory provider to use;
		 * never {@code null}
		 * @return this builder for method chaining
		 * @since 1.12
		 * @see OutputDirectoryProvider
		 */
		@API(status = EXPERIMENTAL, since = "1.12")
		public Builder outputDirectoryProvider(OutputDirectoryProvider outputDirectoryProvider) {
			this.requestBuilder.outputDirectoryProvider(outputDirectoryProvider);
			return this;
		}

		/**
		 * Execute tests for the configured {@link TestEngine},
		 * {@linkplain DiscoverySelector discovery selectors},
		 * {@linkplain DiscoveryFilter discovery filters}, and
		 * <em>configuration parameters</em>.
		 *
		 * @return the recorded {@code EngineExecutionResults}
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(Filter...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 */
		public EngineExecutionResults execute() {
			LauncherDiscoveryRequest request = this.requestBuilder.build();
			ExecutionRecorder executionRecorder = new ExecutionRecorder();
			EngineTestKit.executeUsingLauncherOrchestration(this.testEngine, request, executionRecorder);
			return executionRecorder.getExecutionResults();
		}

		private static class DisabledOutputDirectoryProvider implements OutputDirectoryProvider {

			public static final OutputDirectoryProvider INSTANCE = new DisabledOutputDirectoryProvider();

			private static final String FAILURE_MESSAGE = "Writing outputs is disabled by default when using EngineTestKit. "
					+ "To enable, configure a custom OutputDirectoryProvider via EngineTestKit#outputDirectoryProvider.";

			private DisabledOutputDirectoryProvider() {
			}

			@Override
			public Path getRootDirectory() {
				throw new JUnitException(FAILURE_MESSAGE);
			}

			@Override
			public Path createOutputDirectory(TestDescriptor testDescriptor) {
				throw new JUnitException(FAILURE_MESSAGE);
			}

		}
	}

}
