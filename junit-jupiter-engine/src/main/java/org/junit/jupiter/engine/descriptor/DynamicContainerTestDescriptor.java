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

import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.createDynamicDescriptor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicContainer}.
 *
 * @since 5.0
 */
class DynamicContainerTestDescriptor extends DynamicNodeTestDescriptor {

	private final DynamicContainer dynamicContainer;
	private final TestSource testSource;
	private final DynamicDescendantFilter dynamicDescendantFilter;

	DynamicContainerTestDescriptor(UniqueId uniqueId, int index, DynamicContainer dynamicContainer,
			TestSource testSource, DynamicDescendantFilter dynamicDescendantFilter,
			JupiterConfiguration configuration) {

		super(uniqueId, index, dynamicContainer, testSource, configuration);
		this.dynamicContainer = dynamicContainer;
		this.testSource = testSource;
		this.dynamicDescendantFilter = dynamicDescendantFilter;
	}

	@Override
	protected DynamicContainerTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new DynamicContainerTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), this.index,
			this.dynamicContainer, this.testSource, this.dynamicDescendantFilter, this.configuration);
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {

		AtomicInteger index = new AtomicInteger(1);
		try (Stream<? extends DynamicNode> children = dynamicContainer.getChildren()) {
			// @formatter:off
			children.map(child -> {
						Preconditions.notNull(child, "individual dynamic node must not be null");
						return toDynamicDescriptor(index.getAndIncrement(), child);
					})
					.filter(Optional::isPresent)
					.map(Optional::get)
					.forEachOrdered(dynamicTestExecutor::execute);
			// @formatter:on
		}
		return context;
	}

	private Optional<JupiterTestDescriptor> toDynamicDescriptor(int index, DynamicNode childNode) {
		return createDynamicDescriptor(this, childNode, index, testSource, dynamicDescendantFilter, configuration);
	}

}
