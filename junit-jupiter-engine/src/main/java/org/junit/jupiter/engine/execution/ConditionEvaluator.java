/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static java.lang.String.format;
import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

/**
 * {@code ConditionEvaluator} evaluates {@link ExecutionCondition} extensions.
 *
 * @since 5.0
 * @see ExecutionCondition
 */
@API(status = INTERNAL, since = "5.0")
public class ConditionEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluator.class);

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link ExecutionCondition} extensions registered for the
	 * supplied {@link ExtensionContext}.
	 *
	 * @param context the current {@code ExtensionContext}
	 * @return the first <em>disabled</em> {@code ConditionEvaluationResult},
	 * or a default <em>enabled</em> {@code ConditionEvaluationResult} if no
	 * disabled conditions are encountered
	 */
	public ConditionEvaluationResult evaluate(ExtensionRegistry extensionRegistry, JupiterConfiguration configuration,
			ExtensionContext context) {

		// @formatter:off
		return extensionRegistry.stream(ExecutionCondition.class)
				.filter(configuration.getExecutionConditionFilter())
				.map(condition -> evaluate(condition, context))
				.filter(ConditionEvaluationResult::isDisabled)
				.findFirst()
				.orElse(ENABLED);
		// @formatter:on
	}

	private ConditionEvaluationResult evaluate(ExecutionCondition condition, ExtensionContext context) {
		try {
			ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
			logResult(condition.getClass(), result, context);
			return result;
		}
		catch (Exception ex) {
			throw evaluationException(condition.getClass(), ex);
		}
	}

	private void logResult(Class<?> conditionType, ConditionEvaluationResult result, ExtensionContext context) {
		logger.trace(() -> format("Evaluation of condition [%s] on [%s] resulted in: %s", conditionType.getName(),
			context.getElement().orElse(null), result));
	}

	private ConditionEvaluationException evaluationException(Class<?> conditionType, Exception ex) {
		String cause = StringUtils.isNotBlank(ex.getMessage()) ? ": " + ex.getMessage() : "";
		return new ConditionEvaluationException(
			format("Failed to evaluate condition [%s]%s", conditionType.getName(), cause), ex);
	}

}
