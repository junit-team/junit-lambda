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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;

import de.skuzzle.test.snapshots.Snapshot;
import de.skuzzle.test.snapshots.junit5.EnableSnapshotTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.tests.process.OutputFiles;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

/**
 * @since 1.3
 */
@EnableSnapshotTests
class GradleStarterTests {

	@Test
	void gradle_wrapper(@TempDir Path workspace, @FilePrefix("gradle") OutputFiles outputFiles, Snapshot snapshot)
			throws Exception {

		var result = ProcessStarters.gradlew() //
				.workingDir(copyToWorkspace(Projects.JUPITER_STARTER, workspace)) //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("build", "--no-daemon", "--stacktrace", "--no-build-cache", "--warning-mode=fail") //
				.putEnvironment("JDK8", Helper.getJavaHome("8").orElseThrow(TestAbortedException::new).toString()) //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertTrue(result.stdOut().lines().anyMatch(line -> line.contains("BUILD SUCCESSFUL")));
		assertThat(result.stdOut()).contains("Using Java version: 1.8");

		var testResultsDir = workspace.resolve("build/test-results/test");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir, snapshot);
	}
}
