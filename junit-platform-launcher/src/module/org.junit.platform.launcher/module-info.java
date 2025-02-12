/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Public API for configuring and launching test plans.
 *
 * <p>This API is typically used by IDEs and build tools.
 *
 * @since 1.0
 * @uses org.junit.platform.engine.TestEngine
 * @uses org.junit.platform.launcher.LauncherDiscoveryListener
 * @uses org.junit.platform.launcher.LauncherInterceptor
 * @uses org.junit.platform.launcher.LauncherSessionListener
 * @uses org.junit.platform.launcher.PostDiscoveryFilter
 * @uses org.junit.platform.launcher.TestExecutionListener
 */
module org.junit.platform.launcher {
	requires transitive java.logging;
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.platform.commons;
	requires transitive org.junit.platform.engine;

	exports org.junit.platform.launcher;
	exports org.junit.platform.launcher.core;
	exports org.junit.platform.launcher.listeners;
	exports org.junit.platform.launcher.listeners.discovery;

	uses org.junit.platform.engine.TestEngine;
	uses org.junit.platform.launcher.LauncherDiscoveryListener;
	uses org.junit.platform.launcher.LauncherInterceptor;
	uses org.junit.platform.launcher.LauncherSessionListener;
	uses org.junit.platform.launcher.PostDiscoveryFilter;
	uses org.junit.platform.launcher.TestExecutionListener;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junit.platform.launcher.listeners.UniqueIdTrackingListener;
}
