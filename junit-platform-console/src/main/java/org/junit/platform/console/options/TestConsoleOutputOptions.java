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

import java.nio.file.Path;
import java.util.Locale;

import org.apiguardian.api.API;

/**
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class TestConsoleOutputOptions {

	static final String DEFAULT_DETAILS_NAME = "tree";
	static final Details DEFAULT_DETAILS = Details.valueOf(DEFAULT_DETAILS_NAME.toUpperCase(Locale.ROOT));
	static final Theme DEFAULT_THEME = Theme.valueOf(ConsoleUtils.charset());

	private boolean ansiColorOutputDisabled;
	private Path colorPalettePath;
	private boolean isSingleColorPalette;
	private Details details = DEFAULT_DETAILS;
	private Theme theme = DEFAULT_THEME;
	private Path stdoutPath;
	private Path stderrPath;

	public boolean isAnsiColorOutputDisabled() {
		return this.ansiColorOutputDisabled;
	}

	public void setAnsiColorOutputDisabled(boolean ansiColorOutputDisabled) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
	}

	public Path getColorPalettePath() {
		return colorPalettePath;
	}

	public void setColorPalettePath(Path colorPalettePath) {
		this.colorPalettePath = colorPalettePath;
	}

	public boolean isSingleColorPalette() {
		return isSingleColorPalette;
	}

	public void setSingleColorPalette(boolean singleColorPalette) {
		this.isSingleColorPalette = singleColorPalette;
	}

	public Details getDetails() {
		return this.details;
	}

	public void setDetails(Details details) {
		this.details = details;
	}

	public Theme getTheme() {
		return this.theme;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	@API(status = INTERNAL, since = "5.12")
	public Path getStdoutPath() {
		return this.stdoutPath;
	}

	@API(status = INTERNAL, since = "5.12")
	public void setStdoutPath(Path stdoutPath) {
		this.stdoutPath = stdoutPath;
	}

	@API(status = INTERNAL, since = "5.12")
	public Path getStderrPath() {
		return this.stderrPath;
	}

	@API(status = INTERNAL, since = "5.12")
	public void setStderrPath(Path stderrPath) {
		this.stderrPath = stderrPath;
	}

}
