/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
public abstract class TestDescriptorBuilder<T extends TestDescriptor> {

	final List<TestDescriptorBuilder<?>> children = new ArrayList<>();

	public static JupiterEngineDescriptorBuilder engineDescriptor() {
		return new JupiterEngineDescriptorBuilder();
	}

	public static ClassTestDescriptorBuilder classTestDescriptor(String uniqueId, Class<?> testClass) {
		return new ClassTestDescriptorBuilder(uniqueId, testClass);
	}

	public static NestedClassTestDescriptorBuilder nestedClassTestDescriptor(String uniqueId,
			ClassTestDescriptor parent, Class<?> testClass) {
		return new NestedClassTestDescriptorBuilder(uniqueId, parent, testClass);
	}

	public T build() {
		T testDescriptor = buildDescriptor();
		children.forEach(builder -> {
			// TODO unsafe and dirty => fix it by adjusting Builders
			if (builder instanceof NestedClassTestDescriptorBuilder) {
				((NestedClassTestDescriptorBuilder) builder).parent = (ClassTestDescriptor) testDescriptor;
			}
			testDescriptor.addChild(builder.build());
		});
		return testDescriptor;
	}

	public TestDescriptorBuilder<?> with(TestDescriptorBuilder<?>... children) {
		this.children.addAll(Arrays.asList(children));
		return this;
	}

	abstract T buildDescriptor();

	public static class JupiterEngineDescriptorBuilder extends TestDescriptorBuilder<JupiterEngineDescriptor> {

		@Override
		JupiterEngineDescriptor buildDescriptor() {
			return new JupiterEngineDescriptor(UniqueId.forEngine("junit-jupiter"));
		}
	}

	public static class ClassTestDescriptorBuilder extends TestDescriptorBuilder<ClassTestDescriptor> {

		protected final String uniqueId;
		protected final Class<?> testClass;

		public ClassTestDescriptorBuilder(String uniqueId, Class<?> testClass) {
			this.uniqueId = uniqueId;
			this.testClass = testClass;
		}

		@Override
		ClassTestDescriptor buildDescriptor() {
			return new ClassTestDescriptor(UniqueId.root("class", uniqueId), testClass);
		}
	}

	public static class NestedClassTestDescriptorBuilder extends ClassTestDescriptorBuilder {

		protected ClassTestDescriptor parent;

		public NestedClassTestDescriptorBuilder(String uniqueId, ClassTestDescriptor parent, Class<?> testClass) {
			super(uniqueId, testClass);
			this.parent = parent;
		}

		@Override
		NestedClassTestDescriptor buildDescriptor() {
			return new NestedClassTestDescriptor(UniqueId.root("nested-class", uniqueId), parent, testClass);
		}
	}

}
