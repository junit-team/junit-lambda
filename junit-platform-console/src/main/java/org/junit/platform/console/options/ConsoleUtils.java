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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.Console;
import java.nio.charset.Charset;

import org.apiguardian.api.API;

/**
 * Collection of utilities for working with {@code java.io.Console}
 * and friends.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.9
 */
@API(status = INTERNAL, since = "1.9")
public class ConsoleUtils {

	/**
	 * {@return the charset of the console}
	 */
	public static Charset charset() {
		Console console = System.console();
		return console != null ? console.charset() : Charset.defaultCharset();
	}
}
