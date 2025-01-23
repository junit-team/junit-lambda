/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Order(Integer.MAX_VALUE)
class HelperTests {

	@Test
	void loadModuleDirectoryNames() {
		assertLinesMatch(List.of( //
			"junit-jupiter", //
			"junit-jupiter-api", //
			"junit-jupiter-engine", //
			"junit-jupiter-migrationsupport", //
			"junit-jupiter-params", //
			"junit-platform-commons", //
			"junit-platform-console", //
			"junit-platform-engine", //
			"junit-platform-jfr", //
			"junit-platform-launcher", //
			"junit-platform-reporting", //
			"junit-platform-runner", //
			"junit-platform-suite", //
			"junit-platform-suite-api", //
			"junit-platform-suite-commons", //
			"junit-platform-suite-engine", //
			"junit-platform-testkit", //
			"junit-vintage-engine"//
		), Helper.loadModuleDirectoryNames());
	}

	@Test
	void version() {
		assertNotNull(Helper.version("junit-jupiter"));
		assertNotNull(Helper.version("junit-vintage"));
		assertNotNull(Helper.version("junit-platform"));
	}

	@Test
	void nonExistingJdkVersionYieldsAnEmptyOptional() {
		assertEquals(Optional.empty(), Helper.getJavaHome("does not exist"));
	}

	@ParameterizedTest
	@ValueSource(ints = 17)
	void checkJavaHome(int version) {
		var home = Helper.getJavaHome(String.valueOf(version));
		assumeTrue(home.isPresent(), "No 'jdk' element found in Maven toolchain for: " + version);
		assertTrue(Files.isDirectory(home.get()));
	}

}
