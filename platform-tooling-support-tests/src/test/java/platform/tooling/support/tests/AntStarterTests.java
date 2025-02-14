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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;
import java.util.List;

import de.skuzzle.test.snapshots.Snapshot;
import de.skuzzle.test.snapshots.junit5.EnableSnapshotTests;

import org.apache.tools.ant.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.tests.process.OutputFiles;

import platform.tooling.support.ProcessStarters;

/**
 * @since 1.3
 */
@EnableSnapshotTests
class AntStarterTests {

	@Test
	@Timeout(60)
	void ant_starter(@TempDir Path workspace, @FilePrefix("ant") OutputFiles outputFiles, Snapshot snapshot)
			throws Exception {

		var result = ProcessStarters.java() //
				.workingDir(copyToWorkspace(Projects.JUPITER_STARTER, workspace)) //
				.addArguments("-cp", System.getProperty("antJars"), Main.class.getName()) //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr(), "error log isn't empty");
		assertLinesMatch(List.of(">> HEAD >>", //
			"test.junit.launcher:", //
			">>>>", //
			"\\[junitlauncher\\] Tests run: 6, Failures: 0, Aborted: 0, Skipped: 0, Time elapsed: .+ sec", //
			"\\[junitlauncher\\] Running com.example.project.CalculatorTests", //
			"\\[junitlauncher\\] Tests run: 5, Failures: 0, Aborted: 0, Skipped: 0, Time elapsed: .+ sec", //
			">>>>", //
			"test.console.launcher:", //
			">>>>", //
			"     \\[java\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"     \\[java\\] \\[        11 tests successful      \\]", //
			"     \\[java\\] \\[         0 tests failed          \\]", //
			">> TAIL >>"), //
			result.stdOutLines());

		var testResultsDir = workspace.resolve("build/test-report");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir, snapshot);
	}
}
