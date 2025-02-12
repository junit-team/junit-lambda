/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;

import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

/**
 * Builder for {@link AssertionFailedError AssertionFailedErrors}.
 * <p>
 * Using this builder ensures consistency in how failure message are formatted
 * within JUnit Jupiter and for custom user-defined assertions.
 *
 * @since 5.9
 * @see AssertionFailedError
 */
@API(status = STABLE, since = "5.9")
public class AssertionFailureBuilder {

	private Object message;
	private Throwable cause;
	private boolean mismatch;
	private Object expected;
	private Object actual;
	private String reason;
	private boolean includeValuesInMessage = true;

	/**
	 * Create a new {@code AssertionFailureBuilder}.
	 */
	public static AssertionFailureBuilder assertionFailure() {
		return new AssertionFailureBuilder();
	}

	private AssertionFailureBuilder() {
	}

	/**
	 * Set the user-defined message of the assertion.
	 * <p>
	 * The {@code message} may be passed as a {@link Supplier} or plain
	 * {@link String}. If any other type is passed, it is converted to
	 * {@code String} as per {@link StringUtils#nullSafeToString(Object)}.
	 *
	 * @param message the user-defined failure message; may be {@code null}
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder message(Object message) {
		this.message = message;
		return this;
	}

	/**
	 * Set the reason why the assertion failed.
	 *
	 * @param reason the failure reason; may be {@code null}
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder reason(String reason) {
		this.reason = reason;
		return this;
	}

	/**
	 * Set the cause of the assertion failure.
	 *
	 * @param cause the failure cause; may be {@code null}
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder cause(Throwable cause) {
		this.cause = cause;
		return this;
	}

	/**
	 * Set the expected value of the assertion.
	 *
	 * @param expected the expected value; may be {@code null}
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder expected(Object expected) {
		this.mismatch = true;
		this.expected = expected;
		return this;
	}

	/**
	 * Set the actual value of the assertion.
	 *
	 * @param actual the actual value; may be {@code null}
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder actual(Object actual) {
		this.mismatch = true;
		this.actual = actual;
		return this;
	}

	/**
	 * Set whether to include the actual and expected values in the generated
	 * failure message.
	 *
	 * @param includeValuesInMessage whether to include the actual and expected
	 * values
	 * @return this builder for method chaining
	 */
	public AssertionFailureBuilder includeValuesInMessage(boolean includeValuesInMessage) {
		this.includeValuesInMessage = includeValuesInMessage;
		return this;
	}

	/**
	 * Build the {@link AssertionFailedError AssertionFailedError} and throw it.
	 *
	 * @throws AssertionFailedError always
	 */
	public void buildAndThrow() throws AssertionFailedError {
		throw build();
	}

	/**
	 * Build the {@link AssertionFailedError AssertionFailedError} without
	 * throwing it.
	 *
	 * @return the built assertion failure
	 */
	public AssertionFailedError build() {
		String reason = nullSafeGet(this.reason);
		if (mismatch && includeValuesInMessage) {
			reason = (reason == null ? "" : reason + ", ") + formatValues(expected, actual);
		}
		String message = nullSafeGet(this.message);
		if (reason != null) {
			message = buildPrefix(message) + reason;
		}
		return mismatch //
				? new AssertionFailedError(message, expected, actual, cause) //
				: new AssertionFailedError(message, cause);
	}

	private static String nullSafeGet(Object messageOrSupplier) {
		if (messageOrSupplier == null) {
			return null;
		}
		if (messageOrSupplier instanceof Supplier) {
			Object message = ((Supplier<?>) messageOrSupplier).get();
			return StringUtils.nullSafeToString(message);
		}
		return StringUtils.nullSafeToString(messageOrSupplier);
	}

	private static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}

	private static String formatValues(Object expected, Object actual) {
		String expectedString = toString(expected);
		String actualString = toString(actual);
		if (expectedString.equals(actualString)) {
			return String.format("expected: %s but was: %s", formatClassAndValue(expected, expectedString),
				formatClassAndValue(actual, actualString));
		}
		return String.format("expected: <%s> but was: <%s>", expectedString, actualString);
	}

	private static String formatClassAndValue(Object value, String valueString) {
		// If the value is null, return <null> instead of null<null>.
		if (value == null) {
			return "<null>";
		}
		String classAndHash = getClassName(value) + toHash(value);
		// if it's a class, there's no need to repeat the class name contained in the valueString.
		return (value instanceof Class ? "<" + classAndHash + ">" : classAndHash + "<" + valueString + ">");
	}

	private static String toString(Object obj) {
		if (obj instanceof Class) {
			return getCanonicalName((Class<?>) obj);
		}
		return StringUtils.nullSafeToString(obj);
	}

	private static String toHash(Object obj) {
		return (obj == null ? "" : "@" + Integer.toHexString(System.identityHashCode(obj)));
	}

	private static String getClassName(Object obj) {
		return (obj == null ? "null"
				: obj instanceof Class ? getCanonicalName((Class<?>) obj) : obj.getClass().getName());
	}
}
