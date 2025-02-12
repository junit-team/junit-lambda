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
 * {@link ExecutionCondition} for {@link DisabledForJreRange @DisabledForJreRange}.
 *
 * @since 5.6
 * @see DisabledForJreRange
 */
class DisabledForJreRangeCondition extends AbstractJreRangeCondition<DisabledForJreRange> {

	DisabledForJreRangeCondition() {
		super(DisabledForJreRange.class, DisabledForJreRange::disabledReason);
	}

	@Override
	boolean isEnabled(DisabledForJreRange range) {
		return !isCurrentVersionWithinRange(range.min(), range.max(), range.minVersion(), range.maxVersion());
	}

}
