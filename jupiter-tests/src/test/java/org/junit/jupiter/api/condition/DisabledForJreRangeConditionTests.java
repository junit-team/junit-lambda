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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledForJreRange}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledForJreRangeIntegrationTests}.
 *
 * @since 5.6
 */
class DisabledForJreRangeConditionTests extends AbstractExecutionConditionTests {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledForJreRangeCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledForJreRangeIntegrationTests.class;
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledForJreRange is not present");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#defaultValues()
	 */
	@Test
	void defaultValues() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"You must declare a non-default value for the minimum or maximum value in @DisabledForJreRange");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#effectiveJreDefaultValues()
	 */
	@Test
	void effectiveJreDefaultValues() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#effectiveVersionDefaultValues()
	 */
	@Test
	void effectiveVersionDefaultValues() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#min8()
	 */
	@Test
	void min8() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersion8()
	 */
	@Test
	void minVersion8() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#maxOther()
	 */
	@Test
	void maxOther() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#maxVersionMaxInteger()
	 */
	@Test
	void maxVersionMaxInteger() {
		defaultValues();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersion7()
	 */
	@Test
	void minVersion7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("@DisabledForJreRange's minVersion [7] must be greater than or equal to 8");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#maxVersion7()
	 */
	@Test
	void maxVersion7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("@DisabledForJreRange's maxVersion [7] must be greater than or equal to 8");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minAndMinVersion()
	 */
	@Test
	void minAndMinVersion() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@DisabledForJreRange's minimum value must be configured with either a JRE enum constant or numeric version, but not both");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#maxAndMaxVersion()
	 */
	@Test
	void maxAndMaxVersion() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@DisabledForJreRange's maximum value must be configured with either a JRE enum constant or numeric version, but not both");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minGreaterThanMax()
	 */
	@Test
	void minGreaterThanMax() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@DisabledForJreRange's minimum value [21] must be less than or equal to its maximum value [11]");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minGreaterThanMaxVersion()
	 */
	@Test
	void minGreaterThanMaxVersion() {
		minGreaterThanMax();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersionGreaterThanMaxVersion()
	 */
	@Test
	void minVersionGreaterThanMaxVersion() {
		minGreaterThanMax();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersionGreaterThanMax()
	 */
	@Test
	void minVersionGreaterThanMax() {
		minGreaterThanMax();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#min18()
	 */
	@Test
	void min18() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!onJava17());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersion18()
	 */
	@Test
	void minVersion18() {
		min18();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#max18()
	 */
	@Test
	void max18() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13()
				|| onJava14() || onJava15() || onJava16() || onJava17() || onJava18());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#maxVersion18()
	 */
	@Test
	void maxVersion18() {
		max18();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#min17Max17()
	 */
	@Test
	void min17Max17() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava17());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersion17MaxVersion17()
	 */
	@Test
	void minVersion17MaxVersion17() {
		min17Max17();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#min18Max19()
	 */
	@Test
	void min18Max19() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava18() || onJava19());
		assertCustomDisabledReasonIs("Disabled on Java 18 & 19");
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minVersion18MaxVersion19()
	 */
	@Test
	void minVersion18MaxVersion19() {
		min18Max19();
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minOtherMaxOther()
	 */
	@Test
	void minOtherMaxOther() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!onKnownVersion());
	}

	/**
	 * @see DisabledForJreRangeIntegrationTests#minMaxIntegerMaxMaxInteger()
	 */
	@Test
	void minMaxIntegerMaxMaxInteger() {
		minOtherMaxOther();
	}

	private void assertDisabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + JAVA_VERSION);
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + JAVA_VERSION);
		}
	}

}
