/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class JUnit4ParallelMethodsTestCase {

	public static class AbstractBlockingTestCase {

		public static final Set<String> threadNames = ConcurrentHashMap.newKeySet();
		public static CountDownLatch countDownLatch;

		@Rule
		public final TestWatcher testWatcher = new TestWatcher() {
			@Override
			protected void starting(Description description) {
				AbstractBlockingTestCase.threadNames.add(Thread.currentThread().getName());
			}
		};

		@Test
		public void fistTest() throws Exception {
			countDownAndBlock(countDownLatch);
		}

		@Test
		public void secondTest() throws Exception {
			countDownAndBlock(countDownLatch);
		}

		@Test
		public void thirdTest() throws Exception {
			countDownAndBlock(countDownLatch);
		}

		@SuppressWarnings("ResultOfMethodCallIgnored")
		private static void countDownAndBlock(CountDownLatch countDownLatch) throws InterruptedException {
			countDownLatch.countDown();
			countDownLatch.await(estimateSimulatedTestDurationInMilliseconds(), MILLISECONDS);
		}

		private static long estimateSimulatedTestDurationInMilliseconds() {
			var runningInCi = Boolean.parseBoolean(System.getenv("CI"));
			return runningInCi ? 1000 : 100;
		}
	}

	public static class FirstMethodTestCase extends JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase {
	}

	public static class SecondMethodTestCase extends JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase {
	}

	public static class ThirdMethodTestCase extends JUnit4ParallelMethodsTestCase.AbstractBlockingTestCase {
	}
}
