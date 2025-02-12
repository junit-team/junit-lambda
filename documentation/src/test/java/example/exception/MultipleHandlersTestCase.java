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

import example.exception.MultipleHandlersTestCase.ThirdExecutedHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

// @formatter:off
// tag::user_guide[]
// Register handlers for @Test, @BeforeEach, @AfterEach as well as @BeforeAll and @AfterAll
@ExtendWith(ThirdExecutedHandler.class)
class MultipleHandlersTestCase {

	// Register handlers for @Test, @BeforeEach, @AfterEach only
	@ExtendWith(SecondExecutedHandler.class)
	@ExtendWith(FirstExecutedHandler.class)
	@Test
	void testMethod() {
	}

	// end::user_guide[]

	static class FirstExecutedHandler implements TestExecutionExceptionHandler {
		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable ex)
				throws Throwable {
			throw ex;
		}
	}

	static class SecondExecutedHandler implements LifecycleMethodExecutionExceptionHandler {
		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable ex)
				throws Throwable {
			throw ex;
		}
	}

	static class ThirdExecutedHandler implements LifecycleMethodExecutionExceptionHandler {
		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable ex)
				throws Throwable {
			throw ex;
		}
	}
	// tag::user_guide[]
}
// end::user_guide[]
// @formatter:on
