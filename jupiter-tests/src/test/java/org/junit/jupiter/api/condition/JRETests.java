/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_20;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.junit.jupiter.api.condition.JRE.JAVA_22;
import static org.junit.jupiter.api.condition.JRE.JAVA_23;
import static org.junit.jupiter.api.condition.JRE.JAVA_24;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JRE}
 *
 * @since 5.7
 */
public class JRETests {

	private static final JRE CURRENT_JRE = JRE.currentJre();

	@Test
	@EnabledOnJre(JAVA_17)
	void jre17() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_17);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(17);
	}

	@Test
	@EnabledOnJre(JAVA_18)
	void jre18() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_18);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(18);
	}

	@Test
	@EnabledOnJre(JAVA_19)
	void jre19() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_19);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(19);
	}

	@Test
	@EnabledOnJre(JAVA_20)
	void jre20() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_20);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(20);
	}

	@Test
	@EnabledOnJre(JAVA_21)
	void jre21() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_21);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(21);
	}

	@Test
	@EnabledOnJre(JAVA_22)
	void jre22() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_21);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(22);
	}

	@Test
	@EnabledOnJre(JAVA_23)
	void jre23() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_23);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(23);
	}

	@Test
	@EnabledOnJre(JAVA_24)
	void jre24() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_24);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(24);
	}

	@Test
	@EnabledOnJre(JAVA_25)
	void jre25() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(JAVA_25);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(25);
	}

	@Test
	@EnabledOnJre(versions = 17)
	void version17() {
		jre17();
	}

	@Test
	@EnabledOnJre(versions = 18)
	void version18() {
		jre18();
	}

	@Test
	@EnabledOnJre(versions = 19)
	void version19() {
		jre19();
	}

	@Test
	@EnabledOnJre(versions = 20)
	void version20() {
		jre20();
	}

	@Test
	@EnabledOnJre(versions = 21)
	void version21() {
		jre21();
	}

	@Test
	@EnabledOnJre(versions = 22)
	void version22() {
		jre22();
	}

	@Test
	@EnabledOnJre(versions = 23)
	void version23() {
		jre23();
	}

	@Test
	@EnabledOnJre(versions = 24)
	void version24() {
		jre24();
	}

	@Test
	@EnabledOnJre(versions = 25)
	void version25() {
		jre25();
	}

	@Test
	@EnabledOnJre(OTHER)
	void jreOther() {
		assertThat(CURRENT_JRE).as("current version").isEqualTo(OTHER);
		assertThat(CURRENT_JRE.version()).as("current feature version").isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@EnabledOnJre(versions = Integer.MAX_VALUE)
	void versionMaxInteger() {
		jreOther();
	}

}
