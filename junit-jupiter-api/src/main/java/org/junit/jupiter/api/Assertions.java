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

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.MultipleFailuresError;

/**
 * {@code Assertions} is a collection of utility methods that support asserting
 * conditions in tests.
 *
 * <p>Unless otherwise noted, a <em>failed</em> assertion will throw an
 * {@link org.opentest4j.AssertionFailedError} or a subclass thereof.
 *
 * <h2>Object Equality</h2>
 *
 * <p>Assertion methods comparing two objects for <em>equality</em>, such as the
 * {@code assertEquals(expected, actual)} and {@code assertNotEquals(unexpected, actual)}
 * variants, are <em>only</em> intended to test equality for an (un-)expected value
 * and an actual value. They are not designed for testing whether a class correctly
 * implements {@link Object#equals(Object)}. For example, {@code assertEquals()}
 * might immediately return {@code true} when provided the same object for the
 * expected and actual values, without calling {@code equals(Object)} at all.
 * Tests that aim to verify the {@code equals(Object)} implementation should instead
 * be written to explicitly verify the {@link Object#equals(Object)} contract by
 * using {@link #assertTrue(boolean) assertTrue()} or {@link #assertFalse(boolean)
 * assertFalse()} &mdash; for example, {@code assertTrue(expected.equals(actual))},
 * {@code assertTrue(actual.equals(expected))}, {@code assertFalse(expected.equals(null))},
 * etc.
 *
 * <h2>Kotlin Support</h2>
 *
 * <p>Additional <a href="https://kotlinlang.org/">Kotlin</a> assertions can be
 * found as <em>top-level functions</em> in the {@link org.junit.jupiter.api}
 * package.
 *
 * <h2>Preemptive Timeouts</h2>
 *
 * <p>The various {@code assertTimeoutPreemptively()} methods in this class
 * execute the provided callback ({@code executable} or {@code supplier}) in a
 * different thread than that of the calling code. If the timeout is exceeded,
 * an attempt will be made to preemptively abort execution of the callback by
 * {@linkplain Thread#interrupt() interrupting} the callback's thread. If the
 * callback's thread does not return when interrupted, the thread will continue
 * to run in the background after the {@code assertTimeoutPreemptively()} method
 * has returned.
 *
 * <p>Furthermore, the behavior of {@code assertTimeoutPreemptively()} methods
 * can lead to undesirable side effects if the code that is executed within the
 * callback relies on {@link ThreadLocal} storage. One common example of this is
 * the transactional testing support in the Spring Framework. Specifically, Spring's
 * testing support binds transaction state to the current thread (via a
 * {@code ThreadLocal}) before a test method is invoked. Consequently, if a
 * callback provided to {@code assertTimeoutPreemptively()} invokes Spring-managed
 * components that participate in transactions, any actions taken by those
 * components will not be rolled back with the test-managed transaction. On the
 * contrary, such actions will be committed to the persistent store (e.g.,
 * relational database) even though the test-managed transaction is rolled back.
 * Similar side effects may be encountered with other frameworks that rely on
 * {@code ThreadLocal} storage.
 *
 * <h2>Extensibility</h2>
 *
 * <p>Although it is technically possible to extend this class, extension is
 * strongly discouraged. The JUnit Team highly recommends that the methods
 * defined in this class be used via <em>static imports</em>.
 *
 * @since 5.0
 * @see org.opentest4j.AssertionFailedError
 * @see Assumptions
 */
@API(status = STABLE, since = "5.0")
public class Assertions {

	/**
	 * Protected constructor allowing subclassing but not direct instantiation.
	 *
	 * @since 5.3
	 */
	@API(status = STABLE, since = "5.3")
	protected Assertions() {
		/* no-op */
	}

	// --- fail ----------------------------------------------------------------

