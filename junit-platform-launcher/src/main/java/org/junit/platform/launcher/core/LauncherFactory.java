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

import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.launcher.LauncherConstants.DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME;
import static org.junit.platform.launcher.LauncherConstants.ENABLE_LAUNCHER_INTERCEPTORS;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ClassNamePatternFilterUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Factory for creating {@link Launcher} instances by invoking {@link #create()}
 * or {@link #create(LauncherConfig)}.
 *
 * <p>By default, test engines are discovered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} mechanism. For that purpose, a
 * text file named {@code META-INF/services/org.junit.platform.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.platform.engine.TestEngine}
 * interface is declared.
 *
 * <p>By default, test execution listeners are discovered at runtime via the
 * {@link java.util.ServiceLoader ServiceLoader} mechanism and are
 * automatically registered with the {@link Launcher} created by this factory.
 * Users may register additional listeners using the
 * {@link Launcher#registerTestExecutionListeners(TestExecutionListener...)}
 * method on the created launcher instance.
 *
 * <p>For full control over automatic registration and programmatic registration
 * of test engines and listeners, supply an instance of {@link LauncherConfig}
 * to {@link #create(LauncherConfig)}.
 *
 * @since 1.0
 * @see Launcher
 * @see LauncherConfig
 */
@API(status = STABLE, since = "1.0")
public class LauncherFactory {

	private LauncherFactory() {
		/* no-op */
	}

	/**
	 * Factory method for opening a new {@link LauncherSession} using the
	 * {@linkplain LauncherConfig#DEFAULT default} {@link LauncherConfig}.
	 *
	 * @throws PreconditionViolationException if no test engines are detected
	 * @since 1.8
	 * @see #openSession(LauncherConfig)
	 */
	@API(status = STABLE, since = "1.10")
	public static LauncherSession openSession() throws PreconditionViolationException {
		return openSession(LauncherConfig.DEFAULT);
	}

	/**
	 * Factory method for opening a new {@link LauncherSession} using the
	 * supplied {@link LauncherConfig}.
	 *
	 * @param config the configuration for the session and the launcher; never
	 * {@code null}
	 * @throws PreconditionViolationException if the supplied configuration is
	 * {@code null}, or if no test engines are detected
	 * @since 1.8
	 * @see #openSession()
	 */
	@API(status = STABLE, since = "1.10")
	public static LauncherSession openSession(LauncherConfig config) throws PreconditionViolationException {
		Preconditions.notNull(config, "LauncherConfig must not be null");
		LauncherConfigurationParameters configurationParameters = LauncherConfigurationParameters.builder().build();
		return new DefaultLauncherSession(collectLauncherInterceptors(configurationParameters),
			() -> createLauncherSessionListener(config),
			launcherFactory -> createDefaultLauncher(config, configurationParameters));
	}

	/**
	 * Factory method for creating a new {@link Launcher} using the
	 * {@linkplain LauncherConfig#DEFAULT default} {@link LauncherConfig}.
	 *
	 * @throws PreconditionViolationException if no test engines are detected
	 * @see #create(LauncherConfig)
	 */
	public static Launcher create() throws PreconditionViolationException {
		return create(LauncherConfig.DEFAULT);
	}

	/**
	 * Factory method for creating a new {@link Launcher} using the supplied
	 * {@link LauncherConfig}.
	 *
	 * @param config the configuration for the launcher; never {@code null}
	 * @throws PreconditionViolationException if the supplied configuration is
	 * {@code null}, or if no test engines are detected
	 * registered
	 * @since 1.3
	 * @see #create()
	 */
	@API(status = STABLE, since = "1.10")
	public static Launcher create(LauncherConfig config) throws PreconditionViolationException {
		Preconditions.notNull(config, "LauncherConfig must not be null");
		LauncherConfigurationParameters configurationParameters = LauncherConfigurationParameters.builder().build();
		return new SessionPerRequestLauncher(() -> createDefaultLauncher(config, configurationParameters),
			() -> createLauncherSessionListener(config), () -> collectLauncherInterceptors(configurationParameters));
	}

	private static DefaultLauncher createDefaultLauncher(LauncherConfig config,
			LauncherConfigurationParameters configurationParameters) {
		Set<TestEngine> engines = collectTestEngines(config);
		List<PostDiscoveryFilter> filters = collectPostDiscoveryFilters(config);

		DefaultLauncher launcher = new DefaultLauncher(engines, filters, new NamespacedHierarchicalStore<>(null));

		registerLauncherDiscoveryListeners(config, launcher);
		registerTestExecutionListeners(config, launcher, configurationParameters);

		return launcher;
	}

	private static List<LauncherInterceptor> collectLauncherInterceptors(
			LauncherConfigurationParameters configurationParameters) {
		List<LauncherInterceptor> interceptors = new ArrayList<>();
		if (configurationParameters.getBoolean(ENABLE_LAUNCHER_INTERCEPTORS).orElse(false)) {
			ServiceLoaderRegistry.load(LauncherInterceptor.class).forEach(interceptors::add);
		}
		interceptors.add(ClasspathAlignmentCheckingLauncherInterceptor.INSTANCE);
		return interceptors;
	}

	private static Set<TestEngine> collectTestEngines(LauncherConfig config) {
		Set<TestEngine> engines = new LinkedHashSet<>();
		if (config.isTestEngineAutoRegistrationEnabled()) {
			new ServiceLoaderTestEngineRegistry().loadTestEngines().forEach(engines::add);
		}
		engines.addAll(config.getAdditionalTestEngines());
		return engines;
	}

	private static LauncherSessionListener createLauncherSessionListener(LauncherConfig config) {
		ListenerRegistry<LauncherSessionListener> listenerRegistry = ListenerRegistry.forLauncherSessionListeners();
		if (config.isLauncherSessionListenerAutoRegistrationEnabled()) {
			ServiceLoaderRegistry.load(LauncherSessionListener.class).forEach(listenerRegistry::add);
		}
		config.getAdditionalLauncherSessionListeners().forEach(listenerRegistry::add);
		return listenerRegistry.getCompositeListener();
	}

	private static List<PostDiscoveryFilter> collectPostDiscoveryFilters(LauncherConfig config) {
		List<PostDiscoveryFilter> filters = new ArrayList<>();
		if (config.isPostDiscoveryFilterAutoRegistrationEnabled()) {
			ServiceLoaderRegistry.load(PostDiscoveryFilter.class).forEach(filters::add);
		}
		filters.addAll(config.getAdditionalPostDiscoveryFilters());
		return filters;
	}

	private static void registerLauncherDiscoveryListeners(LauncherConfig config, Launcher launcher) {
		if (config.isLauncherDiscoveryListenerAutoRegistrationEnabled()) {
			ServiceLoaderRegistry.load(LauncherDiscoveryListener.class).forEach(
				launcher::registerLauncherDiscoveryListeners);
		}
		config.getAdditionalLauncherDiscoveryListeners().forEach(launcher::registerLauncherDiscoveryListeners);
	}

	private static void registerTestExecutionListeners(LauncherConfig config, Launcher launcher,
			LauncherConfigurationParameters configurationParameters) {
		if (config.isTestExecutionListenerAutoRegistrationEnabled()) {
			loadAndFilterTestExecutionListeners(configurationParameters).forEach(
				launcher::registerTestExecutionListeners);
		}
		config.getAdditionalTestExecutionListeners().forEach(launcher::registerTestExecutionListeners);
	}

	private static Iterable<TestExecutionListener> loadAndFilterTestExecutionListeners(
			ConfigurationParameters configurationParameters) {
		Predicate<String> classNameFilter = configurationParameters.get(DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME) //
				.map(ClassNamePatternFilterUtils::excludeMatchingClassNames) //
				.orElse(__ -> true);
		return ServiceLoaderRegistry.load(TestExecutionListener.class, classNameFilter);
	}

}
