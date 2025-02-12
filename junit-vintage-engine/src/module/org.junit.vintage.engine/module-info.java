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
 * Provides a {@link org.junit.platform.engine.TestEngine} for running JUnit 3
 * and 4 based tests on the platform.
 *
 * @since 4.12
 * @provides org.junit.platform.engine.TestEngine The {@code VintageTestEngine}
 * runs JUnit 3 and 4 based tests on the platform.
 */
module org.junit.vintage.engine {
	requires junit; // 4
	requires static org.apiguardian.api;
	requires org.junit.platform.engine;

	provides org.junit.platform.engine.TestEngine
			with org.junit.vintage.engine.VintageTestEngine;
}
