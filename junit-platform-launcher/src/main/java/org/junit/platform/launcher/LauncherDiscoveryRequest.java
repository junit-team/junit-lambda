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

import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * {@code LauncherDiscoveryRequest} extends the {@link EngineDiscoveryRequest} API
 * with additional filters that are applied by the {@link Launcher} itself.
 *
 * <p>Specifically, a {@code LauncherDiscoveryRequest} contains the following.
 *
 * <ul>
 * <li>{@linkplain EngineFilter Engine Filters}: filters that are applied before
 * each {@code TestEngine} is executed. All of them have to include an engine for it
 * to contribute to the test plan.</li>
 * <li>{@linkplain ConfigurationParameters Configuration Parameters}: configuration
 * parameters that can be used to influence the discovery process</li>
 * <li>{@linkplain DiscoverySelector Discovery Selectors}: components that select
 * resources that a {@code TestEngine} can use to discover tests</li>
 * <li>{@linkplain DiscoveryFilter Discovery Filters}: filters that should be applied
 * by {@code TestEngines} during test discovery. All of them have to include a
 * resource for it to end up in the test plan.</li>
 * <li>{@linkplain PostDiscoveryFilter Post-Discovery Filters}: filters that will be
 * applied by the {@code Launcher} after {@code TestEngines} have performed test
 * discovery. All of them have to include a {@code TestDescriptor} for it to end up
 * in the test plan.</li>
 * </ul>
 *
 * @since 1.0
 * @see org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
 * @see EngineDiscoveryRequest
 * @see EngineFilter
 * @see ConfigurationParameters
 * @see DiscoverySelector
 * @see DiscoveryFilter
 * @see PostDiscoveryFilter
 * @see #getEngineFilters()
 * @see #getPostDiscoveryFilters()
 */
@API(status = STABLE, since = "1.0")
public interface LauncherDiscoveryRequest extends EngineDiscoveryRequest {

	/**
	 * Get the {@code EngineFilters} for this request.
	 *
	 * <p>The returned filters are to be combined using AND semantics, i.e. all
	 * of them have to include an engine for it to contribute to the test plan.
	 *
	 * @return the list of {@code EngineFilters} for this request; never
	 * {@code null} but potentially empty
	 */
	List<EngineFilter> getEngineFilters();

	/**
	 * Get the {@code PostDiscoveryFilters} for this request.
	 *
	 * <p>The returned filters are to be combined using AND semantics, i.e. all
	 * of them have to include a {@code TestDescriptor} for it to end up in the
	 * test plan.
	 *
	 * @return the list of {@code PostDiscoveryFilters} for this request; never
	 * {@code null} but potentially empty
	 */
	List<PostDiscoveryFilter> getPostDiscoveryFilters();

	/**
	 * Get the {@link LauncherDiscoveryListener} for this request.
	 *
	 * <p>The default implementation returns a no-op listener that ignores all
	 * calls so that engines that call this methods can be used with an earlier
	 * version of the JUnit Platform that did not yet include it.
	 *
	 * @return the discovery listener; never {@code null}
	 * @since 1.6
	 */
	@API(status = STABLE, since = "1.10")
	@Override
	default LauncherDiscoveryListener getDiscoveryListener() {
		return LauncherDiscoveryListener.NOOP;
	}

	NamespacedHierarchicalStore<Namespace> getStore();

}
