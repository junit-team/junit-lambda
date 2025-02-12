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

import static org.junit.jupiter.api.condition.AbstractJreCondition.DISABLED_ON_CURRENT_JRE;
import static org.junit.jupiter.api.condition.AbstractJreCondition.ENABLED_ON_CURRENT_JRE;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.junit.platform.commons.util.Preconditions;

/**
 * Abstract base class for {@link EnabledForJreRangeCondition} and
 * {@link DisabledForJreRangeCondition}.
 *
 * @since 5.12
 */
abstract class AbstractJreRangeCondition<A extends Annotation> extends BooleanExecutionCondition<A> {

	private final String annotationName;

	AbstractJreRangeCondition(Class<A> annotationType, Function<A, String> customDisabledReason) {
		super(annotationType, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE, customDisabledReason);
		this.annotationName = annotationType.getSimpleName();
	}

	protected final boolean isCurrentVersionWithinRange(JRE minJre, JRE maxJre, int minVersion, int maxVersion) {
		boolean minJreSet = minJre != JRE.UNDEFINED;
		boolean maxJreSet = maxJre != JRE.UNDEFINED;
		boolean minVersionSet = minVersion != JRE.UNDEFINED_VERSION;
		boolean maxVersionSet = maxVersion != JRE.UNDEFINED_VERSION;

		// Users must choose between JRE enum constants and version numbers.
		Preconditions.condition(!minJreSet || !minVersionSet, () -> String.format(
			"@%s's minimum value must be configured with either a JRE enum constant or numeric version, but not both",
			this.annotationName));
		Preconditions.condition(!maxJreSet || !maxVersionSet, () -> String.format(
			"@%s's maximum value must be configured with either a JRE enum constant or numeric version, but not both",
			this.annotationName));

		// Users must supply valid values for minVersion and maxVersion.
		Preconditions.condition(!minVersionSet || (minVersion >= JRE.MINIMUM_VERSION),
			() -> String.format("@%s's minVersion [%d] must be greater than or equal to %d", this.annotationName,
				minVersion, JRE.MINIMUM_VERSION));
		Preconditions.condition(!maxVersionSet || (maxVersion >= JRE.MINIMUM_VERSION),
			() -> String.format("@%s's maxVersion [%d] must be greater than or equal to %d", this.annotationName,
				maxVersion, JRE.MINIMUM_VERSION));

		// Now that we have checked the basic preconditions, we need to ensure that we are
		// using valid JRE enum constants.
		if (!minJreSet) {
			minJre = JRE.JAVA_8;
		}
		if (!maxJreSet) {
			maxJre = JRE.OTHER;
		}

		int min = (minVersionSet ? minVersion : minJre.version());
		int max = (maxVersionSet ? maxVersion : maxJre.version());

		// Finally, we need to validate the effective minimum and maximum values.
		Preconditions.condition((min != JRE.MINIMUM_VERSION || max != Integer.MAX_VALUE),
			() -> "You must declare a non-default value for the minimum or maximum value in @" + this.annotationName);
		Preconditions.condition(min >= JRE.MINIMUM_VERSION,
			() -> String.format("@%s's minimum value [%d] must greater than or equal to %d", this.annotationName, min,
				JRE.MINIMUM_VERSION));
		Preconditions.condition(min <= max,
			() -> String.format("@%s's minimum value [%d] must be less than or equal to its maximum value [%d]",
				this.annotationName, min, max));

		return JRE.isCurrentVersionWithinRange(min, max);
	}

}