	/**
	 * <em>Fail</em> the test <em>without</em> a failure message.
	 *
	 * <p>Although failing <em>with</em> an explicit failure message is recommended,
	 * this method may be useful when maintaining legacy code.
	 *
	 * <p>See Javadoc for {@link #fail(String)} for an explanation of this method's
	 * generic return type {@code V}.
	 */
	public static <V> V fail() {
		AssertionUtils.fail();
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fail</em> the test with the given failure {@code message}.
	 *
	 * <p>The generic return type {@code V} allows this method to be used
	 * directly as a single-statement lambda expression, thereby avoiding the
	 * need to implement a code block with an explicit return value. Since this
	 * method throws an {@link org.opentest4j.AssertionFailedError} before its
	 * return statement, this method never actually returns a value to its caller.
	 * The following example demonstrates how this may be used in practice.
	 *
	 * <pre>{@code
	 * Stream.of().map(entry -> fail("should not be called"));
	 * }</pre>
	 */
	public static <V> V fail(String message) {
		AssertionUtils.fail(message);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fail</em> the test with the given failure {@code message} as well
	 * as the underlying {@code cause}.
	 *
	 * <p>See Javadoc for {@link #fail(String)} for an explanation of this method's
	 * generic return type {@code V}.
	 */
	public static <V> V fail(String message, Throwable cause) {
		AssertionUtils.fail(message, cause);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fail</em> the test with the given underlying {@code cause}.
	 *
	 * <p>See Javadoc for {@link #fail(String)} for an explanation of this method's
	 * generic return type {@code V}.
	 */
	public static <V> V fail(Throwable cause) {
		AssertionUtils.fail(cause);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fail</em> the test with the failure message retrieved from the
	 * given {@code messageSupplier}.
	 *
	 * <p>See Javadoc for {@link #fail(String)} for an explanation of this method's
	 * generic return type {@code V}.
	 */
	public static <V> V fail(Supplier<String> messageSupplier) {
		AssertionUtils.fail(messageSupplier);
		return null; // appeasing the compiler: this line will never be executed.
	}

	// --- assertTrue ----------------------------------------------------------

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code true}.
	 */
	public static void assertTrue(boolean condition) {
		AssertTrue.assertTrue(condition);
	}

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		AssertTrue.assertTrue(condition, messageSupplier);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier) {
		AssertTrue.assertTrue(booleanSupplier);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		AssertTrue.assertTrue(booleanSupplier, message);
	}

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(boolean condition, String message) {
		AssertTrue.assertTrue(condition, message);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		AssertTrue.assertTrue(booleanSupplier, messageSupplier);
	}

	// --- assertFalse ---------------------------------------------------------

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code false}.
	 */
	public static void assertFalse(boolean condition) {
		AssertFalse.assertFalse(condition);
	}

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code false}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(boolean condition, String message) {
		AssertFalse.assertFalse(condition, message);
	}

	/**
	 * <em>Assert</em> that the supplied {@code condition} is {@code false}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		AssertFalse.assertFalse(condition, messageSupplier);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code false}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier) {
		AssertFalse.assertFalse(booleanSupplier);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code false}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		AssertFalse.assertFalse(booleanSupplier, message);
	}

	/**
	 * <em>Assert</em> that the boolean condition supplied by {@code booleanSupplier} is {@code false}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		AssertFalse.assertFalse(booleanSupplier, messageSupplier);
	}

	// --- assertNull ----------------------------------------------------------

	/**
	 * <em>Assert</em> that {@code actual} is {@code null}.
	 */
	public static void assertNull(Object actual) {
		AssertNull.assertNull(actual);
	}

	/**
	 * <em>Assert</em> that {@code actual} is {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNull(Object actual, String message) {
		AssertNull.assertNull(actual, message);
	}

	/**
	 * <em>Assert</em> that {@code actual} is {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		AssertNull.assertNull(actual, messageSupplier);
	}

	// --- assertNotNull -------------------------------------------------------

	/**
	 * <em>Assert</em> that {@code actual} is not {@code null}.
	 */
	public static void assertNotNull(Object actual) {
		AssertNotNull.assertNotNull(actual);
	}

	/**
	 * <em>Assert</em> that {@code actual} is not {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotNull(Object actual, String message) {
		AssertNotNull.assertNotNull(actual, message);
	}

	/**
	 * <em>Assert</em> that {@code actual} is not {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		AssertNotNull.assertNotNull(actual, messageSupplier);
	}

	// --- assertEquals --------------------------------------------------------

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, short actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, Short actual) {
		AssertEquals.assertEquals((Short) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(Short expected, short actual) {
		AssertEquals.assertEquals(expected, (Short) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Short expected, Short actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(short expected, short actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(short expected, Short actual, String message) {
		AssertEquals.assertEquals((Short) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Short expected, short actual, String message) {
		AssertEquals.assertEquals(expected, (Short) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Short expected, Short actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(short expected, short actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(short expected, Short actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Short) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Short expected, short actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Short) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Short expected, Short actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, byte actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, Byte actual) {
		AssertEquals.assertEquals((Byte) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(Byte expected, byte actual) {
		AssertEquals.assertEquals(expected, (Byte) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Byte expected, Byte actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(byte expected, byte actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(byte expected, Byte actual, String message) {
		AssertEquals.assertEquals((Byte) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Byte expected, byte actual, String message) {
		AssertEquals.assertEquals(expected, (Byte) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Byte expected, Byte actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(byte expected, byte actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(byte expected, Byte actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Byte) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Byte expected, byte actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Byte) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Byte expected, Byte actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, int actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, Integer actual) {
		AssertEquals.assertEquals((Integer) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(Integer expected, int actual) {
		AssertEquals.assertEquals(expected, (Integer) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Integer expected, Integer actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(int expected, int actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(int expected, Integer actual, String message) {
		AssertEquals.assertEquals((Integer) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Integer expected, int actual, String message) {
		AssertEquals.assertEquals(expected, (Integer) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Integer expected, Integer actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(int expected, int actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(int expected, Integer actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Integer) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Integer expected, int actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Integer) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Integer expected, Integer actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, long actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, Long actual) {
		AssertEquals.assertEquals((Long) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(Long expected, long actual) {
		AssertEquals.assertEquals(expected, (Long) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Long expected, Long actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(long expected, long actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(long expected, Long actual, String message) {
		AssertEquals.assertEquals((Long) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Long expected, long actual, String message) {
		AssertEquals.assertEquals(expected, (Long) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Long expected, Long actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(long expected, long actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(long expected, Long actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Long) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Long expected, long actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Long) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Long expected, Long actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertEquals(float expected, float actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertEquals(float expected, Float actual) {
		AssertEquals.assertEquals((Float) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertEquals(Float expected, float actual) {
		AssertEquals.assertEquals(expected, (Float) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Float expected, Float actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(float expected, float actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(float expected, Float actual, String message) {
		AssertEquals.assertEquals((Float) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Float expected, float actual, String message) {
		AssertEquals.assertEquals(expected, (Float) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Float expected, Float actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, Float actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Float) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Float expected, float actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Float) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Float expected, Float actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertEquals(float expected, float actual, float delta) {
		AssertEquals.assertEquals(expected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(float expected, float actual, float delta, String message) {
		AssertEquals.assertEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, float delta, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertEquals(double expected, double actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertEquals(double expected, Double actual) {
		AssertEquals.assertEquals((Double) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertEquals(Double expected, double actual) {
		AssertEquals.assertEquals(expected, (Double) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Double expected, Double actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(double expected, double actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(double expected, Double actual, String message) {
		AssertEquals.assertEquals((Double) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Double expected, double actual, String message) {
		AssertEquals.assertEquals(expected, (Double) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Double expected, Double actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, Double actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Double) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Double expected, double actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Double) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Double expected, Double actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertEquals(double expected, double actual, double delta) {
		AssertEquals.assertEquals(expected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(double expected, double actual, double delta, String message) {
		AssertEquals.assertEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, double delta, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, char actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, Character actual) {
		AssertEquals.assertEquals((Character) expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(Character expected, char actual) {
		AssertEquals.assertEquals(expected, (Character) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Character expected, Character actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(char expected, char actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(char expected, Character actual, String message) {
		AssertEquals.assertEquals((Character) expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertEquals(Character expected, char actual, String message) {
		AssertEquals.assertEquals(expected, (Character) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Character expected, Character actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(char expected, char actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(char expected, Character actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals((Character) expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(Character expected, char actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, (Character) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertEquals(Character expected, Character actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	// --- assertArrayEquals ---------------------------------------------------

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} float arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta,
			Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} double arrays are equal within the given non-negative {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta,
			Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	// --- assertIterableEquals --------------------------------------------

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in {@link #assertArrayEquals(Object[], Object[])},
	 * if two iterables are encountered (including {@code expected} and {@code actual}) then their
	 * iterators must return equal elements in the same order as each other. <strong>Note:</strong>
	 * this means that the iterables <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  ...
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[])
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual) {
		AssertIterableEquals.assertIterableEquals(expected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in
	 * {@link #assertArrayEquals(Object[], Object[], String)}, if two iterables are encountered
	 * (including {@code expected} and {@code actual}) then their iterators must return equal
	 * elements in the same order as each other. <strong>Note:</strong> this means that the iterables
	 * <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  ...
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[], String)
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, String message) {
		AssertIterableEquals.assertIterableEquals(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in
	 * {@link #assertArrayEquals(Object[], Object[], Supplier)}, if two iterables are encountered
	 * (including {@code expected} and {@code actual}) then their iterators must return equal
	 * elements in the same order as each other. <strong>Note:</strong> this means that the iterables
	 * <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  ...
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[], Supplier)
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual,
			Supplier<String> messageSupplier) {
		AssertIterableEquals.assertIterableEquals(expected, actual, messageSupplier);
	}

	// --- assertLinesMatch ----------------------------------------------------

	/**
	 * <em>Assert</em> that {@code expected} list of {@linkplain String}s matches {@code actual}
	 * list.
	 *
	 * <p>This method differs from other assertions that effectively only check {@link String#equals(Object)},
	 * in that it uses the following staged matching algorithm:
	 *
	 * <p>For each pair of expected and actual lines do
	 * <ol>
	 *   <li>check if {@code expected.equals(actual)} - if yes, continue with next pair</li>
	 *   <li>otherwise treat {@code expected} as a regular expression and check via
	 *   {@link String#matches(String)} - if yes, continue with next pair</li>
	 *   <li>otherwise check if {@code expected} line is a fast-forward marker, if yes apply
	 *   fast-forward actual lines accordingly (see below) and goto 1.</li>
	 * </ol>
	 *
	 * <p>A valid fast-forward marker is an expected line that starts and ends with the literal
	 * {@code >>} and contains at least 4 characters. Examples:
	 * <ul>
	 *   <li>{@code >>>>}<br>{@code >> stacktrace >>}<br>{@code >> single line, non Integer.parse()-able comment >>}
	 *   <br>Skip arbitrary number of actual lines, until first matching subsequent expected line is found. Any
	 *   character between the fast-forward literals are discarded.</li>
	 *   <li>{@code ">> 21 >>"}
	 *   <br>Skip strictly 21 lines. If they can't be skipped for any reason, an assertion error is raised.</li>
	 * </ul>
	 *
	 * <p>Here is an example showing all three kinds of expected line formats:
	 * <pre>{@code
	 * ls -la /
	 * total [\d]+
	 * drwxr-xr-x  0 root root   512 Jan  1  1970 .
	 * drwxr-xr-x  0 root root   512 Jan  1  1970 ..
	 * drwxr-xr-x  0 root root   512 Apr  5 07:45 bin
	 * >> 4 >>
	 * -rwxr-xr-x  1 root root [\d]+ Jan  1  1970 init
	 * >> M A N Y  M O R E  E N T R I E S >>
	 * drwxr-xr-x  0 root root   512 Sep 22  2017 var
	 * }</pre>
	 * <p>Fails with a generated failure message describing the difference.
	 */
	public static void assertLinesMatch(List<String> expectedLines, List<String> actualLines) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines);
	}

	/**
	 * <em>Assert</em> that {@code expected} list of {@linkplain String}s matches {@code actual}
	 * list.
	 *
	 * <p>Find a detailed description of the matching algorithm in {@link #assertLinesMatch(List, List)}.
	 *
	 * <p>Fails with the supplied failure {@code message} and the generated message.
	 *
	 * @see #assertLinesMatch(List, List)
	 */
	public static void assertLinesMatch(List<String> expectedLines, List<String> actualLines, String message) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} list of {@linkplain String}s matches {@code actual}
	 * list.
	 *
	 * <p>Find a detailed description of the matching algorithm in {@link #assertLinesMatch(List, List)}.
	 *
	 * <p>If necessary, a custom failure message will be retrieved lazily from the supplied
	 * {@code messageSupplier}. Fails with the custom failure message prepended to
	 * a generated failure message describing the difference.
	 *
	 * @see #assertLinesMatch(List, List)
	 */
	public static void assertLinesMatch(List<String> expectedLines, List<String> actualLines,
			Supplier<String> messageSupplier) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} stream of {@linkplain String}s matches {@code actual}
	 * stream.
	 *
	 * <p>Find a detailed description of the matching algorithm in {@link #assertLinesMatch(List, List)}.
	 *
	 * <p>Note: An implementation of this method may consume all lines of both streams eagerly and
	 * delegate the evaluation to {@link #assertLinesMatch(List, List)}.
	 *
	 * @since 5.7
	 * @see #assertLinesMatch(List, List)
	 */
	public static void assertLinesMatch(Stream<String> expectedLines, Stream<String> actualLines) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines);
	}

