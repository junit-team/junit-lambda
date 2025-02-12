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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectIteration;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModule;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;

/**
 * @since 1.10
 */
class CommandLineOptionsParsingTests {

	@Test
	void parseNoArguments() {
		String[] noArguments = {};
		var options = parse(noArguments);

		// @formatter:off
		assertAll(
			() -> assertFalse(options.output.isAnsiColorOutputDisabled()),
			() -> assertNull(options.output.getStdoutPath()),
			() -> assertNull(options.output.getStderrPath()),
			() -> assertEquals(TestConsoleOutputOptions.DEFAULT_DETAILS, options.output.getDetails()),
			() -> assertFalse(options.discovery.isScanClasspath()),
			() -> assertEquals(List.of(STANDARD_INCLUDE_PATTERN), options.discovery.getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(), options.discovery.getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(), options.discovery.getIncludedPackages()),
			() -> assertEquals(List.of(), options.discovery.getExcludedPackages()),
			() -> assertEquals(List.of(), options.discovery.getIncludedMethodNamePatterns()),
			() -> assertEquals(List.of(), options.discovery.getExcludedMethodNamePatterns()),
			() -> assertEquals(List.of(), options.discovery.getIncludedTagExpressions()),
			() -> assertEquals(List.of(), options.discovery.getExcludedTagExpressions()),
			() -> assertEquals(List.of(), options.discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(), options.discovery.getSelectedUris()),
			() -> assertEquals(List.of(), options.discovery.getSelectedFiles()),
			() -> assertEquals(List.of(), options.discovery.getSelectedDirectories()),
			() -> assertEquals(List.of(), options.discovery.getSelectedModules()),
			() -> assertEquals(List.of(), options.discovery.getSelectedPackages()),
			() -> assertEquals(List.of(), options.discovery.getSelectedMethods()),
			() -> assertEquals(List.of(), options.discovery.getSelectedClasspathEntries()),
			() -> assertEquals(Map.of(), options.discovery.getConfigurationParameters())
		);
		// @formatter:on
	}

	@Test
	void parseSwitches() {
		// @formatter:off
		assertAll(
			() -> assertTrue(parse("--disable-ansi-colors").output.isAnsiColorOutputDisabled(), "disable ansi"),
			() -> assertTrue(parse("--scan-class-path").discovery.isScanClasspath(), "scan class path")
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidDetails(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(Details.VERBOSE, type.parseArgLine("--details verbose").output.getDetails()),
			() -> assertEquals(Details.TREE, type.parseArgLine("--details tree").output.getDetails()),
			() -> assertEquals(Details.FLAT, type.parseArgLine("--details flat").output.getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details NONE").output.getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details none").output.getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details None").output.getDetails())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetails() {
		assertOptionWithMissingRequiredArgumentThrowsException("--details");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidDetailsTheme(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(Theme.ASCII, type.parseArgLine("--details-theme ascii").output.getTheme()),
			() -> assertEquals(Theme.ASCII, type.parseArgLine("--details-theme ASCII").output.getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme unicode").output.getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme UNICODE").output.getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme uniCode").output.getTheme())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetailsTheme() {
		assertOptionWithMissingRequiredArgumentThrowsException("--details-theme");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludeClassNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("-n .*Test").discovery.getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test", ".*Tests"), type.parseArgLine("--include-classname .*Test --include-classname .*Tests").discovery.getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("--include-classname=.*Test").discovery.getIncludedClassNamePatterns())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludeClassNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("-N .*Test").discovery.getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test", ".*Tests"), type.parseArgLine("--exclude-classname .*Test --exclude-classname .*Tests").discovery.getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("--exclude-classname=.*Test").discovery.getExcludedClassNamePatterns())
		);
		// @formatter:on
	}

	@Test
	void usesDefaultClassNamePatternWithoutExplicitArgument() throws Exception {
		assertEquals(List.of(STANDARD_INCLUDE_PATTERN),
			ArgsType.args.parseArgLine("").discovery.getIncludedClassNamePatterns());
	}

