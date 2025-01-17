/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Collection of utilities for working with {@link ServiceLoader}.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.11
 */
@API(status = Status.INTERNAL, since = "5.11")
public class ServiceLoaderUtils {

	private ServiceLoaderUtils() {
		/* no-op */
	}

	/**
	 * Filters the supplied service loader using the supplied predicate.
	 *
	 * @param <T> the type of the service
	 * @param serviceLoader the service loader to be filtered
	 * @param providerPredicate the predicate to filter the loaded services
	 * @return a stream of loaded services that match the predicate
	 */
	public static <T> Stream<T> filter(ServiceLoader<T> serviceLoader,
			Predicate<? super Class<? extends T>> providerPredicate) {
		// @formatter:off
		return serviceLoader
				.stream()
				.filter(provider -> providerPredicate.test(provider.type()))
				.map(ServiceLoader.Provider::get);
		// @formatter:on
	}

}
