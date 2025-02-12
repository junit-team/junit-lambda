/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code EngineDiscoveryResult} encapsulates the result of test discovery by a
 * {@link org.junit.platform.engine.TestEngine}.
 *
 * <p>A {@code EngineDiscoveryResult} consists of a mandatory
 * {@link #getStatus() Status} and an optional {@link #getThrowable() Throwable}.
 *
 * @since 1.6
 */
@API(status = STABLE, since = "1.10")
public class EngineDiscoveryResult {

	/**
	 * Status of test discovery by a
	 * {@link org.junit.platform.engine.TestEngine}.
	 */
	public enum Status {

		/**
		 * Indicates that test discovery was <em>successful</em>.
		 */
		SUCCESSFUL,

		/**
		 * Indicates that test discovery has <em>failed</em>.
		 */
		FAILED

	}

	private static final EngineDiscoveryResult SUCCESSFUL_RESULT = new EngineDiscoveryResult(Status.SUCCESSFUL, null);

	/**
	 * Create a {@code EngineDiscoveryResult} for a <em>successful</em> test
	 * discovery.
	 * @return the {@code EngineDiscoveryResult}; never {@code null}
	 */
	public static EngineDiscoveryResult successful() {
		return SUCCESSFUL_RESULT;
	}

	/**
	 * Create a {@code EngineDiscoveryResult} for a <em>failed</em> test
	 * discovery.
	 *
	 * @param throwable the throwable that caused the failed discovery; may be
	 * {@code null}
	 * @return the {@code EngineDiscoveryResult}; never {@code null}
	 */
	public static EngineDiscoveryResult failed(Throwable throwable) {
		return new EngineDiscoveryResult(Status.FAILED, throwable);
	}

	private final Status status;
	private final Throwable throwable;

	private EngineDiscoveryResult(Status status, Throwable throwable) {
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
