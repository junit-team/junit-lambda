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

import java.util.List;
import java.util.function.Supplier;

import org.junit.platform.engine.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.8
 */
class SessionPerRequestLauncher implements Launcher {

	private final LauncherListenerRegistry listenerRegistry = new LauncherListenerRegistry();
	private final Supplier<Launcher> launcherSupplier;
	private final Supplier<LauncherSessionListener> sessionListenerSupplier;
	private final Supplier<List<LauncherInterceptor>> interceptorFactory;

	SessionPerRequestLauncher(Supplier<Launcher> launcherSupplier,
			Supplier<LauncherSessionListener> sessionListenerSupplier,
			Supplier<List<LauncherInterceptor>> interceptorFactory) {
		this.launcherSupplier = launcherSupplier;
		this.sessionListenerSupplier = sessionListenerSupplier;
		this.interceptorFactory = interceptorFactory;
	}

	@Override
	public void registerLauncherDiscoveryListeners(LauncherDiscoveryListener... listeners) {
		listenerRegistry.launcherDiscoveryListeners.addAll(listeners);
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		listenerRegistry.testExecutionListeners.addAll(listeners);
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
		try (LauncherSession session = createSession()) {
			return session.getLauncher().discover(launcherDiscoveryRequest);
		}
	}

	@Override
	public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
		try (LauncherSession session = createSession()) {
			session.getLauncher().execute(launcherDiscoveryRequest, listeners);
		}
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		try (LauncherSession session = createSession()) {
			session.getLauncher().execute(testPlan, listeners);
		}
	}

	@Override
	public NamespacedHierarchicalStore<Namespace> getStore(LauncherDiscoveryRequest launcherDiscoveryRequest) {
		try (LauncherSession session = createSession()) {
			return session.getLauncher().getStore(launcherDiscoveryRequest);
		}
	}

	private LauncherSession createSession() {
		LauncherSession session = new DefaultLauncherSession(interceptorFactory.get(), sessionListenerSupplier,
			launcherSupplier);
		Launcher launcher = session.getLauncher();
		listenerRegistry.launcherDiscoveryListeners.getListeners().forEach(
			launcher::registerLauncherDiscoveryListeners);
		listenerRegistry.testExecutionListeners.getListeners().forEach(launcher::registerTestExecutionListeners);
		return session;
	}
}
