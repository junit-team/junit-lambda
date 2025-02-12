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
 * Common support utilities for declarative test suites.
 *
 * @since 1.8
 */
module org.junit.platform.suite.commons {
	requires static transitive org.apiguardian.api;
	requires org.junit.platform.suite.api;
	requires org.junit.platform.commons;
	requires org.junit.platform.engine;
	requires transitive org.junit.platform.launcher;

	exports org.junit.platform.suite.commons to
			org.junit.platform.suite.engine,
			org.junit.platform.runner;
}
