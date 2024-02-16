/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine.testcases;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @since 1.10.2
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ConfigurationSensitiveTestCase {

	boolean shared;

	@Test
	void test1() {
		shared = true;
	}

	@Test
	void test2() {
		assertTrue(shared);
	}

}
