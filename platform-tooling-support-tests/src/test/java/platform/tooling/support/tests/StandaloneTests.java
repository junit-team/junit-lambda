/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.Projects.getSourceDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.platform.tests.process.OutputFiles;
import org.junit.platform.tests.process.ProcessResult;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;
import platform.tooling.support.ThirdPartyJars;

/**
 * @since 1.4
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(CONCURRENT)
class StandaloneTests {

	@TempDir
	static Path workspace;

	@BeforeAll
	static void prepareWorkspace() throws IOException {
		copyToWorkspace(Projects.STANDALONE, workspace);
	}

	@Test
	void jarFileWithoutCompiledModuleDescriptorClass() throws Exception {
		var jar = MavenRepo.jar("junit-platform-console-standalone");
		var name = "module-info.class";
		var found = new ArrayList<Path>();
		try (var fileSystem = FileSystems.newFileSystem(jar)) {
			for (var rootDirectory : fileSystem.getRootDirectories()) {
				try (var stream = Files.walk(rootDirectory)) {
					stream.filter(path -> path.getNameCount() > 0) // skip root entry
							.filter(path -> path.getFileName().toString().equals(name)).forEach(found::add);
				}
			}
		}
		assertTrue(found.isEmpty(), jar + " must not contain any " + name + " files: " + found);
	}

	@Test
	void listAllObservableEngines(@FilePrefix("java") OutputFiles outputFiles) throws Exception {
		var result = ProcessStarters.java() //
				.workingDir(getSourceDirectory(Projects.STANDALONE)) //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("engines", "--disable-ansi-colors", "--disable-banner") //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var suiteVersion = Helper.version("junit-platform-suite-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertLinesMatch("""
				junit-jupiter (org.junit.jupiter:junit-jupiter-engine:%s)
				junit-platform-suite (org.junit.platform:junit-platform-suite-engine:%s)
				junit-vintage (org.junit.vintage:junit-vintage-engine:%s)
				""".formatted(jupiterVersion, suiteVersion, vintageVersion).lines(), //
			result.stdOut().lines());
	}

	@Test
	void printVersionViaJar(@FilePrefix("java") OutputFiles outputFiles) throws Exception {
		var result = ProcessStarters.java() //
				.workingDir(getSourceDirectory(Projects.STANDALONE)) //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("--version", "--disable-ansi-colors") //
				.putEnvironment("CLICOLOR_FORCE", "1") // enable ANSI colors by default (see https://picocli.info/#_heuristics_for_enabling_ansi)
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());

		var version = Helper.version("junit-platform-console");
		assertLinesMatch("""
				JUnit Platform Console Launcher %s
				JVM: .*
				OS: .*
				""".formatted(version).lines(), //
			result.stdOut().lines());
	}

	@Test
	void printVersionViaModule(@FilePrefix("java") OutputFiles outputFiles) throws Exception {
		var junitJars = Stream.of("junit-platform-console", "junit-platform-reporting", "junit-platform-engine",
			"junit-platform-launcher", "junit-platform-commons") //
				.map(MavenRepo::jar);
		var thirdPartyJars = Stream.of( //
			ThirdPartyJars.find("org.opentest4j", "opentest4j"), //
			ThirdPartyJars.find("org.opentest4j.reporting", "open-test-reporting-tooling-spi") //
		);
		var modulePath = Stream.concat(junitJars, thirdPartyJars) //
				.map(String::valueOf) //
				.collect(joining(File.pathSeparator));
		var result = ProcessStarters.java() //
				.workingDir(getSourceDirectory(Projects.STANDALONE)) //
				.addArguments("--module-path", modulePath) //
				.addArguments("--module", "org.junit.platform.console") //
				.addArguments("--version", "--disable-ansi-colors") //
				.putEnvironment("CLICOLOR_FORCE", "1") // enable ANSI colors by default (see https://picocli.info/#_heuristics_for_enabling_ansi)
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());

		var version = Helper.version("junit-platform-console");
		assertLinesMatch("""
				JUnit Platform Console Launcher %s
				JVM: .*
				OS: .*
				""".formatted(version).lines(), //
			result.stdOut().lines());
	}

	@Test
	@Order(1)
	@Execution(SAME_THREAD)
	void compile(@FilePrefix("javac") OutputFiles javacOutputFiles, @FilePrefix("jar") OutputFiles jarOutputFiles)
			throws Exception {
		var result = ProcessStarters.javaCommand("javac") //
				.workingDir(workspace) //
				.addArguments("-Xlint:-options") //
				.addArguments("--release", "17") //
				.addArguments("-proc:none") //
				.addArguments("-d", workspace.resolve("bin").toString()) //
				.addArguments("--class-path", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments(workspace.resolve("src/standalone/JupiterIntegration.java").toString()) //
				.addArguments(workspace.resolve("src/standalone/JupiterParamsIntegration.java").toString()) //
				.addArguments(workspace.resolve("src/standalone/SuiteIntegration.java").toString()) //
				.addArguments(workspace.resolve("src/standalone/VintageIntegration.java").toString()) //
				.redirectOutput(javacOutputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertTrue(result.stdOut().isEmpty());
		assertTrue(result.stdErr().isEmpty());

		// create "tests.jar" that'll be picked-up by "testWithJarredTestClasses()" later
		var jarFolder = Files.createDirectories(workspace.resolve("jar"));
		var jarResult = ProcessStarters.javaCommand("jar") //
				.workingDir(workspace) //
				.addArguments("--create") //
				.addArguments("--file", jarFolder.resolve("tests.jar").toString()) //
				.addArguments("-C", workspace.resolve("bin").toString(), ".") //
				.redirectOutput(jarOutputFiles) //
				.startAndWait();
		assertEquals(0, jarResult.exitCode());
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverTree(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details-theme=ascii");

		var expected = """
				.
				+-- JUnit Platform Suite
				| '-- SuiteIntegration
				|   '-- JUnit Jupiter
				|     '-- SuiteIntegration$SingleTestContainer
				|       '-- successful()
				+-- JUnit Jupiter
				| +-- JupiterIntegration
				| | +-- successful()
				| | +-- fail()
				| | +-- abort()
				| | '-- disabled()
				| +-- SuiteIntegration$SingleTestContainer
				| | '-- successful()
				| '-- JupiterParamsIntegration
				|   '-- parameterizedTest(String)
				'-- JUnit Vintage
				  '-- VintageIntegration
				    +-- f4il
				    +-- ignored
				    '-- succ3ssful

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.stdOut().lines());
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverFlat(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details=flat");

		var expected = """
				JUnit Platform Suite ([engine:junit-platform-suite])
				SuiteIntegration ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration])
				JUnit Jupiter ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter])
				SuiteIntegration$SingleTestContainer ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer])
				successful() ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()])
				JUnit Jupiter ([engine:junit-jupiter])
				JupiterIntegration ([engine:junit-jupiter]/[class:standalone.JupiterIntegration])
				successful() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:successful()])
				fail() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:fail()])
				abort() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:abort()])
				disabled() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:disabled()])
				SuiteIntegration$SingleTestContainer ([engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer])
				successful() ([engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()])
				JupiterParamsIntegration ([engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration])
				parameterizedTest(String) ([engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]/[test-template:parameterizedTest(java.lang.String)])
				JUnit Vintage ([engine:junit-vintage])
				VintageIntegration ([engine:junit-vintage]/[runner:standalone.VintageIntegration])
				f4il ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:f4il(standalone.VintageIntegration)])
				ignored ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:ignored(standalone.VintageIntegration)])
				succ3ssful ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:succ3ssful(standalone.VintageIntegration)])

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.stdOut().lines());
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverVerbose(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details=verbose", "--details-theme=ascii");

		var expected = """
				+-- JUnit Platform Suite
				| +-- SuiteIntegration
				| | +-- JUnit Jupiter
				| | | +-- SuiteIntegration$SingleTestContainer
				| | | | +-- successful()
				| | | | |      tags: []
				| | | | |  uniqueId: [engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()]
				| | | | |    parent: [engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]
				| | | | |    source: MethodSource [className = 'standalone.SuiteIntegration$SingleTestContainer', methodName = 'successful', methodParameterTypes = '']
				| | | '-- SuiteIntegration$SingleTestContainer
				| | '-- JUnit Jupiter
				| '-- SuiteIntegration
				'-- JUnit Platform Suite
				+-- JUnit Jupiter
				| +-- JupiterIntegration
				| | +-- successful()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:successful()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'successful', methodParameterTypes = '']
				| | +-- fail()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:fail()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'fail', methodParameterTypes = '']
				| | +-- abort()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:abort()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'abort', methodParameterTypes = '']
				| | +-- disabled()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:disabled()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'disabled', methodParameterTypes = '']
				| '-- JupiterIntegration
				| +-- SuiteIntegration$SingleTestContainer
				| | +-- successful()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]
				| | |    source: MethodSource [className = 'standalone.SuiteIntegration$SingleTestContainer', methodName = 'successful', methodParameterTypes = '']
				| '-- SuiteIntegration$SingleTestContainer
				| +-- JupiterParamsIntegration
				| | +-- parameterizedTest(String)
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]/[test-template:parameterizedTest(java.lang.String)]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterParamsIntegration', methodName = 'parameterizedTest', methodParameterTypes = 'java.lang.String']
				| '-- JupiterParamsIntegration
				'-- JUnit Jupiter
				+-- JUnit Vintage
				| +-- VintageIntegration
				| | +-- f4il
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:f4il(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'f4il', methodParameterTypes = '']
				| | +-- ignored
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:ignored(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'ignored', methodParameterTypes = '']
				| | +-- succ3ssful
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:succ3ssful(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'succ3ssful', methodParameterTypes = '']
				| '-- VintageIntegration
				'-- JUnit Vintage

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.stdOut().lines());
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverNone(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details=none");

		assertThat(result.stdOut()).isEmpty();
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverSummary(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details=summary");

		var expected = """

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.stdOut().lines());
	}

	@Test
	@Order(2)
	@Execution(SAME_THREAD)
	void discoverTestFeed(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = discover(outputFiles, "--details=testfeed");
		var expected = """
				JUnit Platform Suite > SuiteIntegration > JUnit Jupiter > SuiteIntegration$SingleTestContainer > successful()
				JUnit Jupiter > JupiterIntegration > successful()
				JUnit Jupiter > JupiterIntegration > fail()
				JUnit Jupiter > JupiterIntegration > abort()
				JUnit Jupiter > JupiterIntegration > disabled()
				JUnit Jupiter > SuiteIntegration$SingleTestContainer > successful()
				JUnit Vintage > VintageIntegration > f4il
				JUnit Vintage > VintageIntegration > ignored
				JUnit Vintage > VintageIntegration > succ3ssful

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();

		assertLinesMatch(expected.lines(), result.stdOut().lines());
	}

	private static ProcessResult discover(OutputFiles outputFiles, String... args) throws Exception {
		var result = ProcessStarters.java() //
				.workingDir(workspace) //
				.putEnvironment("NO_COLOR", "1") // --disable-ansi-colors
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("discover") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin") //
				.addArguments(args) //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		return result;
	}

	@Test
	@Order(3)
	@Execution(SAME_THREAD)
	void execute(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var result = ProcessStarters.java() //
				.workingDir(workspace) //
				.putEnvironment("NO_COLOR", "1") // --disable-ansi-colors
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin") //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(1, result.exitCode());

		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = Files.readAllLines(workspace.resolve("expected-err.txt"));
		assertLinesMatch(expectedOutLines, result.stdOutLines());
		var actualErrLines = result.stdErrLines();
		if (actualErrLines.getFirst().contains("stty: /dev/tty: No such device or address")) {
			// Happens intermittently on GitHub Actions on Windows
			actualErrLines = new ArrayList<>(actualErrLines);
			actualErrLines.removeFirst();
		}
		assertLinesMatch(expectedErrLines, actualErrLines);

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.stdErr().contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.stdErr().contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	@Test
	@Order(4)
	@Execution(SAME_THREAD)
	void executeOnJava17(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var javaHome = Helper.getJavaHome("17").orElseThrow(TestAbortedException::new);
		var result = ProcessStarters.java(javaHome) //
				.workingDir(workspace) //
				.addArguments("-showversion") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin") //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(1, result.exitCode());

		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = getExpectedErrLinesOnJava17(workspace);
		assertLinesMatch(expectedOutLines, result.stdOutLines());
		assertLinesMatch(expectedErrLines, result.stdErrLines());

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.stdErr().contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.stdErr().contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	@Test
	@Order(5)
	@Execution(SAME_THREAD)
	// https://github.com/junit-team/junit5/issues/2600
	void executeOnJava17SelectPackage(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var javaHome = Helper.getJavaHome("17").orElseThrow(TestAbortedException::new);
		var result = ProcessStarters.java(javaHome) //
				.workingDir(workspace).addArguments("-showversion") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone").toString()) //
				.addArguments("execute") //
				.addArguments("--select-package", Projects.STANDALONE) //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin") //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(1, result.exitCode());

		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = getExpectedErrLinesOnJava17(workspace);
		assertLinesMatch(expectedOutLines, result.stdOutLines());
		assertLinesMatch(expectedErrLines, result.stdErrLines());

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.stdErr().contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.stdErr().contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	private static List<String> getExpectedErrLinesOnJava17(Path workspace) throws IOException {
		var expectedErrLines = new ArrayList<String>();
		expectedErrLines.add(">> JAVA VERSION >>");
		expectedErrLines.addAll(Files.readAllLines(workspace.resolve("expected-err.txt")));
		return expectedErrLines;
	}

	@Test
	@Order(6)
	@Execution(SAME_THREAD)
	@Disabled("https://github.com/junit-team/junit5/issues/1724")
	void executeWithJarredTestClasses(@FilePrefix("console-launcher") OutputFiles outputFiles) throws Exception {
		var jar = MavenRepo.jar("junit-platform-console-standalone");
		var path = new ArrayList<String>();
		// path.add("bin"); // "exploded" test classes are found, see also test() above
		path.add(workspace.resolve("standalone/jar/tests.jar").toAbsolutePath().toString());
		path.add(jar.toString());
		var result = ProcessStarters.java() //
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("--class-path", String.join(File.pathSeparator, path)) //
				.addArguments("org.junit.platform.console.ConsoleLauncher") //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--fail-if-no-tests") //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(1, result.exitCode());
	}
}
