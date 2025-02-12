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

import static java.util.Collections.emptySet;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Abstract base implementation of {@link TestDescriptor} that may be used by
 * custom {@link org.junit.platform.engine.TestEngine TestEngines}.
 *
 * <p>Subclasses should provide a {@link TestSource} in their constructor, if
 * possible, and override {@link #getTags()}, if appropriate.
 *
 * @since 1.0
 */
@API(status = STABLE, since = "1.0")
public abstract class AbstractTestDescriptor implements TestDescriptor {

	private final UniqueId uniqueId;

	private final String displayName;

	private final TestSource source;

	private TestDescriptor parent;

	/**
	 * The synchronized set of children associated with this {@code TestDescriptor}.
	 *
	 * <p>This set is used in methods such as {@link #addChild(TestDescriptor)},
	 * {@link #removeChild(TestDescriptor)}, {@link #removeFromHierarchy()}, and
	 * {@link #findByUniqueId(UniqueId)}, and an immutable copy of this set is
	 * returned by {@link #getChildren()}.
	 *
	 * <p>If a subclass overrides any of the methods related to children, this
	 * set should be used instead of a set local to the subclass.
	 */
	protected final Set<TestDescriptor> children = Collections.synchronizedSet(new LinkedHashSet<>(16));

	/**
	 * Create a new {@code AbstractTestDescriptor} with the supplied
	 * {@link UniqueId} and display name.
	 *
	 * @param uniqueId the unique ID of this {@code TestDescriptor}; never
	 * {@code null}
	 * @param displayName the display name for this {@code TestDescriptor};
	 * never {@code null} or blank
	 * @see #AbstractTestDescriptor(UniqueId, String, TestSource)
	 */
	protected AbstractTestDescriptor(UniqueId uniqueId, String displayName) {
		this(uniqueId, displayName, null);
	}

	/**
	 * Create a new {@code AbstractTestDescriptor} with the supplied
	 * {@link UniqueId}, display name, and source.
	 *
	 * @param uniqueId the unique ID of this {@code TestDescriptor}; never
	 * {@code null}
	 * @param displayName the display name for this {@code TestDescriptor};
	 * never {@code null} or blank
	 * @param source the source of the test or container described by this
	 * {@code TestDescriptor}; can be {@code null}
	 * @see #AbstractTestDescriptor(UniqueId, String)
	 */
	protected AbstractTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		this.uniqueId = Preconditions.notNull(uniqueId, "UniqueId must not be null");
		this.displayName = Preconditions.notBlank(displayName, "displayName must not be null or blank");
		this.source = source;
	}

	@Override
	public final UniqueId getUniqueId() {
		return this.uniqueId;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public Set<TestTag> getTags() {
		return emptySet();
	}

	@Override
	public Optional<TestSource> getSource() {
		return Optional.ofNullable(this.source);
	}

	@Override
	public final Optional<TestDescriptor> getParent() {
		return Optional.ofNullable(this.parent);
	}

	@Override
	public final void setParent(TestDescriptor parent) {
		this.parent = parent;
	}

	@Override
	public final Set<? extends TestDescriptor> getChildren() {
		return Collections.unmodifiableSet(this.children);
	}

	@Override
	public void addChild(TestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		child.setParent(this);
		this.children.add(child);
	}

	@Override
	public void removeChild(TestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		this.children.remove(child);
		child.setParent(null);
	}

	@Override
	public void removeFromHierarchy() {
		Preconditions.condition(!isRoot(), "cannot remove the root of a hierarchy");
		this.parent.removeChild(this);
		this.children.forEach(child -> child.setParent(null));
		this.children.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void orderChildren(UnaryOperator<List<TestDescriptor>> orderer) {
		Preconditions.notNull(orderer, "orderer must not be null");
		List<TestDescriptor> suggestedOrder = orderer.apply(new ArrayList<>(this.children));
		Preconditions.notNull(suggestedOrder, "orderer may not return null");

		Set<? extends TestDescriptor> orderedChildren = new LinkedHashSet<>(suggestedOrder);
		boolean unmodified = this.children.equals(orderedChildren);
		Preconditions.condition(unmodified && this.children.size() == suggestedOrder.size(),
			"orderer may not add or remove test descriptors");

		this.children.clear();
		this.children.addAll(orderedChildren);
	}

	@Override
	public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
		Preconditions.notNull(uniqueId, "UniqueId must not be null");
		if (getUniqueId().equals(uniqueId)) {
			return Optional.of(this);
		}
		// @formatter:off
		return this.children.stream()
				.map(child -> child.findByUniqueId(uniqueId))
				.filter(Optional::isPresent)
				.findAny()
				.orElse(Optional.empty());
		// @formatter:on
	}

	@Override
	public final int hashCode() {
		return this.uniqueId.hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this.getClass() != other.getClass()) {
			return false;
		}
		TestDescriptor that = (TestDescriptor) other;
		return this.getUniqueId().equals(that.getUniqueId());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getUniqueId();
	}

}
