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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_20;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.OTHER;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava10;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava11;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava12;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava13;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava14;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava15;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava16;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava17;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava18;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava19;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava20;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava21;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava22;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava23;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledForJreRange @EnabledForJreRange}.
 *
 * @since 5.6
 */
class EnabledForJreRangeIntegrationTests {

	private static final JRE CURRENT_JRE = JRE.currentJre();

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange
	void defaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(min = JAVA_8, max = OTHER)
	void effectiveJreDefaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(minVersion = 8, maxVersion = Integer.MAX_VALUE)
	void effectiveVersionDefaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(min = JAVA_8)
	void min8() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(minVersion = 8)
	void minVersion8() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(max = OTHER)
	void maxOther() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(maxVersion = Integer.MAX_VALUE)
	void maxVersionMaxInteger() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(minVersion = 7)
	void minVersion7() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(maxVersion = 7)
	void maxVersion7() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(min = JAVA_18, minVersion = 21)
	void minAndMinVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(max = JAVA_18, maxVersion = 21)
	void maxAndMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(min = JAVA_21, max = JAVA_11)
	void minGreaterThanMax() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(min = JAVA_21, maxVersion = 11)
	void minGreaterThanMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(minVersion = 21, maxVersion = 11)
	void minVersionGreaterThanMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange(minVersion = 21, max = JAVA_11)
	void minVersionGreaterThanMax() {
		fail("should result in a configuration exception");
	}

	@Test
	@EnabledForJreRange(min = JAVA_20)
	void min20() {
		assertTrue(onKnownVersion());
		assertTrue(JRE.currentVersionNumber() >= 20);
		assertTrue(CURRENT_JRE.compareTo(JAVA_20) >= 0);
		assertTrue(CURRENT_JRE.version() >= 20);
		assertFalse(onJava19());
	}

	@Test
	@EnabledForJreRange(minVersion = 20)
	void minVersion20() {
		min20();
	}

	@Test
	@EnabledForJreRange(max = JAVA_21)
	void max21() {
		assertTrue(onKnownVersion());
		assertTrue(JRE.currentVersionNumber() <= 21);
		assertTrue(CURRENT_JRE.compareTo(JAVA_21) <= 0);
		assertTrue(CURRENT_JRE.version() <= 21);

		assertTrue(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14()
				|| onJava15() || onJava16() || onJava17() || onJava18() || onJava19() || onJava20() || onJava21());
		assertFalse(onJava22());
	}

	@Test
	@EnabledForJreRange(maxVersion = 21)
	void maxVersion21() {
		max21();
	}

	@Test
	@EnabledForJreRange(min = JAVA_8, max = JAVA_21)
	void min8Max21() {
		max21();
	}

	@Test
	@EnabledForJreRange(min = JAVA_17, max = JAVA_17)
	void min17Max17() {
		assertTrue(onJava17());
	}

	@Test
	@EnabledForJreRange(min = JAVA_17, maxVersion = 17)
	void min17MaxVersion17() {
		min17Max17();
	}

	@Test
	@EnabledForJreRange(minVersion = 17, max = JAVA_17)
	void minVersion17Max17() {
		min17Max17();
	}

	@Test
	@EnabledForJreRange(minVersion = 17, maxVersion = 17)
	void minVersion17MaxVersion17() {
		min17Max17();
	}

	@Test
	@EnabledForJreRange(min = JAVA_20, max = JAVA_21)
	void min20Max21() {
		assertTrue(onJava20() || onJava21());
		assertFalse(onJava17() || onJava23());
	}

	@Test
	@EnabledForJreRange(min = JAVA_20, maxVersion = 21)
	void min20MaxVersion21() {
		min20Max21();
	}

	@Test
	@EnabledForJreRange(minVersion = 20, max = JAVA_21)
	void minVersion20Max21() {
		min20Max21();
	}

	@Test
	@EnabledForJreRange(minVersion = 20, maxVersion = 21)
	void minVersion20MaxVersion21() {
		min20Max21();
	}

	@Test
	@EnabledForJreRange(minVersion = 17, maxVersion = Integer.MAX_VALUE)
	void minVersion17MaxVersionMaxInteger() {
		assertTrue(onKnownVersion());
		assertTrue(JRE.currentVersionNumber() >= 17);
	}

	@Test
	@EnabledForJreRange(min = OTHER, max = OTHER)
	void minOtherMaxOther() {
		assertFalse(onKnownVersion());
	}

	@Test
	@EnabledForJreRange(minVersion = Integer.MAX_VALUE, maxVersion = Integer.MAX_VALUE)
	void minMaxIntegerMaxMaxInteger() {
		minOtherMaxOther();
	}

}
