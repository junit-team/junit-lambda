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

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

abstract class BooleanExecutionCondition<A extends Annotation> implements ExecutionCondition {

	protected final Class<A> annotationType;
	private final String enabledReason;
	private final String disabledReason;
	private final Function<A, String> customDisabledReason;

	BooleanExecutionCondition(Class<A> annotationType, String enabledReason, String disabledReason,
			Function<A, String> customDisabledReason) {

		this.annotationType = annotationType;
		this.enabledReason = enabledReason;
		this.disabledReason = disabledReason;
		this.customDisabledReason = customDisabledReason;
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return findAnnotation(context.getElement(), this.annotationType) //
				.map(annotation -> isEnabled(annotation) ? enabled(this.enabledReason)
						: disabled(this.disabledReason, this.customDisabledReason.apply(annotation))) //
				.orElseGet(this::enabledByDefault);
	}

	abstract boolean isEnabled(A annotation);

	private ConditionEvaluationResult enabledByDefault() {
		String reason = String.format("@%s is not present", this.annotationType.getSimpleName());
		return enabled(reason);
	}

}
