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
 * Defines the Test Kit API for the JUnit Platform.
 *
 * @since 1.4
 * @uses org.junit.platform.engine.TestEngine
 */
module org.junit.platform.testkit {
	requires static transitive org.apiguardian.api;
	requires transitive org.assertj.core;
	requires org.junit.platform.commons;
	requires transitive org.junit.platform.engine;
	requires transitive org.junit.platform.launcher;
	requires transitive org.opentest4j;

	// exports org.junit.platform.testkit; empty package
	exports org.junit.platform.testkit.engine;

	uses org.junit.platform.engine.TestEngine;
}
