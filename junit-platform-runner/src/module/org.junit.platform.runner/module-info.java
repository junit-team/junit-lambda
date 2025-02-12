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
 * {@code Runner} and annotations for configuring and executing tests on the
 * JUnit Platform in a JUnit 4 environment.
 *
 * @since 1.0
 */
module org.junit.platform.runner {
	requires transitive junit; // 4
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.platform.launcher;
	requires transitive org.junit.platform.suite.api;
	requires org.junit.platform.suite.commons;

	exports org.junit.platform.runner;
}
