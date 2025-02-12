/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;

class ConditionalTestExecutionDemo {

	// tag::user_guide_os[]
	@Test
	@EnabledOnOs(MAC)
	void onlyOnMacOs() {
		// ...
	}

	@TestOnMac
	void testOnMac() {
		// ...
	}

	@Test
	@EnabledOnOs({ LINUX, MAC })
	void onLinuxOrMac() {
		// ...
	}

	@Test
	@DisabledOnOs(WINDOWS)
	void notOnWindows() {
		// ...
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Test
	@EnabledOnOs(MAC)
	@interface TestOnMac {
	}
	// end::user_guide_os[]

	// tag::user_guide_architecture[]
	@Test
	@EnabledOnOs(architectures = "aarch64")
	void onAarch64() {
		// ...
	}

	@Test
	@DisabledOnOs(architectures = "x86_64")
	void notOnX86_64() {
		// ...
	}

	@Test
	@EnabledOnOs(value = MAC, architectures = "aarch64")
	void onNewMacs() {
		// ...
	}

	@Test
	@DisabledOnOs(value = MAC, architectures = "aarch64")
	void notOnNewMacs() {
		// ...
	}
	// end::user_guide_architecture[]

	// tag::user_guide_jre[]
	@Test
	@EnabledOnJre(JAVA_17)
	void onlyOnJava17() {
		// ...
	}

	@Test
	@EnabledOnJre({ JAVA_17, JAVA_21 })
	void onJava17And21() {
		// ...
	}

	@Test
	@EnabledForJreRange(min = JAVA_9, max = JAVA_11)
	void fromJava9To11() {
		// ...
	}

	@Test
	@EnabledForJreRange(min = JAVA_9)
	void onJava9AndHigher() {
		// ...
	}

	@Test
	@EnabledForJreRange(max = JAVA_11)
	void fromJava8To11() {
		// ...
	}

	@Test
	@DisabledOnJre(JAVA_9)
	void notOnJava9() {
		// ...
	}

	@Test
	@DisabledForJreRange(min = JAVA_9, max = JAVA_11)
	void notFromJava9To11() {
		// ...
	}

	@Test
	@DisabledForJreRange(min = JAVA_9)
	void notOnJava9AndHigher() {
		// ...
	}

	@Test
	@DisabledForJreRange(max = JAVA_11)
	void notFromJava8To11() {
		// ...
	}
	// end::user_guide_jre[]

	// tag::user_guide_jre_arbitrary_versions[]
	@Test
	@EnabledOnJre(versions = 26)
	void onlyOnJava26() {
		// ...
	}

	@Test
	@EnabledOnJre(versions = { 25, 26 })
	// Can also be expressed as follows.
	// @EnabledOnJre(value = JAVA_25, versions = 26)
	void onJava25And26() {
		// ...
	}

	@Test
	@EnabledForJreRange(minVersion = 26)
	void onJava26AndHigher() {
		// ...
	}

	@Test
	@EnabledForJreRange(minVersion = 25, maxVersion = 27)
	// Can also be expressed as follows.
	// @EnabledForJreRange(min = JAVA_25, maxVersion = 27)
	void fromJava25To27() {
		// ...
	}

	@Test
	@DisabledOnJre(versions = 26)
	void notOnJava26() {
		// ...
	}

	@Test
	@DisabledOnJre(versions = { 25, 26 })
	// Can also be expressed as follows.
	// @DisabledOnJre(value = JAVA_25, versions = 26)
	void notOnJava25And26() {
		// ...
	}

	@Test
	@DisabledForJreRange(minVersion = 26)
	void notOnJava26AndHigher() {
		// ...
	}

	@Test
	@DisabledForJreRange(minVersion = 25, maxVersion = 27)
	// Can also be expressed as follows.
	// @DisabledForJreRange(min = JAVA_25, maxVersion = 27)
	void notFromJava25To27() {
		// ...
	}
	// end::user_guide_jre_arbitrary_versions[]

	// tag::user_guide_native[]
	@Test
	@EnabledInNativeImage
	void onlyWithinNativeImage() {
		// ...
	}

	@Test
	@DisabledInNativeImage
	void neverWithinNativeImage() {
		// ...
	}
	// end::user_guide_native[]

	// tag::user_guide_system_property[]
	@Test
	@EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
	void onlyOn64BitArchitectures() {
		// ...
	}

	@Test
	@DisabledIfSystemProperty(named = "ci-server", matches = "true")
	void notOnCiServer() {
		// ...
	}
	// end::user_guide_system_property[]

	// tag::user_guide_environment_variable[]
	@Test
	@EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server")
	void onlyOnStagingServer() {
		// ...
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "ENV", matches = ".*development.*")
	void notOnDeveloperWorkstation() {
		// ...
	}
	// end::user_guide_environment_variable[]

	// tag::user_guide_custom[]
	@Test
	@EnabledIf("customCondition")
	void enabled() {
		// ...
	}

	@Test
	@DisabledIf("customCondition")
	void disabled() {
		// ...
	}

	boolean customCondition() {
		return true;
	}
	// end::user_guide_custom[]

}
