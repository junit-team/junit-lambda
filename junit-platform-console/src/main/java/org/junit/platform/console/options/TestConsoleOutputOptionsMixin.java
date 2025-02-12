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

import static org.junit.platform.console.options.TestConsoleOutputOptions.DEFAULT_DETAILS;
import static org.junit.platform.console.options.TestConsoleOutputOptions.DEFAULT_DETAILS_NAME;
import static org.junit.platform.console.options.TestConsoleOutputOptions.DEFAULT_THEME;

import java.nio.file.Path;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

class TestConsoleOutputOptionsMixin {

	@ArgGroup(validate = false, order = 5, heading = "%n@|bold CONSOLE OUTPUT|@%n%n")
	ConsoleOutputOptions consoleOutputOptions = new ConsoleOutputOptions();

	static class ConsoleOutputOptions {

		@Option(names = "--color-palette", paramLabel = "FILE", description = "Specify a path to a properties file to customize ANSI style of output (not supported by all terminals).")
		private Path colorPalette;
		@Option(names = "-color-palette", hidden = true)
		private Path colorPalette2;

		@Option(names = "--single-color", description = "Style test output using only text attributes, no color (not supported by all terminals).")
		private boolean singleColorPalette;
		@Option(names = "-single-color", hidden = true)
		private boolean singleColorPalette2;

		@Option(names = "--details", paramLabel = "MODE", defaultValue = DEFAULT_DETAILS_NAME, description = "Select an output details mode for when tests are executed. " //
				+ "Use one of: ${COMPLETION-CANDIDATES}. If 'none' is selected, " //
				+ "then only the summary and test failures are shown. Default: ${DEFAULT-VALUE}.")
		private final Details details = DEFAULT_DETAILS;

		@Option(names = "-details", hidden = true, defaultValue = DEFAULT_DETAILS_NAME)
		private final Details details2 = DEFAULT_DETAILS;

		@Option(names = "--details-theme", paramLabel = "THEME", description = "Select an output details tree theme for when tests are executed. "
				+ "Use one of: ${COMPLETION-CANDIDATES}. Default is detected based on default character encoding.")
		private final Theme theme = DEFAULT_THEME;

		@Option(names = "-details-theme", hidden = true)
		private final Theme theme2 = DEFAULT_THEME;

		@Option(names = "--redirect-stdout", paramLabel = "FILE", description = "Redirect tests stdout to a file.")
		private Path stdout;

		@Option(names = "--redirect-stderr", paramLabel = "FILE", description = "Redirect tests stderr to a file.")
		private Path stderr;

		private void applyTo(TestConsoleOutputOptions result) {
			result.setColorPalettePath(choose(colorPalette, colorPalette2, null));
			result.setSingleColorPalette(singleColorPalette || singleColorPalette2);
			result.setDetails(choose(details, details2, DEFAULT_DETAILS));
			result.setTheme(choose(theme, theme2, DEFAULT_THEME));
			result.setStdoutPath(stdout);
			result.setStderrPath(stderr);
		}
	}

	TestConsoleOutputOptions toTestConsoleOutputOptions() {
		TestConsoleOutputOptions result = new TestConsoleOutputOptions();
		if (this.consoleOutputOptions != null) {
			this.consoleOutputOptions.applyTo(result);
		}
		return result;
	}

	private static <T> T choose(T left, T right, T defaultValue) {
		return left == right ? left : (left == defaultValue ? right : left);
	}
}
