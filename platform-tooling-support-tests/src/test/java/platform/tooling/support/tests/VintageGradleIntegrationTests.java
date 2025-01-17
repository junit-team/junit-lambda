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
import static platform.tooling.support.tests.Projects.copyToWorkspace;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.tests.process.OutputFiles;
import org.junit.platform.tests.process.ProcessResult;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

class VintageGradleIntegrationTests {

	@TempDir
	Path workspace;

	@Test
	void unsupportedVersion(@FilePrefix("gradle") OutputFiles outputFiles) throws Exception {
		var result = run(outputFiles, "4.11");

		assertThat(result.exitCode()).isGreaterThan(0);
		assertThat(result.stdOut()) //
				.doesNotContain("STARTED") //
				.contains("Unsupported version of junit:junit: 4.11");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "4.12", "4.13.2" })
	void supportedVersions(String version, @FilePrefix("gradle") OutputFiles outputFiles) throws Exception {
		var result = run(outputFiles, version);

		assertThat(result.exitCode()).isGreaterThan(0);
		assertThat(result.stdOut()) //
				.contains("VintageTest > success PASSED") //
				.contains("VintageTest > failure FAILED");

		var testResultsDir = workspace.resolve("build/test-results/test");
		assertThat(testResultsDir.resolve("TEST-com.example.vintage.VintageTest.xml")).isRegularFile();
	}

	private ProcessResult run(OutputFiles outputFiles, String version) throws Exception {
		return ProcessStarters.gradlew() //
				.workingDir(copyToWorkspace(Projects.VINTAGE, workspace)) //
				.putEnvironment("JDK17", Helper.getJavaHome("17").orElseThrow(TestAbortedException::new).toString()) //
				.addArguments("build", "--no-daemon", "--stacktrace", "--no-build-cache", "--warning-mode=fail") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Djunit4Version=" + version) //
				.redirectOutput(outputFiles) //
				.startAndWait();
	}

}
