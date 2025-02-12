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
 * Defines the JUnit Jupiter API for writing tests.
 *
 * @since 5.0
 */
module org.junit.jupiter.api {
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.platform.commons;
	requires transitive org.opentest4j;

	exports org.junit.jupiter.api;
	exports org.junit.jupiter.api.condition;
	exports org.junit.jupiter.api.extension;
	exports org.junit.jupiter.api.extension.support;
	exports org.junit.jupiter.api.function;
	exports org.junit.jupiter.api.io;
	exports org.junit.jupiter.api.parallel;

	opens org.junit.jupiter.api.condition to org.junit.platform.commons;
}
