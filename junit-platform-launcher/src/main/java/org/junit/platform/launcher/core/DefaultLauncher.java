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

import static java.util.Collections.unmodifiableCollection;
import static org.junit.platform.launcher.core.EngineDiscoveryOrchestrator.Phase.DISCOVERY;
import static org.junit.platform.launcher.core.EngineDiscoveryOrchestrator.Phase.EXECUTION;

import java.util.Collection;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.Namespace;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Default implementation of the {@link Launcher} API.
 *
 * <p>External clients can obtain an instance by invoking
 * {@link LauncherFactory#create()}.
 *
 * @since 1.0
 * @see Launcher
 * @see LauncherFactory
 */
class DefaultLauncher implements Launcher {

	private final LauncherListenerRegistry listenerRegistry = new LauncherListenerRegistry();
	private final EngineExecutionOrchestrator executionOrchestrator = new EngineExecutionOrchestrator(
		listenerRegistry.testExecutionListeners);
	private final EngineDiscoveryOrchestrator discoveryOrchestrator;
	private final LauncherSession launcherSession;
	private final NamespacedHierarchicalStore<Namespace> sessionStore;

	/**
	 * Construct a new {@code DefaultLauncher} with the supplied test engines.
	 *
	 * @param testEngines the test engines to delegate to; never {@code null} or
	 * empty
	 * @param postDiscoveryFilters the additional post discovery filters for
	 * discovery requests; never {@code null}
	 */
	DefaultLauncher(Iterable<TestEngine> testEngines, Collection<PostDiscoveryFilter> postDiscoveryFilters,
			LauncherConfig config) {
		Preconditions.condition(testEngines != null && testEngines.iterator().hasNext(),
			() -> "Cannot create Launcher without at least one TestEngine; "
					+ "consider adding an engine implementation JAR to the classpath");
		Preconditions.notNull(postDiscoveryFilters, "PostDiscoveryFilter array must not be null");
		Preconditions.containsNoNullElements(postDiscoveryFilters,
			"PostDiscoveryFilter array must not contain null elements");
		this.discoveryOrchestrator = new EngineDiscoveryOrchestrator(testEngines,
			unmodifiableCollection(postDiscoveryFilters), listenerRegistry.launcherDiscoveryListeners);
		this.launcherSession = LauncherFactory.openSession(config);
		this.sessionStore = launcherSession.getStore();
	}

	@Override
	public void registerLauncherDiscoveryListeners(LauncherDiscoveryListener... listeners) {
		this.listenerRegistry.launcherDiscoveryListeners.addAll(listeners);
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		this.listenerRegistry.testExecutionListeners.addAll(listeners);
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		return InternalTestPlan.from(discover(discoveryRequest, DISCOVERY));
	}

	@Override
	public void execute(LauncherDiscoveryRequest discoveryRequest, TestExecutionListener... listeners) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		execute(InternalTestPlan.from(discover(discoveryRequest, EXECUTION)), listeners);
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		Preconditions.notNull(testPlan, "TestPlan must not be null");
		Preconditions.condition(testPlan instanceof InternalTestPlan, "TestPlan was not returned by this Launcher");
		Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		execute((InternalTestPlan) testPlan, listeners);
	}

	private LauncherDiscoveryResult discover(LauncherDiscoveryRequest discoveryRequest,
			EngineDiscoveryOrchestrator.Phase phase) {
		return discoveryOrchestrator.discover(discoveryRequest, phase);
	}

	private void execute(InternalTestPlan internalTestPlan, TestExecutionListener[] listeners) {
		executionOrchestrator.execute(internalTestPlan, listeners);
	}

}
