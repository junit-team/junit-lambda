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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * Unit tests for {@link AbstractTestDescriptor} and {@link EngineDescriptor}.
 *
 * @since 1.0
 */
class AbstractTestDescriptorTests implements TestDescriptorOrderChildrenTests {

	private EngineDescriptor engineDescriptor;
	private GroupDescriptor group1;
	private GroupDescriptor group11;
	private LeafDescriptor leaf111;

	@BeforeEach
	void initTree() {
		engineDescriptor = new EngineDescriptor(UniqueId.forEngine("testEngine"), "testEngine");
		group1 = new GroupDescriptor(UniqueId.root("group", "group1"));
		engineDescriptor.addChild(group1);
		var group2 = new GroupDescriptor(UniqueId.root("group", "group2"));
		engineDescriptor.addChild(group2);
		group11 = new GroupDescriptor(UniqueId.root("group", "group1-1"));
		group1.addChild(group11);

		group1.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf1-1")));
		group1.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf1-2")));

		group2.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf2-1")));

		leaf111 = new LeafDescriptor(UniqueId.root("leaf", "leaf11-1"));
		group11.addChild(leaf111);
	}

	@Override
	public TestDescriptor createEmptyTestDescriptor() {
		return new GroupDescriptor(UniqueId.root("group", "1"));
	}

	@Test
	void removeRootFromHierarchyFails() {
		var e = assertThrows(JUnitException.class, () -> engineDescriptor.removeFromHierarchy());
		assertTrue(e.toString().contains("cannot remove the root of a hierarchy"));
	}

	@Test
	void removeFromHierarchyClearsParentFromAllChildren() {
		var group = engineDescriptor.getChildren().iterator().next();
		assertSame(engineDescriptor, group.getParent().orElseThrow(Error::new));
		assertTrue(group.getChildren().stream().allMatch(d -> d.getParent().orElseThrow(Error::new) == group));

		var formerChildren = group.getChildren();
		group.removeFromHierarchy();

		assertFalse(group.getParent().isPresent());
		assertTrue(group.getChildren().isEmpty());
		assertTrue(formerChildren.stream().noneMatch(d -> d.getParent().isPresent()));
	}

	@Test
	void setParentToOtherInstance() {
		TestDescriptor newEngine = new EngineDescriptor(UniqueId.forEngine("newEngine"), "newEngine");
		var group = engineDescriptor.getChildren().iterator().next();
		assertSame(engineDescriptor, group.getParent().orElseThrow(Error::new));
		group.setParent(newEngine);
		assertSame(newEngine, group.getParent().orElseThrow(Error::new));
	}

	@Test
	void setParentToNull() {
		var group = engineDescriptor.getChildren().iterator().next();
		assertTrue(group.getParent().isPresent());
		group.setParent(null);
		assertFalse(group.getParent().isPresent());
	}

	@Test
	void visitAllNodes() {
		List<TestDescriptor> visited = new ArrayList<>();
		engineDescriptor.accept(visited::add);

		assertEquals(8, visited.size());
	}

	@Test
	void pruneLeaf() {
		TestDescriptor.Visitor visitor = descriptor -> {
			if (descriptor.getUniqueId().equals(UniqueId.root("leaf", "leaf1-1")))
				descriptor.removeFromHierarchy();
		};
		engineDescriptor.accept(visitor);

		List<UniqueId> visited = new ArrayList<>();
		engineDescriptor.accept(descriptor -> visited.add(descriptor.getUniqueId()));

		assertEquals(7, visited.size());
		assertTrue(visited.contains(UniqueId.root("group", "group1")));
		assertFalse(visited.contains(UniqueId.root("leaf", "leaf1-1")));
	}

	@Test
	void pruneGroup() {
		final var countVisited = new AtomicInteger();
		TestDescriptor.Visitor visitor = descriptor -> {
			if (descriptor.getUniqueId().equals(UniqueId.root("group", "group1")))
				descriptor.removeFromHierarchy();
			countVisited.incrementAndGet();
		};
		engineDescriptor.accept(visitor);

		assertEquals(4, countVisited.get(), "Children of pruned element are not visited");

		List<UniqueId> visited = new ArrayList<>();
		engineDescriptor.accept(descriptor -> visited.add(descriptor.getUniqueId()));

		assertEquals(3, visited.size());
		assertFalse(visited.contains(UniqueId.root("group", "group1")));
	}

	@Test
	void getAncestors() {
		assertThat(getAncestorsUniqueIds(engineDescriptor)).isEmpty();

		assertThat(getAncestorsUniqueIds(group1)).containsExactly( //
			UniqueId.forEngine("testEngine"));

		assertThat(getAncestorsUniqueIds(group11)).containsExactly( //
			UniqueId.root("group", "group1"), //
			UniqueId.forEngine("testEngine"));

		assertThat(getAncestorsUniqueIds(leaf111)).containsExactly( //
			UniqueId.root("group", "group1-1"), //
			UniqueId.root("group", "group1"), //
			UniqueId.forEngine("testEngine"));
	}

	private List<UniqueId> getAncestorsUniqueIds(TestDescriptor descriptor) {
		return descriptor.getAncestors().stream().map(TestDescriptor::getUniqueId).toList();
	}

}

class GroupDescriptor extends AbstractTestDescriptor {

	GroupDescriptor(UniqueId uniqueId) {
		super(uniqueId, "group: " + uniqueId);
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

}

class LeafDescriptor extends AbstractTestDescriptor {

	LeafDescriptor(UniqueId uniqueId) {
		super(uniqueId, "leaf: " + uniqueId);
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

}
