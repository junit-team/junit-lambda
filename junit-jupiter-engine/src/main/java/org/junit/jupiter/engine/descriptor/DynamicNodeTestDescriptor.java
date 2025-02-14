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

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * Base {@link TestDescriptor} for a {@link DynamicNode}.
 *
 * @since 5.0.3
 */
abstract class DynamicNodeTestDescriptor extends JupiterTestDescriptor {

	protected final int index;

	DynamicNodeTestDescriptor(UniqueId uniqueId, int index, DynamicNode dynamicNode, TestSource testSource,
			JupiterConfiguration configuration) {
		super(uniqueId, dynamicNode.getDisplayName(), testSource, configuration);
		this.index = index;
	}

	@Override
	public String getLegacyReportingName() {
		// @formatter:off
		return getParent()
				.map(TestDescriptor::getLegacyReportingName)
				.orElseGet(this::getDisplayName)
						+ "[" + index + "]";
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		ExtensionContext extensionContext = new DynamicExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, context.getConfiguration(), context.getExtensionRegistry());
		// @formatter:off
		return context.extend()
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) {
		return SkipResult.doNotSkip();
	}

}
