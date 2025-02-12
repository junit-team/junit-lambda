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

import org.junit.jupiter.api.extension.ExecutionCondition;

/**
 * {@link ExecutionCondition} for {@link DisabledOnJre @DisabledOnJre}.
 *
 * @since 5.1
 * @see DisabledOnJre
 */
class DisabledOnJreCondition extends AbstractJreCondition<DisabledOnJre> {

	DisabledOnJreCondition() {
		super(DisabledOnJre.class, DisabledOnJre::disabledReason);
	}

	@Override
	boolean isEnabled(DisabledOnJre annotation) {
		return validatedVersions(annotation.value(), annotation.versions()).noneMatch(JRE::isCurrentVersion);
	}

}
