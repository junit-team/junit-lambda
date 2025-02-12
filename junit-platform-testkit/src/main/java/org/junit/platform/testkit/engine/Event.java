/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.FileEntry;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * {@code Event} represents a single event fired during execution of
 * a test plan on the JUnit Platform.
 *
 * @since 1.4
 * @see EventType
 */
@API(status = MAINTAINED, since = "1.7")
public class Event {

	// --- Factories -----------------------------------------------------------

	/**
	 * Create an {@code Event} for a reporting entry published for the
	 * supplied {@link TestDescriptor} and {@link ReportEntry}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param entry the {@code ReportEntry} that was published; never {@code null}
	 * @return the newly created {@code Event}
	 * @see EventType#REPORTING_ENTRY_PUBLISHED
	 */
	public static Event reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		Preconditions.notNull(entry, "ReportEntry must not be null");
		return new Event(EventType.REPORTING_ENTRY_PUBLISHED, testDescriptor, entry);
	}

	/**
	 * Create an {@code Event} for a published file for the supplied
	 * {@link TestDescriptor} and {@link FileEntry}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param file the {@code FileEntry} that was published; never {@code null}
	 * @return the newly created {@code Event}
	 * @since 1.12
	 * @see EventType#FILE_ENTRY_PUBLISHED
	 */
	@API(status = EXPERIMENTAL, since = "1.12")
	public static Event fileEntryPublished(TestDescriptor testDescriptor, FileEntry file) {
		Preconditions.notNull(file, "FileEntry must not be null");
		return new Event(EventType.FILE_ENTRY_PUBLISHED, testDescriptor, file);
	}

	/**
	 * Create an {@code Event} for the dynamic registration of the
	 * supplied {@link TestDescriptor}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @return the newly created {@code Event}
	 * @see EventType#DYNAMIC_TEST_REGISTERED
	 */
	public static Event dynamicTestRegistered(TestDescriptor testDescriptor) {
		return new Event(EventType.DYNAMIC_TEST_REGISTERED, testDescriptor, null);
	}

	/**
	 * Create a <em>skipped</em> {@code Event} for the supplied
	 * {@link TestDescriptor} and {@code reason}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param reason the reason the execution was skipped; may be {@code null}
	 * @return the newly created {@code Event}
	 * @see EventType#SKIPPED
	 */
	public static Event executionSkipped(TestDescriptor testDescriptor, String reason) {
		return new Event(EventType.SKIPPED, testDescriptor, reason);
	}

	/**
	 * Create a <em>started</em> {@code Event} for the supplied
	 * {@link TestDescriptor}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @return the newly created {@code Event}
	 * @see EventType#STARTED
	 */
	public static Event executionStarted(TestDescriptor testDescriptor) {
		return new Event(EventType.STARTED, testDescriptor, null);
	}

	/**
	 * Create a <em>finished</em> {@code Event} for the supplied
	 * {@link TestDescriptor} and {@link TestExecutionResult}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param result the {@code TestExecutionResult} for the supplied
	 * {@code TestDescriptor}; never {@code null}
	 * @return the newly created {@code Event}
	 * @see EventType#FINISHED
	 */
	public static Event executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		Preconditions.notNull(result, "Event of type FINISHED cannot have a null TestExecutionResult");
		return new Event(EventType.FINISHED, testDescriptor, result);
	}

	// --- Predicates ----------------------------------------------------------

	/**
	 * Create a {@link Predicate} for {@linkplain Event events} whose payload
	 * types match the supplied {@code payloadType} and whose payloads match the
	 * supplied {@code payloadPredicate}.
	 *
	 * @param payloadType the required payload type
	 * @param payloadPredicate a {@code Predicate} to match against payloads
	 * @return the resulting {@code Predicate}
	 */
	public static <T> Predicate<Event> byPayload(Class<T> payloadType, Predicate<? super T> payloadPredicate) {
		return event -> event.getPayload(payloadType).filter(payloadPredicate).isPresent();
	}

	/**
	 * Create a {@link Predicate} for {@linkplain Event events} whose
	 * {@linkplain EventType event types} match the supplied {@code type}.
	 *
	 * @param type the type to match against
	 * @return the resulting {@code Predicate}
	 */
	public static Predicate<Event> byType(EventType type) {
		return event -> event.type.equals(type);
	}

	/**
	 * Create a {@link Predicate} for {@linkplain Event events} whose
	 * {@link TestDescriptor TestDescriptors} match the supplied
	 * {@code testDescriptorPredicate}.
	 *
	 * @param testDescriptorPredicate a {@code Predicate} to match against test
	 * descriptors
	 * @return the resulting {@link Predicate}
	 */
	public static Predicate<Event> byTestDescriptor(Predicate<? super TestDescriptor> testDescriptorPredicate) {
		return where(Event::getTestDescriptor, testDescriptorPredicate);
	}

	// -------------------------------------------------------------------------

	private final Instant timestamp = Instant.now();
	private final EventType type;
	private final TestDescriptor testDescriptor;
	private final Object payload;

	/**
	 * Construct an {@code Event} with the supplied arguments.
	 *
	 * @param type the type of the event; never {@code null}
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param payload the generic payload associated with the event; may be {@code null}
	 */
	private Event(EventType type, TestDescriptor testDescriptor, Object payload) {
		this.type = Preconditions.notNull(type, "EventType must not be null");
		this.testDescriptor = Preconditions.notNull(testDescriptor, "TestDescriptor must not be null");
		this.payload = payload;
	}

	/**
	 * Get the type of this {@code Event}.
	 *
	 * @return the event type; never {@code null}
	 * @see EventType
	 */
	public EventType getType() {
		return this.type;
	}

	/**
	 * Get the {@link TestDescriptor} associated with this {@code Event}.
	 *
	 * @return the {@code TestDescriptor}; never {@code null}
	 */
	public TestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	/**
	 * Get the {@link Instant} when this {@code Event} occurred.
	 *
	 * @return the {@code Instant} when this {@code Event} occurred;
	 * never {@code null}
	 */
	public Instant getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Get the payload, if available.
	 *
	 * @return an {@code Optional} containing the payload; never {@code null}
	 * but potentially empty
	 * @see #getPayload(Class)
	 * @see #getRequiredPayload(Class)
	 */
	public Optional<Object> getPayload() {
		return Optional.ofNullable(this.payload);
	}

	/**
	 * Get the payload of the expected type, if available.
	 *
	 * <p>This is a convenience method that automatically casts the payload to
	 * the expected type. If the payload is not present or is not of the expected
	 * type, this method will return {@link Optional#empty()}.
	 *
	 * @param payloadType the expected payload type; never {@code null}
	 * @return an {@code Optional} containing the payload; never {@code null}
	 * but potentially empty
	 * @see #getPayload()
	 * @see #getRequiredPayload(Class)
	 */
	public <T> Optional<T> getPayload(Class<T> payloadType) {
		Preconditions.notNull(payloadType, "Payload type must not be null");
		return getPayload().filter(payloadType::isInstance).map(payloadType::cast);
	}

	/**
	 * Get the payload of the required type.
	 *
	 * <p>This is a convenience method that automatically casts the payload to
	 * the required type. If the payload is not present or is not of the expected
	 * type, this method will throw an {@link IllegalArgumentException}.
	 *
	 * @param payloadType the required payload type; never {@code null}
	 * @return the payload
	 * @throws IllegalArgumentException if the payload is of a different type
	 * or is not present
	 * @see #getPayload()
	 * @see #getPayload(Class)
	 */
	public <T> T getRequiredPayload(Class<T> payloadType) throws IllegalArgumentException {
		return getPayload(payloadType).orElseThrow(//
			() -> new IllegalArgumentException("Event does not contain a payload of type " + payloadType.getName()));
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("type", this.type)
				.append("testDescriptor", this.testDescriptor)
				.append("timestamp", this.timestamp)
				.append("payload", this.payload)
				.toString();
		// @formatter:on
	}

}
