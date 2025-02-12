/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.api.parallel.ResourceLockTarget.CHILDREN;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * @since 5.12
 */
interface ResourceLockAware extends TestDescriptor {

	default Stream<ExclusiveResource> determineExclusiveResources() {

		Deque<ResourceLockAware> ancestors = new ArrayDeque<>();
		TestDescriptor parent = this.getParent().orElse(null);
		while (parent instanceof ResourceLockAware) {
			ancestors.addFirst((ResourceLockAware) parent);
			parent = parent.getParent().orElse(null);
		}

		Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> evaluator = getResourceLocksProviderEvaluator();

		if (ancestors.isEmpty()) {
			return determineOwnExclusiveResources(evaluator);
		}

		Stream<ExclusiveResource> parentStaticResourcesForChildren = ancestors.getLast() //
				.getExclusiveResourceCollector().getStaticResourcesFor(CHILDREN);

		Stream<ExclusiveResource> ancestorDynamicResources = ancestors.stream() //
				.map(ResourceLockAware::getExclusiveResourceCollector) //
				.flatMap(collector -> collector.getDynamicResources(evaluator));

		return Stream.of(ancestorDynamicResources, parentStaticResourcesForChildren,
			determineOwnExclusiveResources(evaluator))//
				.flatMap(s -> s);
	}

	default Stream<ExclusiveResource> determineOwnExclusiveResources(
			Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> providerToLocks) {
		return this.getExclusiveResourceCollector().getAllExclusiveResources(providerToLocks);
	}

	ExclusiveResourceCollector getExclusiveResourceCollector();

	Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator();

	static Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> enclosingInstanceTypesDependentResourceLocksProviderEvaluator(
			Supplier<List<Class<?>>> enclosingInstanceTypesSupplier,
			BiFunction<ResourceLocksProvider, List<Class<?>>, Set<ResourceLocksProvider.Lock>> evaluator) {
		return new Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>>() {

			private List<Class<?>> enclosingInstanceTypes;

			@Override
			public Set<ResourceLocksProvider.Lock> apply(ResourceLocksProvider provider) {
				if (this.enclosingInstanceTypes == null) {
					this.enclosingInstanceTypes = makeUnmodifiable(enclosingInstanceTypesSupplier.get());
				}
				return evaluator.apply(provider, this.enclosingInstanceTypes);
			}

			private <T> List<T> makeUnmodifiable(List<T> list) {
				return list.isEmpty() ? emptyList() : unmodifiableList(list);
			}
		};
	}

}
