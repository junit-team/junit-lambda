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
 * {@link ExecutionCondition} for {@link EnabledForJreRange @EnabledForJreRange}.
 *
 * @since 5.6
 * @see EnabledForJreRange
 */
class EnabledForJreRangeCondition extends AbstractJreRangeCondition<EnabledForJreRange> {

	EnabledForJreRangeCondition() {
		super(EnabledForJreRange.class, EnabledForJreRange::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledForJreRange range) {
		return isCurrentVersionWithinRange(range.min(), range.max(), range.minVersion(), range.maxVersion());
	}

}
