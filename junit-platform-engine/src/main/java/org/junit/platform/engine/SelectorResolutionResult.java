/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code SelectorResolutionResult} encapsulates the result of resolving a
 * {@link DiscoverySelector} by a {@link TestEngine}.
 *
 * <p>A {@code SelectorResolutionResult} consists of a mandatory
 * {@link #getStatus() Status} and an optional {@link #getThrowable() Throwable}.
 *
 * @since 1.6
 */
@API(status = STABLE, since = "1.10")
public class SelectorResolutionResult {

	/**
	 * Status of resolving a {@link DiscoverySelector}.
	 */
	public enum Status {

		/**
		 * Indicates that the {@link TestEngine} has successfully resolved the
		 * selector.
		 */
		RESOLVED,

		/**
		 * Indicates that the {@link TestEngine} was unable to resolve the
		 * selector.
		 */
		UNRESOLVED,

		/**
		 * Indicates that the {@link TestEngine} has encountered an error while
		 * resolving the selector.
		 */
		FAILED

	}

	private static final SelectorResolutionResult RESOLVED_RESULT = new SelectorResolutionResult(Status.RESOLVED, null);
	private static final SelectorResolutionResult UNRESOLVED_RESULT = new SelectorResolutionResult(Status.UNRESOLVED,
		null);

	/**
	 * Create a {@code SelectorResolutionResult} for a <em>resolved</em>
	 * selector.
	 * @return the {@code SelectorResolutionResult}; never {@code null}
	 */
	public static SelectorResolutionResult resolved() {
		return RESOLVED_RESULT;
	}

	/**
	 * Create a {@code SelectorResolutionResult} for an <em>unresolved</em>
	 * selector.
	 * @return the {@code SelectorResolutionResult}; never {@code null}
	 */
	public static SelectorResolutionResult unresolved() {
		return UNRESOLVED_RESULT;
	}

	/**
	 * Create a {@code SelectorResolutionResult} for a <em>failed</em>
	 * selector resolution.
	 * @return the {@code SelectorResolutionResult}; never {@code null}
	 */
	public static SelectorResolutionResult failed(Throwable throwable) {
		return new SelectorResolutionResult(Status.FAILED, throwable);
	}

	private final Status status;
	private final Throwable throwable;

	private SelectorResolutionResult(Status status, Throwable throwable) {
		this.status = status;
		this.throwable = throwable;
	}

	/**
	 * Get the {@linkplain Status status} of this result.
	 *
	 * @return the status; never {@code null}
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Get the throwable that caused this result, if available.
	 *
	 * @return an {@code Optional} containing the throwable; never {@code null}
	 * but potentially empty
	 */
	public Optional<Throwable> getThrowable() {
		return Optional.ofNullable(throwable);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("status", status)
				.append("throwable", throwable)
				.toString();
		// @formatter:on
	}

}
