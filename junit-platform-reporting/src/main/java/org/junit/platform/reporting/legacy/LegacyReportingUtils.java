/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy;

import static org.apiguardian.api.API.Status.MAINTAINED;

import org.apiguardian.api.API;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Utility methods for dealing with legacy reporting infrastructure, such as
 * reporting systems built on the Ant-based XML reporting format for JUnit 4.
 *
 * <p>This class was formerly from {@code junit-platform-launcher} in the
 * {@link org.junit.platform.launcher.listeners} package.
 *
 * @since 1.0.3
 */
@API(status = MAINTAINED, since = "1.6")
public final class LegacyReportingUtils {

	private LegacyReportingUtils() {
		/* no-op */
	}

	/**
	 * Get the class name for the supplied {@link TestIdentifier} using the
	 * supplied {@link TestPlan}.
	 *
	 * <p>This implementation attempts to find the closest test identifier with
	 * a {@link ClassSource} by traversing the hierarchy upwards towards the
	 * root starting with the supplied test identifier. In case no such source
	 * is found, it falls back to using the parent's
	 * {@linkplain TestIdentifier#getLegacyReportingName legacy reporting name}.
	 *
	 * @param testPlan the test plan that contains the {@code TestIdentifier};
	 * never {@code null}
	 * @param testIdentifier the identifier to determine the class name for;
	 * never {@code null}
	 * @see TestIdentifier#getLegacyReportingName
	 */
	@SuppressWarnings("deprecation")
	public static String getClassName(TestPlan testPlan, TestIdentifier testIdentifier) {
		return org.junit.platform.launcher.listeners.LegacyReportingUtils.getClassName(testPlan, testIdentifier);
	}

}
