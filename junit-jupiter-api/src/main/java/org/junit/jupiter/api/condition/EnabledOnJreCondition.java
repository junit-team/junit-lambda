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

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExecutionCondition;

/**
 * {@link ExecutionCondition} for {@link EnabledOnJre @EnabledOnJre}.
 *
 * @since 5.1
 * @see EnabledOnJre
 */
class EnabledOnJreCondition extends AbstractJreCondition<EnabledOnJre> {

	EnabledOnJreCondition() {
		super(EnabledOnJre.class, EnabledOnJre::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledOnJre annotation) {
		JRE[] jres = annotation.value();
		int[] versions = annotation.versions();
		validateVersions(jres, versions);
		return Arrays.stream(jres).anyMatch(JRE::isCurrentVersion)
				|| Arrays.stream(versions).anyMatch(JRE::isCurrentVersion);
	}

}
