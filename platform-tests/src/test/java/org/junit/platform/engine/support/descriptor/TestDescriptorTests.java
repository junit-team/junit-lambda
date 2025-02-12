/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

class TestDescriptorTests implements TestDescriptorOrderChildrenTests {

	@Override
	public TestDescriptor createEmptyTestDescriptor() {
		return new MinimalTestDescriptorImplementation();
	}

	private static class MinimalTestDescriptorImplementation implements TestDescriptor {

		private final Set<TestDescriptor> children = Collections.synchronizedSet(new LinkedHashSet<>());

		@Override
		public UniqueId getUniqueId() {
			return UniqueId.root("root", "value");
		}

		@Override
		public String getDisplayName() {
			return "TestDescriptorImplementation";
		}

		@Override
		public Set<TestTag> getTags() {
			return Set.of();
		}

		@Override
		public Optional<TestSource> getSource() {
			return Optional.empty();
		}

		@Override
		public Optional<TestDescriptor> getParent() {
			return Optional.empty();
		}

		@Override
		public void setParent(TestDescriptor parent) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public Set<? extends TestDescriptor> getChildren() {
			return Collections.unmodifiableSet(children);
		}

		@Override
		public void addChild(TestDescriptor descriptor) {
			children.add(descriptor);
		}

		@Override
		public void removeChild(TestDescriptor descriptor) {
			children.remove(descriptor);
		}

		@Override
		public void removeFromHierarchy() {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public Type getType() {
			return Type.CONTAINER;
		}

		@Override
		public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}
}