	@Test
	void parseInvalidIncludeClassNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("-n", "--include-classname");
	}

	@Test
	void parseInvalidExcludeClassNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("-N", "--exclude-classname");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedPackages(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("org.junit.included"),
						type.parseArgLine("--include-package org.junit.included").discovery.getIncludedPackages()),
				() -> assertEquals(List.of("org.junit.included"),
						type.parseArgLine("--include-package=org.junit.included").discovery.getIncludedPackages()),
				() -> assertEquals(List.of("org.junit.included1", "org.junit.included2"),
						type.parseArgLine("--include-package org.junit.included1 --include-package org.junit.included2").discovery.getIncludedPackages())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedPackages(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("org.junit.excluded"),
						type.parseArgLine("--exclude-package org.junit.excluded").discovery.getExcludedPackages()),
				() -> assertEquals(List.of("org.junit.excluded"),
						type.parseArgLine("--exclude-package=org.junit.excluded").discovery.getExcludedPackages()),
				() -> assertEquals(List.of("org.junit.excluded1", "org.junit.excluded2"),
						type.parseArgLine("--exclude-package org.junit.excluded1 --exclude-package org.junit.excluded2").discovery.getExcludedPackages())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludeMethodNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(".+#method.*"),
						type.parseArgLine("--include-methodname .+#method.*").discovery.getIncludedMethodNamePatterns()),
				() -> assertEquals(List.of(".+#methodA.*", ".+#methodB.*"),
						type.parseArgLine("--include-methodname .+#methodA.* --include-methodname .+#methodB.*").discovery.getIncludedMethodNamePatterns()),
				() -> assertEquals(List.of(".+#method.*"),
						type.parseArgLine("--include-methodname=.+#method.*").discovery.getIncludedMethodNamePatterns())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludeMethodNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(".+#method.*"),
						type.parseArgLine("--exclude-methodname .+#method.*").discovery.getExcludedMethodNamePatterns()),
				() -> assertEquals(List.of(".+#methodA.*", ".+#methodB.*"),
						type.parseArgLine("--exclude-methodname .+#methodA.* --exclude-methodname .+#methodB.*").discovery.getExcludedMethodNamePatterns()),
				() -> assertEquals(List.of(".+#method.*"),
						type.parseArgLine("--exclude-methodname=.+#method.*").discovery.getExcludedMethodNamePatterns())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludeMethodNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("--include-methodname");
	}

	@Test
	void parseInvalidExcludeMethodNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("--exclude-methodname");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedTags(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("fast"), type.parseArgLine("-t fast").discovery.getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--include-tag fast").discovery.getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--include-tag=fast").discovery.getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast", "slow"), type.parseArgLine("-t fast -t slow").discovery.getIncludedTagExpressions())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-t", "--include-tag");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedTags(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("fast"), type.parseArgLine("-T fast").discovery.getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--exclude-tag fast").discovery.getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--exclude-tag=fast").discovery.getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast", "slow"), type.parseArgLine("-T fast -T slow").discovery.getExcludedTagExpressions())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-T", "--exclude-tag");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedEngines(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("junit-jupiter"), type.parseArgLine("-e junit-jupiter").discovery.getIncludedEngines()),
			() -> assertEquals(List.of("junit-vintage"), type.parseArgLine("--include-engine junit-vintage").discovery.getIncludedEngines()),
			() -> assertEquals(List.of(), type.parseArgLine("").discovery.getIncludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedEngines() {
		assertOptionWithMissingRequiredArgumentThrowsException("-e", "--include-engine");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedEngines(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("junit-jupiter"), type.parseArgLine("-E junit-jupiter").discovery.getExcludedEngines()),
			() -> assertEquals(List.of("junit-vintage"), type.parseArgLine("--exclude-engine junit-vintage").discovery.getExcludedEngines()),
			() -> assertEquals(List.of(), type.parseArgLine("").discovery.getExcludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedEngines() {
		assertOptionWithMissingRequiredArgumentThrowsException("-E", "--exclude-engine");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidAdditionalClasspathEntries(ArgsType type) {
		var dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(dir), type.parseArgLine("-cp .").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--cp .").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-classpath .").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-classpath=.").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--classpath .").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--classpath=.").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--class-path .").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--class-path=.").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir, Path.of("lib/some.jar")), type.parseArgLine("-cp . -cp lib/some.jar").discovery.getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir, Path.of("lib/some.jar")), type.parseArgLine("-cp ." + File.pathSeparator + "lib/some.jar").discovery.getAdditionalClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void parseValidAndAbsoluteAdditionalClasspathEntries() throws Exception {
		ArgsType type = ArgsType.args;
		assertEquals(List.of(Path.of("C:\\a.jar")),
			type.parseArgLine("-cp C:\\a.jar").discovery.getAdditionalClasspathEntries());
		assertEquals(List.of(Path.of("C:\\foo.jar"), Path.of("D:\\bar.jar")),
			type.parseArgLine("-cp C:\\foo.jar;D:\\bar.jar").discovery.getAdditionalClasspathEntries());
	}

	@Test
	void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithMissingRequiredArgumentThrowsException("-cp", "--classpath", "--class-path");
	}

	@Test
	void parseInvalidXmlReportsDirs() {
		assertOptionWithMissingRequiredArgumentThrowsException("--reports-dir");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidUriSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("-u file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("--u file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("-select-uri file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("-select-uri=file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("--select-uri file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt")), type.parseArgLine("--select-uri=file:///foo.txt").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt"), selectUri("https://example")), type.parseArgLine("-u file:///foo.txt -u https://example").discovery.getSelectedUris()),
				() -> assertEquals(List.of(selectUri("file:///foo.txt"), selectUri("https://example")), type.parseArgLine("-u file:///foo.txt https://example").discovery.getSelectedUris())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidUriSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-u", "--select-uri", "-u unknown-scheme:");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidFileSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("-f foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("--f foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("-select-file foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("-select-file=foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("--select-file foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt")), type.parseArgLine("--select-file=foo.txt").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt"), selectFile("bar.csv")), type.parseArgLine("-f foo.txt -f bar.csv").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt"), selectFile("bar.csv")), type.parseArgLine("-f foo.txt bar.csv").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt", FilePosition.from(5))), type.parseArgLine("-f foo.txt?line=5").discovery.getSelectedFiles()),
				() -> assertEquals(List.of(selectFile("foo.txt", FilePosition.from(12, 34))), type.parseArgLine("-f foo.txt?line=12&column=34").discovery.getSelectedFiles())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidFileSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-f", "--select-file");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidDirectorySelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("-d foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("--d foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("-select-directory foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("-select-directory=foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("--select-directory foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), type.parseArgLine("--select-directory=foo/bar").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar"), selectDirectory("bar/qux")), type.parseArgLine("-d foo/bar -d bar/qux").discovery.getSelectedDirectories()),
				() -> assertEquals(List.of(selectDirectory("foo/bar"), selectDirectory("bar/qux")), type.parseArgLine("-d foo/bar bar/qux").discovery.getSelectedDirectories())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDirectorySelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-d", "--select-directory");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidModuleSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("-o com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("--o com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("-select-module com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("-select-module=com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("--select-module com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), type.parseArgLine("--select-module=com.acme.foo").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo"), selectModule("com.example.bar")), type.parseArgLine("-o com.acme.foo -o com.example.bar").discovery.getSelectedModules()),
				() -> assertEquals(List.of(selectModule("com.acme.foo"), selectModule("com.example.bar")), type.parseArgLine("-o com.acme.foo com.example.bar").discovery.getSelectedModules())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidModuleSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-o", "--select-module");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidPackageSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("-p com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("--p com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("-select-package com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("-select-package=com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("--select-package com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), type.parseArgLine("--select-package=com.acme.foo").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo"), selectPackage("com.example.bar")), type.parseArgLine("-p com.acme.foo -p com.example.bar").discovery.getSelectedPackages()),
				() -> assertEquals(List.of(selectPackage("com.acme.foo"), selectPackage("com.example.bar")), type.parseArgLine("-p com.acme.foo com.example.bar").discovery.getSelectedPackages())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidPackageSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-p", "--select-package");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidClassSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("-c com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("--c com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("-select-class com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("-select-class=com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("--select-class com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), type.parseArgLine("--select-class=com.acme.Foo").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo"), selectClass("com.example.Bar")), type.parseArgLine("-c com.acme.Foo -c com.example.Bar").discovery.getSelectedClasses()),
				() -> assertEquals(List.of(selectClass("com.acme.Foo"), selectClass("com.example.Bar")), type.parseArgLine("-c com.acme.Foo com.example.Bar").discovery.getSelectedClasses())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClassSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-c", "--select-class");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidMethodSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("-m com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("--m com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("-select-method com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("-select-method=com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("--select-method com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), type.parseArgLine("--select-method=com.acme.Foo#m()").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()"), selectMethod("com.example.Bar#method(java.lang.Object)")),
						type.parseArgLine("-m com.acme.Foo#m() -m com.example.Bar#method(java.lang.Object)").discovery.getSelectedMethods()),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()"), selectMethod("com.example.Bar#method(java.lang.Object)")),
						type.parseArgLine("-m com.acme.Foo#m() com.example.Bar#method(java.lang.Object)").discovery.getSelectedMethods())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidMethodSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-m", "--select-method");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidClasspathResourceSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("-r /foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("--r /foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("-select-resource /foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("-select-resource=/foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("--select-resource /foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), type.parseArgLine("--select-resource=/foo.csv").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv"), selectClasspathResource("bar.json")), type.parseArgLine("-r /foo.csv -r bar.json").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv"), selectClasspathResource("bar.json")), type.parseArgLine("-r /foo.csv bar.json").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv", FilePosition.from(5))), type.parseArgLine("-r /foo.csv?line=5").discovery.getSelectedClasspathResources()),
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv", FilePosition.from(12, 34))), type.parseArgLine("-r /foo.csv?line=12&column=34").discovery.getSelectedClasspathResources())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClasspathResourceSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-r", "--select-resource");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIterationSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectIteration(selectClasspathResource("/foo.csv"), 0)), type.parseArgLine("-i resource:/foo.csv[0]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectMethod("com.acme.Foo#m()"), 1, 2)), type.parseArgLine("--i method:com.acme.Foo#m()[1..2]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectClass("com.acme.Foo"), 0, 2)), type.parseArgLine("-select-iteration class:com.acme.Foo[0,2]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectPackage("com.acme.foo"), 3)), type.parseArgLine("-select-iteration=package:com.acme.foo[3]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectModule("com.acme.foo"), 0, 1, 2, 4, 5, 6)), type.parseArgLine("--select-iteration module:com.acme.foo[0..2,4..6]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectDirectory("foo/bar"), 1, 5)), type.parseArgLine("--select-iteration=directory:foo/bar[1,5]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectFile("foo.txt"), 6), selectIteration(selectUri("file:///foo.txt"), 7)), type.parseArgLine("-i file:foo.txt[6] -i uri:file:///foo.txt[7]").discovery.getSelectedIterations()),
				() -> assertEquals(List.of(selectIteration(selectFile("foo.txt"), 6), selectIteration(selectUri("file:///foo.txt"), 7)), type.parseArgLine("-i file:foo.txt[6] uri:file:///foo.txt[7]").discovery.getSelectedIterations())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIterationSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-i", "--select-iteration");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidUniqueIdSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectUniqueId("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]")), type.parseArgLine("--uid [engine:junit-jupiter]/[class:MyClass]/[method:myMethod]").discovery.getSelectedUniqueIds()),
				() -> assertEquals(List.of(selectUniqueId("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]")), type.parseArgLine("--select-unique-id [engine:junit-jupiter]/[class:MyClass]/[method:myMethod]").discovery.getSelectedUniqueIds()),
				() -> assertEquals(List.of(selectUniqueId("[engine:junit-jupiter]/[class:MyClass1]"), selectUniqueId("[engine:junit-jupiter]/[class:MyClass2]")), type.parseArgLine("--uid [engine:junit-jupiter]/[class:MyClass1] --uid [engine:junit-jupiter]/[class:MyClass2]").discovery.getSelectedUniqueIds()),
				() -> assertEquals(List.of(selectUniqueId("[engine:junit-jupiter]/[class:MyClass1]"), selectUniqueId("[engine:junit-jupiter]/[class:MyClass2]")), type.parseArgLine("--uid [engine:junit-jupiter]/[class:MyClass1] [engine:junit-jupiter]/[class:MyClass2]").discovery.getSelectedUniqueIds())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidUniqueIdSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("--uid", "--select-unique-id");
	}

	@ParameterizedTest
	@EnumSource
	void parseClasspathScanningEntries(ArgsType type) {
		var dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertTrue(type.parseArgLine("--scan-class-path").discovery.isScanClasspath()),
			() -> assertEquals(List.of(), type.parseArgLine("--scan-class-path").discovery.getSelectedClasspathEntries()),
			() -> assertTrue(type.parseArgLine("--scan-classpath").discovery.isScanClasspath()),
			() -> assertEquals(List.of(), type.parseArgLine("--scan-classpath").discovery.getSelectedClasspathEntries()),
			() -> assertTrue(type.parseArgLine("--scan-class-path .").discovery.isScanClasspath()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--scan-class-path .").discovery.getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--scan-class-path=.").discovery.getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-scan-class-path .").discovery.getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-scan-class-path=.").discovery.getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src/test/java")), type.parseArgLine("--scan-class-path . --scan-class-path src/test/java").discovery.getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src/test/java")), type.parseArgLine("--scan-class-path ." + File.pathSeparator + "src/test/java").discovery.getSelectedClasspathEntries())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidConfigurationParameters(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertThat(type.parseArgLine("-config foo=bar").discovery.getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("-config=foo=bar").discovery.getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config foo=bar").discovery.getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config=foo=bar").discovery.getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config foo=bar --config baz=qux").discovery.getConfigurationParameters())
						.containsExactly(entry("foo", "bar"), entry("baz", "qux"))
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidConfigurationParametersResource(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertThat(type.parseArgLine("--config-resource foo.properties").discovery.getConfigurationParametersResources())
						.containsOnly("foo.properties"),
				() -> assertThat(type.parseArgLine("--config-resource foo.properties --config-resource bar.properties").discovery.getConfigurationParametersResources())
						.containsExactly("foo.properties", "bar.properties")
		);
		// @formatter:on
	}

	@Test
	void parseInvalidConfigurationParameters() {
		assertOptionWithMissingRequiredArgumentThrowsException("-config", "--config");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidStdoutRedirectionFile(ArgsType type) {
		var file = Paths.get("foo.txt");
		// @formatter:off
		assertAll(
				() -> assertNull(type.parseArgLine("").output.getStdoutPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stdout=foo.txt").output.getStdoutPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stdout foo.txt").output.getStdoutPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stdout bar.txt --redirect-stdout foo.txt").output.getStdoutPath())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidStdoutRedirectionFile() {
		assertOptionWithMissingRequiredArgumentThrowsException("--redirect-stdout");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidStderrRedirectionFile(ArgsType type) {
		var file = Paths.get("foo.txt");
		// @formatter:off
		assertAll(
				() -> assertNull(type.parseArgLine("").output.getStderrPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stderr=foo.txt").output.getStderrPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stderr foo.txt").output.getStderrPath()),
				() -> assertEquals(file, type.parseArgLine("--redirect-stderr bar.txt --redirect-stderr foo.txt").output.getStderrPath())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidStderrRedirectionFile() {
		assertOptionWithMissingRequiredArgumentThrowsException("--redirect-stderr");
	}

	@Test
	void parseInvalidConfigurationParametersResource() {
		assertOptionWithMissingRequiredArgumentThrowsException("--config-resource");
	}

	@ParameterizedTest
	@EnumSource
	void parseInvalidConfigurationParametersWithDuplicateKey(ArgsType type) {
		Exception e = assertThrows(Exception.class, () -> type.parseArgLine("--config foo=bar --config foo=baz"));

		assertThat(e.getMessage()).isEqualTo("Duplicate key 'foo' for values 'bar' and 'baz'.");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidSelectorIdentifier(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(selectClasspathResource("/foo.csv")), parseIdentifiers(type,"--select resource:/foo.csv")),
				() -> assertEquals(List.of(selectMethod("com.acme.Foo#m()")), parseIdentifiers(type,"--select method:com.acme.Foo#m()")),
				() -> assertEquals(List.of(selectClass("com.acme.Foo")), parseIdentifiers(type,"--select class:com.acme.Foo")),
				() -> assertEquals(List.of(selectPackage("com.acme.foo")), parseIdentifiers(type,"--select package:com.acme.foo")),
				() -> assertEquals(List.of(selectModule("com.acme.foo")), parseIdentifiers(type,"--select module:com.acme.foo")),
				() -> assertEquals(List.of(selectDirectory("foo/bar")), parseIdentifiers(type,"--select directory:foo/bar")),
				() -> assertEquals(List.of(selectFile("foo.txt"), selectUri("file:///foo.txt")), parseIdentifiers(type,"--select file:foo.txt --select uri:file:///foo.txt")),
				() -> assertEquals(List.of(selectUniqueId("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]")), parseIdentifiers(type,"--select uid:[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]"))
		);
		// @formatter:on
	}

	private static List<? extends DiscoverySelector> parseIdentifiers(ArgsType type, String argLine)
			throws IOException {
		return DiscoverySelectors.parseAll(type.parseArgLine(argLine).discovery.getSelectorIdentifiers()).toList();
	}

	@Test
	void parseInvalidSelectorIdentifier() {
		assertOptionWithMissingRequiredArgumentThrowsException("--select");
	}

	private void assertOptionWithMissingRequiredArgumentThrowsException(String... options) {
		assertAll(
			Stream.of(options).map(opt -> () -> assertThrows(Exception.class, () -> ArgsType.args.parseArgLine(opt))));
	}

	enum ArgsType {
		args {
			@Override
			Result parseArgLine(String argLine) {
				return parse(split(argLine));
			}
		},
		atFile {
			@Override
			Result parseArgLine(String argLine) throws IOException {
				var atFile = Files.createTempFile("junit-launcher-args", ".txt");
				try {
					Files.write(atFile, List.of(split(argLine)));
					return parse("@" + atFile);
				}
				finally {
					Files.deleteIfExists(atFile);
				}
			}
		};

		abstract Result parseArgLine(String argLine) throws IOException;

		private static String[] split(String argLine) {
			return "".equals(argLine) ? new String[0] : argLine.split("\\s+");
		}
	}

	private static Result parse(String... args) {
		ExecuteTestsCommand command = new ExecuteTestsCommand((__, ___) -> null);
		command.parseArgs(args);
		return new Result(command.toTestDiscoveryOptions(), command.toTestConsoleOutputOptions());
	}

	record Result(TestDiscoveryOptions discovery, TestConsoleOutputOptions output) {
	}

}
