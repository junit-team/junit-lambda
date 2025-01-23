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

class VintageMavenIntegrationTests {

	@GlobalResource
	LocalMavenRepo localMavenRepo;

	@TempDir
	Path workspace;

	@Test
	void unsupportedVersion(@FilePrefix("maven") OutputFiles outputFiles) throws Exception {
		var result = run(outputFiles, "4.11");

		assertThat(result.exitCode()).isEqualTo(1);
		assertThat(result.stdOut()) //
				.contains("TestEngine with ID 'junit-vintage' failed to discover tests") //
				.contains("Tests run: 0, Failures: 0, Errors: 0, Skipped: 0");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "4.12", "4.13.2" })
	void supportedVersions(String version, @FilePrefix("maven") OutputFiles outputFiles) throws Exception {
		var result = run(outputFiles, version);

		assertThat(result.exitCode()).isGreaterThan(0);
		assertThat(result.stdOut()) //
				.contains("Running com.example.vintage.VintageTest") //
				.contains("VintageTest.failure:") //
				.contains("Tests run: 2, Failures: 1, Errors: 0, Skipped: 0");

		var surefireReportsDir = workspace.resolve("target/surefire-reports");
		assertThat(surefireReportsDir.resolve("com.example.vintage.VintageTest.txt")).isRegularFile();
		assertThat(surefireReportsDir.resolve("TEST-com.example.vintage.VintageTest.xml")).isRegularFile();
	}

	private ProcessResult run(OutputFiles outputFiles, String version) throws Exception {
		return ProcessStarters.maven(Helper.getJavaHome("17").orElseThrow(TestAbortedException::new)) //
				.workingDir(copyToWorkspace(Projects.VINTAGE, workspace)) //
				.addArguments("clean", "test", "--update-snapshots", "--batch-mode") //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Djunit4Version=" + version) //
				.redirectOutput(outputFiles) //
				.startAndWait();
	}

}
