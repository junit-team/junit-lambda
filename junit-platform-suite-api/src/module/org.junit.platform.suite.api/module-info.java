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
 * Annotations for configuring a test suite on the JUnit Platform.
 *
 * @since 1.0
 */
module org.junit.platform.suite.api {
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.platform.commons;

	exports org.junit.platform.suite.api;
}
