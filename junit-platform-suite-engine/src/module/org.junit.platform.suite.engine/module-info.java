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
 * Provides a {@link org.junit.platform.engine.TestEngine} for running
 * declarative test suites.
 *
 * @since 1.8
 * @provides org.junit.platform.engine.TestEngine
 */
module org.junit.platform.suite.engine {
	requires static org.apiguardian.api;
	requires org.junit.platform.suite.api;
	requires org.junit.platform.suite.commons;
	requires org.junit.platform.commons;
	requires org.junit.platform.engine;
	requires org.junit.platform.launcher;

	provides org.junit.platform.engine.TestEngine
			with org.junit.platform.suite.engine.SuiteTestEngine;
}
