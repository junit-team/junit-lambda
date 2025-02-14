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
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;

import de.skuzzle.test.snapshots.Snapshot;
import de.skuzzle.test.snapshots.junit5.EnableSnapshotTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.tests.process.OutputFiles;
import org.junit.platform.tests.process.ProcessResult;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

/**
 * @since 1.3
 */
@EnableSnapshotTests
class MavenStarterTests {

	@ManagedResource
	LocalMavenRepo localMavenRepo;

	@ManagedResource
	MavenRepoProxy mavenRepoProxy;

	@TempDir
	Path workspace;

	@BeforeEach
	void prepareWorkspace() throws Exception {
		copyToWorkspace(Projects.JUPITER_STARTER, workspace);
	}

	@Test
	void verifyJupiterStarterProject(@FilePrefix("maven") OutputFiles outputFiles, Snapshot snapshot) throws Exception {

		var result = runMaven(outputFiles, "verify");

		assertThat(result.stdOutLines()).contains("[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0");
		assertThat(result.stdOut()).contains("Using Java version: 1.8");

		var testResultsDir = workspace.resolve("target/surefire-reports");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir, snapshot);
	}

	@Test
	void runOnlyOneMethodInContainerTemplate(@FilePrefix("maven") OutputFiles outputFiles) throws Exception {

		var result = runMaven(outputFiles, "test", "-Dtest=CalculatorContainerTemplateTests#regularTest");

		assertThat(result.stdOutLines()) //
				.doesNotContain("CalculatorTests") //
				.contains("[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0");

		result = runMaven(outputFiles, "test", "-Dtest=CalculatorContainerTemplateTests#parameterizedTest");

		assertThat(result.stdOutLines()) //
				.doesNotContain("CalculatorTests") //
				.contains("[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0");
	}

	private ProcessResult runMaven(OutputFiles outputFiles, String... extraArgs) throws InterruptedException {
		var result = ProcessStarters.maven(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.workingDir(workspace) //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Dsnapshot.repo.url=" + mavenRepoProxy.getBaseUri()) //
				.addArguments("--update-snapshots", "--batch-mode") //
				.addArguments(extraArgs).redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr());
		assertThat(result.stdOutLines()).contains("[INFO] BUILD SUCCESS");
		return result;
	}
}
