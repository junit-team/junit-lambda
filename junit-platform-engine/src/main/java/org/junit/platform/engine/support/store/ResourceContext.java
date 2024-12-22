/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.store;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;

public interface ResourceContext {

	Optional<ResourceContext> getParent();

	ResourceContext getRoot();

	// TODO: Implement methods to retrieve session and request
	// Optional<Object> getSession();
	// Optional<Object> getRequest();

	// TODO : Implement methods to retrieve lifecycle about session and request
	// Optional<Lifecycle> getSessionLifecycle();
	// Optional<Lifecycle> getRequestLifecycle();

	interface Store {

		@API(status = STABLE, since = "5.1")
		interface CloseableResource {

			void close() throws Throwable;

		}

		Object get(Object key);

		<V> V get(Object key, Class<V> requiredType);

		default <V> V getOrDefault(Object key, Class<V> requiredType, V defaultValue) {
			V value = get(key, requiredType);
			return (value != null ? value : defaultValue);
		}

		@API(status = STABLE, since = "5.1")
		default <V> V getOrComputeIfAbsent(Class<V> type) {
			return getOrComputeIfAbsent(type, ReflectionSupport::newInstance, type);
		}

		<K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator);

		<K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType);

		void put(Object key, Object value);

		Object remove(Object key);

		<V> V remove(Object key, Class<V> requiredType);

	}

	class Namespace {

		public static final Namespace GLOBAL = Namespace.create(new Object());

		public static Namespace create(Object... parts) {
			Preconditions.notEmpty(parts, "parts array must not be null or empty");
			Preconditions.containsNoNullElements(parts, "individual parts must not be null");
			return new Namespace(new ArrayList<>(Arrays.asList(parts)));
		}

		private final List<Object> parts;

		private Namespace(List<Object> parts) {
			this.parts = parts;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Namespace that = (Namespace) o;
			return this.parts.equals(that.parts);
		}

		@Override
		public int hashCode() {
			return this.parts.hashCode();
		}

		public Namespace append(Object... parts) {
			Preconditions.notEmpty(parts, "parts array must not be null or empty");
			Preconditions.containsNoNullElements(parts, "individual parts must not be null");
			ArrayList<Object> newParts = new ArrayList<>(this.parts.size() + parts.length);
			newParts.addAll(this.parts);
			Collections.addAll(newParts, parts);
			return new Namespace(newParts);
		}
	}

}
