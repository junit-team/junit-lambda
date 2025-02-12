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
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava20;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava21;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava22;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava23;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava24;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava25;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledForJreRange @EnabledForJreRange}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledForJreRangeIntegrationTests}.
 *
 * @since 5.6
 */
class EnabledForJreRangeConditionTests extends AbstractExecutionConditionTests {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledForJreRangeCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledForJreRangeIntegrationTests.class;
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledForJreRange is not present");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#defaultValues()
	 */
	@Test
	void defaultValues() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"You must declare a non-default value for the minimum or maximum value in @EnabledForJreRange");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#effectiveJreDefaultValues()
	 */
	@Test
	void effectiveJreDefaultValues() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#effectiveVersionDefaultValues()
	 */
	@Test
	void effectiveVersionDefaultValues() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min8()
	 */
	@Test
	void min8() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion8()
	 */
	@Test
	void minVersion8() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#maxOther()
	 */
	@Test
	void maxOther() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#maxVersionMaxInteger()
	 */
	@Test
	void maxVersionMaxInteger() {
		defaultValues();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion7()
	 */
	@Test
	void minVersion7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("@EnabledForJreRange's minVersion [7] must be greater than or equal to 8");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#maxVersion7()
	 */
	@Test
	void maxVersion7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("@EnabledForJreRange's maxVersion [7] must be greater than or equal to 8");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minAndMinVersion()
	 */
	@Test
	void minAndMinVersion() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@EnabledForJreRange's minimum value must be configured with either a JRE enum constant or numeric version, but not both");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#maxAndMaxVersion()
	 */
	@Test
	void maxAndMaxVersion() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@EnabledForJreRange's maximum value must be configured with either a JRE enum constant or numeric version, but not both");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minGreaterThanMax()
	 */
	@Test
	void minGreaterThanMax() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage(
					"@EnabledForJreRange's minimum value [21] must be less than or equal to its maximum value [11]");
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minGreaterThanMaxVersion()
	 */
	@Test
	void minGreaterThanMaxVersion() {
		minGreaterThanMax();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersionGreaterThanMaxVersion()
	 */
	@Test
	void minVersionGreaterThanMaxVersion() {
		minGreaterThanMax();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersionGreaterThanMax()
	 */
	@Test
	void minVersionGreaterThanMax() {
		minGreaterThanMax();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min20()
	 */
	@Test
	void min20() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava20() || onJava21() || onJava22() || onJava23() || onJava24() || onJava25());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion20()
	 */
	@Test
	void minVersion20() {
		min20();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#max21()
	 */
	@Test
	void max21() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(
			onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14() || onJava15()
					|| onJava16() || onJava17() || onJava18() || onJava19() || onJava20() || onJava21());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#maxVersion21()
	 */
	@Test
	void maxVersion21() {
		max21();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min8Max21()
	 */
	@Test
	void min8Max21() {
		max21();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min17Max17()
	 */
	@Test
	void min17Max17() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava17());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min17MaxVersion17()
	 */
	@Test
	void min17MaxVersion17() {
		min17Max17();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion17Max17()
	 */
	@Test
	void minVersion17Max17() {
		min17Max17();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion17MaxVersion17()
	 */
	@Test
	void minVersion17MaxVersion17() {
		min17Max17();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min20Max21()
	 */
	@Test
	void min20Max21() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava20() || onJava21());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#min20MaxVersion21()
	 */
	@Test
	void min20MaxVersion21() {
		min20Max21();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion20Max21()
	 */
	@Test
	void minVersion20Max21() {
		min20Max21();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion20MaxVersion21()
	 */
	@Test
	void minVersion20MaxVersion21() {
		min20Max21();
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minVersion17MaxVersionMaxInteger()
	 */
	@Test
	void minVersion17MaxVersionMaxInteger() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava17() || onJava18() || onJava19() || onJava20() || onJava21() || onJava22()
				|| onJava23() || onJava24() || onJava25());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minOtherMaxOther()
	 */
	@Test
	void minOtherMaxOther() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!onKnownVersion());
	}

	/**
	 * @see EnabledForJreRangeIntegrationTests#minMaxIntegerMaxMaxInteger()
	 */
	@Test
	void minMaxIntegerMaxMaxInteger() {
		minOtherMaxOther();
	}

	private void assertEnabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + JAVA_VERSION);
		}
		else {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + JAVA_VERSION);
		}
	}

}
