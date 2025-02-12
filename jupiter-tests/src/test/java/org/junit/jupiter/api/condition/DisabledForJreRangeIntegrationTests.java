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
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
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
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DisabledForJreRange @DisabledForJreRange}.
 *
 * @since 5.6
 */
class DisabledForJreRangeIntegrationTests {

	@Test
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange
	void defaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(min = JAVA_8, max = OTHER)
	void effectiveJreDefaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(minVersion = 8, maxVersion = Integer.MAX_VALUE)
	void effectiveVersionDefaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(min = JAVA_8)
	void min8() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(minVersion = 8)
	void minVersion8() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(max = OTHER)
	void maxOther() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(maxVersion = Integer.MAX_VALUE)
	void maxVersionMaxInteger() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(minVersion = 7)
	void minVersion7() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(maxVersion = 7)
	void maxVersion7() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(min = JAVA_18, minVersion = 21)
	void minAndMinVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(max = JAVA_18, maxVersion = 21)
	void maxAndMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(min = JAVA_21, max = JAVA_11)
	void minGreaterThanMax() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(min = JAVA_21, maxVersion = 11)
	void minGreaterThanMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(minVersion = 21, maxVersion = 11)
	void minVersionGreaterThanMaxVersion() {
		fail("should result in a configuration exception");
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledForJreRange(minVersion = 21, max = JAVA_11)
	void minVersionGreaterThanMax() {
		fail("should result in a configuration exception");
	}

	@Test
	@DisabledForJreRange(min = JAVA_18)
	void min18() {
		assertFalse(onJava18() || onJava19());
		assertTrue(onJava17());
	}

	@Test
	@DisabledForJreRange(minVersion = 18)
	void minVersion18() {
		min18();
	}

	@Test
	@DisabledForJreRange(max = JAVA_18)
	void max18() {
		assertFalse(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14()
				|| onJava15() || onJava16() || onJava17() || onJava18());
	}

	@Test
	@DisabledForJreRange(maxVersion = 18)
	void maxVersion18() {
		max18();
	}

	@Test
	@DisabledForJreRange(min = JAVA_17, max = JAVA_17)
	void min17Max17() {
		assertFalse(onJava17());
	}

	@Test
	@DisabledForJreRange(minVersion = 17, maxVersion = 17)
	void minVersion17MaxVersion17() {
		min17Max17();
	}

	@Test
	@DisabledForJreRange(min = JAVA_18, max = JAVA_19, disabledReason = "Disabled on Java 18 & 19")
	void min18Max19() {
		assertFalse(onJava18() || onJava19());
	}

	@Test
	@DisabledForJreRange(minVersion = 18, maxVersion = 19, disabledReason = "Disabled on Java 18 & 19")
	void minVersion18MaxVersion19() {
		min18Max19();
	}

	@Test
	@DisabledForJreRange(min = OTHER, max = OTHER)
	void minOtherMaxOther() {
		assertTrue(onKnownVersion());
	}

	@Test
	@DisabledForJreRange(minVersion = Integer.MAX_VALUE, maxVersion = Integer.MAX_VALUE)
	void minMaxIntegerMaxMaxInteger() {
		minOtherMaxOther();
	}

}
