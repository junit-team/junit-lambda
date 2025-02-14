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
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static platform.tooling.support.ProcessStarters.currentJdkHome;
import static platform.tooling.support.tests.Projects.copyToWorkspace;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.tests.process.OutputFiles;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

/**
 * @since 1.3
 */
class UnalignedClasspathTests {

	@ManagedResource
	LocalMavenRepo localMavenRepo;

	@ManagedResource
	MavenRepoProxy mavenRepoProxy;

	@ParameterizedTest
	@MethodSource("javaVersions")
	@Execution(SAME_THREAD)
	void verifyErrorMessageForUnalignedClasspath(JRE jre, Path javaHome, @TempDir Path workspace,
			@FilePrefix("maven") OutputFiles outputFiles) throws Exception {
		var starter = ProcessStarters.maven(javaHome) //
				.workingDir(copyToWorkspace(Projects.JUPITER_STARTER, workspace)) //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Dsnapshot.repo.url=" + mavenRepoProxy.getBaseUri()) //
				.addArguments("-Djunit.platform.commons.version=1.11.4").addArguments("--update-snapshots",
					"--batch-mode", "verify") //
				.putEnvironment(MavenEnvVars.forJre(jre)) //
				.redirectOutput(outputFiles);
		var result = starter.startAndWait();

		assertEquals(1, result.exitCode());
		assertEquals("", result.stdErr());
		assertThat(result.stdOutLines()).contains("[INFO] BUILD FAILURE");
		assertThat(result.stdOut()) //
				.contains("The wrapped NoClassDefFoundError is likely caused by the versions of JUnit jars "
						+ "on the classpath/module path not being properly aligned");
	}

	static Stream<Arguments> javaVersions() {
		return Stream.concat( //
			Helper.getJavaHome("8").map(path -> Arguments.of(JRE.JAVA_8, path)).stream(), //
			Stream.of(Arguments.of(JRE.currentJre(), currentJdkHome())) //
		);
	}
}