	/**
	 * <em>Assert</em> that {@code expected} stream of {@linkplain String}s matches {@code actual}
	 * stream.
	 *
	 * <p>Find a detailed description of the matching algorithm in {@link #assertLinesMatch(List, List)}.
	 *
	 * <p>Fails with the supplied failure {@code message} and the generated message.
	 *
	 * <p>Note: An implementation of this method may consume all lines of both streams eagerly and
	 * delegate the evaluation to {@link #assertLinesMatch(List, List)}.
	 *
	 * @since 5.7
	 * @see #assertLinesMatch(List, List)
	 */
	public static void assertLinesMatch(Stream<String> expectedLines, Stream<String> actualLines, String message) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} stream of {@linkplain String}s matches {@code actual}
	 * stream.
	 *
	 * <p>Find a detailed description of the matching algorithm in {@link #assertLinesMatch(List, List)}.
	 *
	 * <p>If necessary, a custom failure message will be retrieved lazily from the supplied
	 * {@code messageSupplier}. Fails with the custom failure message prepended to
	 * a generated failure message describing the difference.
	 *
	 * <p>Note: An implementation of this method may consume all lines of both streams eagerly and
	 * delegate the evaluation to {@link #assertLinesMatch(List, List)}.
	 *
	 * @since 5.7
	 * @see #assertLinesMatch(List, List)
	 */
	public static void assertLinesMatch(Stream<String> expectedLines, Stream<String> actualLines,
			Supplier<String> messageSupplier) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines, messageSupplier);
	}

	// --- assertNotEquals -----------------------------------------------------

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, byte actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, Byte actual) {
		AssertNotEquals.assertNotEquals((Byte) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, byte actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Byte) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, Byte actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, byte actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, Byte actual, String message) {
		AssertNotEquals.assertNotEquals((Byte) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, byte actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Byte) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, Byte actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, byte actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(byte unexpected, Byte actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Byte) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, byte actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Byte) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Byte unexpected, Byte actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, short actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, Short actual) {
		AssertNotEquals.assertNotEquals((Short) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, short actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Short) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, Short actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, short actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, Short actual, String message) {
		AssertNotEquals.assertNotEquals((Short) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, short actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Short) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, Short actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, short actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(short unexpected, Short actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Short) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, short actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Short) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Short unexpected, Short actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, int actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, Integer actual) {
		AssertNotEquals.assertNotEquals((Integer) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, int actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Integer) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, Integer actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, int actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, Integer actual, String message) {
		AssertNotEquals.assertNotEquals((Integer) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, int actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Integer) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, Integer actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, int actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(int unexpected, Integer actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Integer) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, int actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Integer) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Integer unexpected, Integer actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, long actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, Long actual) {
		AssertNotEquals.assertNotEquals((Long) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, long actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Long) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, Long actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, long actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, Long actual, String message) {
		AssertNotEquals.assertNotEquals((Long) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, long actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Long) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, Long actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, long actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(long unexpected, Long actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Long) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, long actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Long) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Long unexpected, Long actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, Float actual) {
		AssertNotEquals.assertNotEquals((Float) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, float actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Float) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, Float actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, Float actual, String message) {
		AssertNotEquals.assertNotEquals((Float) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, float actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Float) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, Float actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, Float actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Float) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, float actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Float) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Float unexpected, Float actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual, float delta) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual, float delta, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Float#equals(Object)} and {@link Float#compare(float, float)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(float unexpected, float actual, float delta, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, Double actual) {
		AssertNotEquals.assertNotEquals((Double) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, double actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Double) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, Double actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, Double actual, String message) {
		AssertNotEquals.assertNotEquals((Double) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, double actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Double) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, Double actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, Double actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Double) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, double actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Double) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Double unexpected, Double actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual, double delta) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual, double delta, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal
	 * within the given {@code delta}.
	 *
	 * <p>Inequality imposed by this method is consistent with
	 * {@link Double#equals(Object)} and {@link Double#compare(double, double)}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(double unexpected, double actual, double delta,
			Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, char actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, Character actual) {
		AssertNotEquals.assertNotEquals((Character) unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, char actual) {
		AssertNotEquals.assertNotEquals(unexpected, (Character) actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, Character actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, char actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, Character actual, String message) {
		AssertNotEquals.assertNotEquals((Character) unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, char actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, (Character) actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, Character actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, char actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(char unexpected, Character actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals((Character) unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, char actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, (Character) actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	public static void assertNotEquals(Character unexpected, Character actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails if both are {@code null}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails if both are {@code null}.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are not equal.
	 *
	 * <p>Fails if both are {@code null}.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	// --- assertSame ----------------------------------------------------------

	/**
	 * <em>Assert</em> that the {@code expected} object and the {@code actual} object
	 * are the same object.
	 * <p>This method should only be used to assert <em>identity</em> between objects.
	 * To assert <em>equality</em> between two objects or two primitive values,
	 * use one of the {@code assertEquals(...)} methods instead &mdash; for example,
	 * use {@code assertEquals(999, 999)} instead of {@code assertSame(999, 999)}.
	 */
	public static void assertSame(Object expected, Object actual) {
		AssertSame.assertSame(expected, actual);
	}

	/**
	 * <em>Assert</em> that the {@code expected} object and the {@code actual} object
	 * are the same object.
	 * <p>This method should only be used to assert <em>identity</em> between objects.
	 * To assert <em>equality</em> between two objects or two primitive values,
	 * use one of the {@code assertEquals(...)} methods instead &mdash; for example,
	 * use {@code assertEquals(999, 999)} instead of {@code assertSame(999, 999)}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertSame(Object expected, Object actual, String message) {
		AssertSame.assertSame(expected, actual, message);
	}

	/**
	 * <em>Assert</em> that the {@code expected} object and the {@code actual} object
	 * are the same object.
	 * <p>This method should only be used to assert <em>identity</em> between objects.
	 * To assert <em>equality</em> between two objects or two primitive values,
	 * use one of the {@code assertEquals(...)} methods instead &mdash; for example,
	 * use {@code assertEquals(999, 999)} instead of {@code assertSame(999, 999)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied
	 * {@code messageSupplier}.
	 */
	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		AssertSame.assertSame(expected, actual, messageSupplier);
	}

	// --- assertNotSame -------------------------------------------------------

	/**
	 * <em>Assert</em> that the {@code unexpected} object and the {@code actual}
	 * object are not the same object.
	 * <p>This method should only be used to compare the <em>identity</em> of two
	 * objects. To assert that two objects or two primitive values are not
	 * <em>equal</em>, use one of the {@code assertNotEquals(...)} methods instead.
	 */
	public static void assertNotSame(Object unexpected, Object actual) {
		AssertNotSame.assertNotSame(unexpected, actual);
	}

	/**
	 * <em>Assert</em> that the {@code unexpected} object and the {@code actual}
	 * object are not the same object.
	 * <p>This method should only be used to compare the <em>identity</em> of two
	 * objects. To assert that two objects or two primitive values are not
	 * <em>equal</em>, use one of the {@code assertNotEquals(...)} methods instead.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, String message) {
		AssertNotSame.assertNotSame(unexpected, actual, message);
	}

	/**
	 * <em>Assert</em> that the {@code unexpected} object and the {@code actual}
	 * object are not the same object.
	 * <p>This method should only be used to compare the <em>identity</em> of two
	 * objects. To assert that two objects or two primitive values are not
	 * <em>equal</em>, use one of the {@code assertNotEquals(...)} methods instead.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied
	 * {@code messageSupplier}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		AssertNotSame.assertNotSame(unexpected, actual, messageSupplier);
	}

	// --- assertAll -----------------------------------------------------------

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Collection)
	 * @see #assertAll(String, Collection)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Stream)
	 */
	public static void assertAll(Executable... executables) throws MultipleFailuresError {
		AssertAll.assertAll(executables);
	}

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(Collection)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Collection)
	 * @see #assertAll(String, Stream)
	 */
	public static void assertAll(String heading, Executable... executables) throws MultipleFailuresError {
		AssertAll.assertAll(heading, executables);
	}

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(String, Collection)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Stream)
	 */
	public static void assertAll(Collection<Executable> executables) throws MultipleFailuresError {
		AssertAll.assertAll(executables);
	}

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Collection)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Stream)
	 */
	public static void assertAll(String heading, Collection<Executable> executables) throws MultipleFailuresError {
		AssertAll.assertAll(heading, executables);
	}

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Collection)
	 * @see #assertAll(String, Collection)
	 * @see #assertAll(String, Stream)
	 */
	public static void assertAll(Stream<Executable> executables) throws MultipleFailuresError {
		AssertAll.assertAll(executables);
	}

	/**
	 * <em>Assert</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>If any supplied {@link Executable} throws an exception (i.e., a {@link Throwable}
	 * or any subclass thereof), all remaining {@code executables} will still be executed,
	 * and all exceptions will be aggregated and reported in a {@link MultipleFailuresError}.
	 * In addition, all aggregated exceptions will be added as {@linkplain
	 * Throwable#addSuppressed(Throwable) suppressed exceptions} to the
	 * {@code MultipleFailuresError}. However, if an {@code executable} throws an
	 * <em>unrecoverable</em> exception &mdash; for example, an {@link OutOfMemoryError}
	 * &mdash; execution will halt immediately, and the unrecoverable exception will be
	 * rethrown <em>as is</em> but <em>masked</em> as an unchecked exception.
	 *
	 * <p>The supplied {@code heading} will be included in the message string for the
	 * {@code MultipleFailuresError}.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Collection)
	 * @see #assertAll(String, Collection)
	 * @see #assertAll(Stream)
	 */
	public static void assertAll(String heading, Stream<Executable> executables) throws MultipleFailuresError {
		AssertAll.assertAll(heading, executables);
	}

	// --- assert exceptions ---------------------------------------------------

	// --- executable ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable) {
		return AssertThrowsExactly.assertThrowsExactly(expectedType, executable);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * <p>Fails with the supplied failure {@code message}. Note that the supplied
	 * {@code message} is <strong>not</strong> the expected message of the thrown
	 * exception. To assert the expected message of the thrown exception, you must
	 * use a separate, subsequent assertion against the exception returned from
	 * this method.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable,
			String message) {
		return AssertThrowsExactly.assertThrowsExactly(expectedType, executable, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}. Note that the failure message is
	 * <strong>not</strong> the expected message of the thrown exception. To
	 * assert the expected message of the thrown exception, you must use a
	 * separate, subsequent assertion against the exception returned from this
	 * method.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {
		return AssertThrowsExactly.assertThrowsExactly(expectedType, executable, messageSupplier);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType} and return the exception.
	 *
	 * <p>The assertion passes if the thrown exception type is the same as
	 * {@code expectedType} or a subtype thereof. To check for the exact thrown
	 * type use {@link #assertThrowsExactly(Class, Executable) assertThrowsExactly}.
	 * If no exception is thrown, or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @see #assertThrowsExactly(Class, Executable)
	 */
	public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return AssertThrows.assertThrows(expectedType, executable);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType} and return the exception.
	 *
	 * <p>The assertion passes if the thrown exception type is the same as
	 * {@code expectedType} or a subtype thereof. To check for the exact thrown
	 * type use {@link #assertThrowsExactly(Class, Executable, String) assertThrowsExactly}.
	 * If no exception is thrown, or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * <p>Fails with the supplied failure {@code message}. Note that the supplied
	 * {@code message} is <strong>not</strong> the expected message of the thrown
	 * exception. To assert the expected message of the thrown exception, you must
	 * use a separate, subsequent assertion against the exception returned from
	 * this method.
	 *
	 * @see #assertThrowsExactly(Class, Executable, String)
	 */
	public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return AssertThrows.assertThrows(expectedType, executable, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType} and return the exception.
	 *
	 * <p>The assertion passes if the thrown exception type is the same as
	 * {@code expectedType} or a subtype thereof. To check for the exact thrown
	 * type use {@link #assertThrowsExactly(Class, Executable, Supplier) assertThrowsExactly}.
	 * If no exception is thrown, or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}. Note that the failure message is
	 * <strong>not</strong> the expected message of the thrown exception. To
	 * assert the expected message of the thrown exception, you must use a
	 * separate, subsequent assertion against the exception returned from this
	 * method.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @see #assertThrowsExactly(Class, Executable, Supplier)
	 */
	public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {
		return AssertThrows.assertThrows(expectedType, executable, messageSupplier);
	}

	// --- executable ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static void assertDoesNotThrow(Executable executable) {
		AssertDoesNotThrow.assertDoesNotThrow(executable);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static void assertDoesNotThrow(Executable executable, String message) {
		AssertDoesNotThrow.assertDoesNotThrow(executable, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static void assertDoesNotThrow(Executable executable, Supplier<String> messageSupplier) {
		AssertDoesNotThrow.assertDoesNotThrow(executable, messageSupplier);
	}

	// --- supplier ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <p>If the assertion passes, the {@code supplier}'s result will be returned.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static <T> T assertDoesNotThrow(ThrowingSupplier<T> supplier) {
		return AssertDoesNotThrow.assertDoesNotThrow(supplier);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <p>If the assertion passes, the {@code supplier}'s result will be returned.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static <T> T assertDoesNotThrow(ThrowingSupplier<T> supplier, String message) {
		return AssertDoesNotThrow.assertDoesNotThrow(supplier, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier} does
	 * <em>not</em> throw any kind of {@linkplain Throwable exception}.
	 *
	 * <p>If the assertion passes, the {@code supplier}'s result will be returned.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * <h4>Usage Note</h4>
	 * <p>Although any exception thrown from a test method will cause the test
	 * to <em>fail</em>, there are certain use cases where it can be beneficial
	 * to explicitly assert that an exception is not thrown for a given code
	 * block within a test method.
	 *
	 * @since 5.2
	 */
	@API(status = STABLE, since = "5.2")
	public static <T> T assertDoesNotThrow(ThrowingSupplier<T> supplier, Supplier<String> messageSupplier) {
		return AssertDoesNotThrow.assertDoesNotThrow(supplier, messageSupplier);
	}

	// --- assertTimeout -------------------------------------------------------

	// --- executable ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 */
	public static void assertTimeout(Duration timeout, Executable executable) {
		AssertTimeout.assertTimeout(timeout, executable);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, String message) {
		AssertTimeout.assertTimeout(timeout, executable, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		AssertTimeout.assertTimeout(timeout, executable, messageSupplier);
	}

	// --- supplier ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier) {
		return AssertTimeout.assertTimeout(timeout, supplier);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return AssertTimeout.assertTimeout(timeout, supplier, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return AssertTimeout.assertTimeout(timeout, supplier, messageSupplier);
	}

	// --- executable - preemptively ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, executable);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, String)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, executable, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable,
			Supplier<String> messageSupplier) {
		AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, executable, messageSupplier);
	}

	// --- supplier - preemptively ---

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier) {
		return AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, supplier);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, String)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, supplier, message);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, supplier, messageSupplier);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>See the {@linkplain Assertions Preemptive Timeouts} section of the
	 * class-level Javadoc for further details.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>In the case the assertion does not pass, the supplied
	 * {@link TimeoutFailureFactory} is invoked to create an exception which is
	 * then thrown.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	@API(status = INTERNAL, since = "5.9.1")
	public static <T, E extends Throwable> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier, TimeoutFailureFactory<E> failureFactory) throws E {
		return AssertTimeoutPreemptively.assertTimeoutPreemptively(timeout, supplier, messageSupplier, failureFactory);
	}

	// --- assertInstanceOf ----------------------------------------------------

	/**
	 * <em>Assert</em> that the supplied {@code actualValue} is an instance of the
	 * {@code expectedType}.
	 *
	 * <p>Like the {@code instanceof} operator a {@code null} value is not
	 * considered to be of the {@code expectedType} and does not pass the assertion.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue) {
		return AssertInstanceOf.assertInstanceOf(expectedType, actualValue);
	}

	/**
	 * <em>Assert</em> that the supplied {@code actualValue} is an instance of the
	 * {@code expectedType}.
	 *
	 * <p>Like the {@code instanceof} operator a {@code null} value is not
	 * considered to be of the {@code expectedType} and does not pass the assertion.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue, String message) {
		return AssertInstanceOf.assertInstanceOf(expectedType, actualValue, message);
	}

	/**
	 * <em>Assert</em> that the supplied {@code actualValue} is an instance of the
	 * {@code expectedType}.
	 *
	 * <p>Like the {@code instanceof} operator a {@code null} value is not
	 * considered to be of the {@code expectedType} and does not pass the assertion.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @since 5.8
	 */
	@API(status = STABLE, since = "5.10")
	public static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue, Supplier<String> messageSupplier) {
		return AssertInstanceOf.assertInstanceOf(expectedType, actualValue, messageSupplier);
	}

	/**
	 * Factory for timeout failures.
	 *
	 * @param <T> The type of error or exception created
	 * @since 5.9.1
	 * @see Assertions#assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier, TimeoutFailureFactory)
	 */
	@API(status = INTERNAL, since = "5.9.1")
	public interface TimeoutFailureFactory<T extends Throwable> {

		/**
		 * Create a failure for the given timeout, message, and cause.
		 *
		 * @return timeout failure; never {@code null}
		 */
		T createTimeoutFailure(Duration timeout, Supplier<String> messageSupplier, Throwable cause, Thread testThread);
	}
}
