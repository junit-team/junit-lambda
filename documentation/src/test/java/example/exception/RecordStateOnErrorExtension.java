/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.exception;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;

// @formatter:off
// tag::user_guide[]
class RecordStateOnErrorExtension implements LifecycleMethodExecutionExceptionHandler {

	@Override
	public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable ex)
			throws Throwable {
		memoryDumpForFurtherInvestigation("Failure recorded during class setup");
		throw ex;
	}

	@Override
	public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable ex)
			throws Throwable {
		memoryDumpForFurtherInvestigation("Failure recorded during test setup");
		throw ex;
	}

	@Override
	public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable ex)
			throws Throwable {
		memoryDumpForFurtherInvestigation("Failure recorded during test cleanup");
		throw ex;
	}

	@Override
	public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable ex)
			throws Throwable {
		memoryDumpForFurtherInvestigation("Failure recorded during class cleanup");
		throw ex;
	}
	// end::user_guide[]

	private void memoryDumpForFurtherInvestigation(String error) {
	}
	// tag::user_guide[]
}
// end::user_guide[]
// @formatter:on
