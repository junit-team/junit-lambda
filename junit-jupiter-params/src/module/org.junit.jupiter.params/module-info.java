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
 * JUnit Jupiter extension for parameterized tests.
 *
 * @since 5.0
 */
module org.junit.jupiter.params {
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.jupiter.api;
	requires transitive org.junit.platform.commons;

	exports org.junit.jupiter.params;
	exports org.junit.jupiter.params.aggregator;
	exports org.junit.jupiter.params.converter;
	exports org.junit.jupiter.params.provider;
	exports org.junit.jupiter.params.support;

	opens org.junit.jupiter.params to org.junit.platform.commons;
	opens org.junit.jupiter.params.converter to org.junit.platform.commons;
	opens org.junit.jupiter.params.provider to org.junit.platform.commons;
}
