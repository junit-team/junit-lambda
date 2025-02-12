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
 * Aggregates all JUnit Platform Suite modules.
 *
 * @since 1.8
 */
module org.junit.platform.suite {
	requires transitive org.junit.platform.suite.api;
	requires transitive org.junit.platform.suite.engine;
}
