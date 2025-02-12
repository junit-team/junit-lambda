/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.vintage.engine.Constants.PARALLEL_CLASS_EXECUTION;
import static org.junit.vintage.engine.Constants.PARALLEL_EXECUTION_ENABLED;
import static org.junit.vintage.engine.Constants.PARALLEL_METHOD_EXECUTION;
import static org.junit.vintage.engine.Constants.PARALLEL_POOL_SIZE;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_TEST;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelClassesTestCase.FirstClassTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelClassesTestCase.SecondClassTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelClassesTestCase.ThirdClassTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelMethodsTestCase.FirstMethodTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelMethodsTestCase.SecondMethodTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelMethodsTestCase.ThirdMethodTestCase;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;
import org.junit.vintage.engine.VintageTestEngine;
import org.junit.vintage.engine.samples.junit4.JUnit4ParallelClassesTestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4ParallelMethodsTestCase;

class ParallelExecutionIntegrationTests {

	@Test
	void executesTestClassesInParallel(TestReporter reporter) {
		JUnit4ParallelClassesTestCase.AbstractBlockingTestCase.threadNames.clear();
		JUnit4ParallelClassesTestCase.AbstractBlockingTestCase.countDownLatch = new CountDownLatch(3);

		var events = executeInParallelSuccessfully(3, true, false, FirstClassTestCase.class, SecondClassTestCase.class,
			ThirdClassTestCase.class).list();

		var startedTimestamps = getTimestampsFor(events, event(container(SEGMENT_TYPE_RUNNER), started()));
		var finishedTimestamps = getTimestampsFor(events,
			event(container(SEGMENT_TYPE_RUNNER), finishedSuccessfully()));
		var threadNames = new HashSet<>(JUnit4ParallelClassesTestCase.AbstractBlockingTestCase.threadNames);

		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().noneMatch(
			finishedTimestamp -> finishedTimestamp.isBefore(startTimestamp)));
		assertThat(threadNames).hasSize(3);
	}

	@Test
	void executesTestMethodsInParallel(TestReporter reporter) {
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames.clear();
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.countDownLatch = new CountDownLatch(3);

		var events = executeInParallelSuccessfully(3, false, true, FirstMethodTestCase.class).list();

		var startedTimestamps = getTimestampsFor(events, event(test(SEGMENT_TYPE_TEST), started()));
		var finishedTimestamps = getTimestampsFor(events, event(test(SEGMENT_TYPE_TEST), finishedSuccessfully()));
		var threadNames = new HashSet<>(JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames);

		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().noneMatch(
			finishedTimestamp -> finishedTimestamp.isBefore(startTimestamp)));
		assertThat(threadNames).hasSize(3);
	}

	@Test
	void executesTestClassesAndMethodsInParallel(TestReporter reporter) {
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames.clear();
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.countDownLatch = new CountDownLatch(9);

		var events = executeInParallelSuccessfully(3, true, true, FirstMethodTestCase.class, SecondMethodTestCase.class,
			ThirdMethodTestCase.class).list();

		var startedClassesTimestamps = getTimestampsFor(events, event(container(SEGMENT_TYPE_RUNNER), started()));
		var finishedClassesTimestamps = getTimestampsFor(events,
			event(container(SEGMENT_TYPE_RUNNER), finishedSuccessfully()));
		var startedMethodsTimestamps = getTimestampsFor(events, event(test(SEGMENT_TYPE_TEST), started()));
		var finishedMethodsTimestamps = getTimestampsFor(events,
			event(test(SEGMENT_TYPE_TEST), finishedSuccessfully()));

		var threadNames = new HashSet<>(JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames);

		reporter.publishEntry("startedClassesTimestamps", startedClassesTimestamps.toString());
		reporter.publishEntry("finishedClassesTimestamps", finishedClassesTimestamps.toString());
		reporter.publishEntry("startedMethodsTimestamps", startedMethodsTimestamps.toString());
		reporter.publishEntry("finishedMethodsTimestamps", finishedMethodsTimestamps.toString());

		assertThat(startedClassesTimestamps).hasSize(3);
		assertThat(finishedClassesTimestamps).hasSize(3);
		assertThat(startedMethodsTimestamps).hasSize(9);
		assertThat(finishedMethodsTimestamps).hasSize(9);

		assertThat(threadNames).hasSize(3);
	}

	@Test
	void executesInParallelWhenNoScopeIsDefined(@TrackLogRecords LogRecordListener listener) {
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames.clear();
		JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.countDownLatch = new CountDownLatch(9);
		execute(3, false, false, FirstMethodTestCase.class, SecondMethodTestCase.class, ThirdMethodTestCase.class);

		// @formatter:off
		assertTrue(listener.stream(Level.WARNING)
				.map(LogRecord::getMessage)
				.anyMatch(m -> m.startsWith(
					"Parallel execution is enabled but no scope is defined. Falling back to sequential execution.")));
		// @formatter:on

		var threadNames = new HashSet<>(JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase.threadNames);
		assertThat(threadNames).hasSize(1);
	}

	@Test
	void executesInParallelWhenInvalidPoolSizeIsDefined(@TrackLogRecords LogRecordListener listener) {
		execute(-1, true, true, FirstMethodTestCase.class, SecondMethodTestCase.class, ThirdMethodTestCase.class);

		// @formatter:off
		assertTrue(listener.stream(Level.WARNING)
				.map(LogRecord::getMessage)
				.anyMatch(m -> m.startsWith("Invalid value for parallel pool size: -1")));
		// @formatter:on
	}

	private List<Instant> getTimestampsFor(List<Event> events, Condition<Event> condition) {
		// @formatter:off
		return events.stream()
				.filter(condition::matches)
				.map(Event::getTimestamp)
				.toList();
		// @formatter:on
	}

	private Events executeInParallelSuccessfully(int poolSize, boolean parallelClasses, boolean parallelMethods,
			Class<?>... testClasses) {
		var events = execute(poolSize, parallelClasses, parallelMethods, testClasses).allEvents();
		try {
			return events.assertStatistics(it -> it.failed(0));
		}
		catch (AssertionError error) {
			events.debug();
			throw error;
		}
	}

	private static EngineExecutionResults execute(int poolSize, boolean parallelClasses, boolean parallelMethods,
			Class<?>... testClass) {
		return EngineTestKit.execute(new VintageTestEngine(),
			request(poolSize, parallelClasses, parallelMethods, testClass));
	}

	private static LauncherDiscoveryRequest request(int poolSize, boolean parallelClasses, boolean parallelMethods,
			Class<?>... testClasses) {
		var classSelectors = Arrays.stream(testClasses) //
				.map(DiscoverySelectors::selectClass) //
				.toArray(ClassSelector[]::new);

		return LauncherDiscoveryRequestBuilder.request() //
				.selectors(classSelectors) //
				.configurationParameter(PARALLEL_EXECUTION_ENABLED, String.valueOf(true)) //
				.configurationParameter(PARALLEL_POOL_SIZE, String.valueOf(poolSize)) //
				.configurationParameter(PARALLEL_CLASS_EXECUTION, String.valueOf(parallelClasses)) //
				.configurationParameter(PARALLEL_METHOD_EXECUTION, String.valueOf(parallelMethods)) //
				.build();
	}

}
