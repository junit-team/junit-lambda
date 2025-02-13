/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import org.junit.jupiter.api.Test;

public class StdStreamTest {

	@Test
	void printTest() {
		System.out.print("Writing to STDOUT...");
		System.err.print("Writing to STDERR...");
	}
}
