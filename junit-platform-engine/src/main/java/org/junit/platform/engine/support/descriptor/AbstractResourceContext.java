/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import java.util.Optional;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.engine.support.store.ResourceContext;
import org.junit.platform.engine.support.store.ResourceContext.Store.CloseableResource;

public class AbstractResourceContext implements ResourceContext, AutoCloseable {

	private static final NamespacedHierarchicalStore.CloseAction<Namespace> CLOSE_RESOURCES = (__, ___, value) -> {
		if (value instanceof CloseableResource) {
			((CloseableResource) value).close();
		}
	};

	private final ResourceContext parent;
	private final NamespacedHierarchicalStore<Namespace> valueStore;

	public AbstractResourceContext(ResourceContext parent) {
		this.parent = parent;
		this.valueStore = createStore(parent);
	}

	private static NamespacedHierarchicalStore<Namespace> createStore(ResourceContext parent) {
		NamespacedHierarchicalStore<Namespace> parentStore = null;
		if (parent != null) {
			parentStore = ((AbstractResourceContext) parent).valueStore;
		}

		return new NamespacedHierarchicalStore<>(parentStore, CLOSE_RESOURCES);
	}

	@Override
	public void close() throws Exception {
		this.valueStore.close();
	}

	@Override
	public Optional<ResourceContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public ResourceContext getRoot() {
		if (this.parent != null) {
			return this.parent.getRoot();
		}
		return this;
	}

}
