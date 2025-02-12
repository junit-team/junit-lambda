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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.tests.Projects.copyToWorkspace;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.tests.process.OutputFiles;

import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

/**
 * @since 1.4
 */
class MultiReleaseJarTests {

	@ManagedResource
	LocalMavenRepo localMavenRepo;

	@ManagedResource
	MavenRepoProxy mavenRepoProxy;

	@Test
	void checkDefault(@TempDir Path workspace, @FilePrefix("maven") OutputFiles outputFiles) throws Exception {
		var expectedLines = List.of( //
			">> BANNER >>", //
			".", //
			"'-- JUnit Jupiter [OK]", //
			"  +-- ModuleUtilsTests [OK]", //
			"  | +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"  | +-- findAllClassesInModule() [OK]", //
			"  | +-- findAllNonSystemBootModuleNames() [OK]", //
			"  | '-- preconditions() [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- moduleIsNamed() [A] Assumption failed: not running on the module-path", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         3 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         3 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         3 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         7 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         7 tests started         ]", //
			"[         1 tests aborted         ]", //
			"[         6 tests successful      ]", //
			"[         0 tests failed          ]", //
			"" //
		);

		var result = ProcessStarters.maven() //
				.workingDir(copyToWorkspace(Projects.MULTI_RELEASE_JAR, workspace)) //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Dsnapshot.repo.url=" + mavenRepoProxy.getBaseUri()) //
				.addArguments("--update-snapshots", "--show-version", "--errors", "--batch-mode") //
				.addArguments("test") //
				.putEnvironment(MavenEnvVars.forJre(JRE.currentJre())) //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr());

		var outputLines = result.stdOutLines();
		assertTrue(outputLines.contains("[INFO] BUILD SUCCESS"));
		assertFalse(outputLines.contains("[WARNING] "), "Warning marker detected");
		assertFalse(outputLines.contains("[ERROR] "), "Error marker detected");

		var actualLines = Files.readAllLines(workspace.resolve("target/junit-platform/console-launcher.out.log"));
		assertLinesMatch(expectedLines, actualLines);
	}

}
