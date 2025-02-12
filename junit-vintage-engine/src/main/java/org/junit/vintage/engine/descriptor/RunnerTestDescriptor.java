/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static java.util.Collections.singletonList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.RunnerScheduler;

/**
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public class RunnerTestDescriptor extends VintageTestDescriptor {

	private static final Logger logger = LoggerFactory.getLogger(RunnerTestDescriptor.class);

	private final Set<Description> rejectedExclusions = new HashSet<>();
	private Runner runner;
	private final boolean ignored;
	private boolean wasFiltered;
	private List<Filter> filters = new ArrayList<>();

	public RunnerTestDescriptor(UniqueId uniqueId, Class<?> testClass, Runner runner, boolean ignored) {
		super(uniqueId, runner.getDescription(), testClass.getSimpleName(), ClassSource.from(testClass));
		this.runner = runner;
		this.ignored = ignored;
	}

	@Override
	public String getLegacyReportingName() {
		return getSource().map(source -> ((ClassSource) source).getClassName()) //
				.orElseThrow(() -> new JUnitException("source should have been present"));
	}

	public Request toRequest() {
		return new RunnerRequest(this.runner);
	}

	@Override
	protected boolean tryToExcludeFromRunner(Description description) {
		boolean excluded = tryToFilterRunner(description);
		if (excluded) {
			wasFiltered = true;
		}
		else {
			rejectedExclusions.add(description);
		}
		return excluded;
	}

	private boolean tryToFilterRunner(Description description) {
		if (runner instanceof Filterable) {
			ExcludeDescriptionFilter filter = new ExcludeDescriptionFilter(description);
			try {
				((Filterable) runner).filter(filter);
			}
			catch (NoTestsRemainException ignore) {
				// it's safe to ignore this exception because childless TestDescriptors will get pruned
			}
			return filter.wasSuccessful();
		}
		return false;
	}

	@Override
	protected boolean canBeRemovedFromHierarchy() {
		return true;
	}

	@Override
	public void prune() {
		if (wasFiltered) {
			// filtering the runner may render intermediate Descriptions obsolete
			// (e.g. test classes without any remaining children in a suite)
			pruneDescriptorsForObsoleteDescriptions(singletonList(runner.getDescription()));
		}
		if (rejectedExclusions.isEmpty()) {
			super.prune();
		}
		else if (rejectedExclusions.containsAll(getDescription().getChildren())) {
			// since the Runner was asked to remove all of its direct children,
			// it's safe to remove it entirely
			removeFromHierarchy();
		}
		else {
			logIncompleteFiltering();
		}
	}

	private void logIncompleteFiltering() {
		if (runner instanceof Filterable) {
			logger.warn(() -> "Runner " + getRunnerToReport().getClass().getName() //
					+ " (used on class " + getLegacyReportingName() + ") was not able to satisfy all filter requests.");
		}
		else {
			warnAboutUnfilterableRunner();
		}
	}

	private void warnAboutUnfilterableRunner() {
		logger.warn(() -> "Runner " + getRunnerToReport().getClass().getName() //
				+ " (used on class " + getLegacyReportingName() + ") does not support filtering" //
				+ " and will therefore be run completely.");
	}

	public Optional<List<Filter>> getFilters() {
		return Optional.ofNullable(filters);
	}

	public void clearFilters() {
		this.filters = null;
	}

	public void applyFilters(Consumer<RunnerTestDescriptor> childrenCreator) {
		if (filters != null && !filters.isEmpty()) {
			if (runner instanceof Filterable) {
				this.runner = toRequest().filterWith(new OrFilter(filters)).getRunner();
				this.description = runner.getDescription();
				this.children.clear();
				childrenCreator.accept(this);
			}
			else {
				warnAboutUnfilterableRunner();
			}
		}
		clearFilters();
	}

	private Runner getRunnerToReport() {
		return (runner instanceof RunnerDecorator) ? ((RunnerDecorator) runner).getDecoratedRunner() : runner;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setExecutorService(ExecutorService executorService) {
		Runner runner = getRunnerToReport();
		if (runner instanceof ParentRunner) {
			((ParentRunner<?>) runner).setScheduler(new RunnerScheduler() {

				private final List<Future<?>> futures = new CopyOnWriteArrayList<>();

				@Override
				public void schedule(Runnable childStatement) {
					futures.add(executorService.submit(childStatement));
				}

				@Override
				public void finished() {
					ThrowableCollector collector = new OpenTest4JAwareThrowableCollector();
					AtomicBoolean wasInterrupted = new AtomicBoolean(false);
					for (Future<?> future : futures) {
						collector.execute(() -> {
							// We're calling `Future.get()` individually to allow for work stealing
							// in case `ExecutorService` is a `ForkJoinPool`
							try {
								future.get();
							}
							catch (ExecutionException e) {
								throw e.getCause();
							}
							catch (InterruptedException e) {
								wasInterrupted.set(true);
							}
						});
					}
					collector.assertEmpty();
					if (wasInterrupted.get()) {
						logger.warn(() -> "Interrupted while waiting for runner to finish");
						Thread.currentThread().interrupt();
					}
				}
			});
		}
	}

	private static class ExcludeDescriptionFilter extends Filter {

		private final Description description;
		private boolean successful;

		ExcludeDescriptionFilter(Description description) {
			this.description = description;
		}

		@Override
		public boolean shouldRun(Description description) {
			if (this.description.equals(description)) {
				successful = true;
				return false;
			}
			return true;
		}

		@Override
		public String describe() {
			return "exclude " + description;
		}

		boolean wasSuccessful() {
			return successful;
		}
	}
}
