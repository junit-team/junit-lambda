/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.PrintWriter;
import java.util.spi.ToolProvider;

import org.apiguardian.api.API;

/**
 * Run the JUnit Platform Console Launcher as a service.
 *
 * @since 1.6
 */
@API(status = STABLE, since = "1.10")
public class ConsoleLauncherToolProvider implements ToolProvider {

	@Override
	public String name() {
		return "junit";
	}

	@Override
	public int run(PrintWriter out, PrintWriter err, String... args) {
		return ConsoleLauncher.run(out, err, args).getExitCode();
	}
}
