/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsPotentialTestContainer;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
class TestContainerResolver implements ElementResolver {

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	static final String SEGMENT_TYPE = "class";

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Class))
			return Collections.emptySet();

		Class<?> clazz = (Class<?>) element;
		if (!isPotentialCandidate(clazz))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueId(clazz, parent);
		return resolveClass(parent, clazz, uniqueId).map(Collections::singleton).orElse(Collections.emptySet());
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(getSegmentType()))
			return Optional.empty();

		if (!requiredParentType().isInstance(parent))
			return Optional.empty();

		String className = getClassName(parent, segment.getValue());

		Optional<Class<?>> optionalContainerClass = ReflectionUtils.loadClass(className);
		if (!optionalContainerClass.isPresent())
			return Optional.empty();

		Class<?> containerClass = optionalContainerClass.get();
		if (!isPotentialCandidate(containerClass))
			return Optional.empty();

		UniqueId uniqueId = createUniqueId(containerClass, parent);
		return resolveClass(parent, containerClass, uniqueId);
	}

	protected Class<? extends TestDescriptor> requiredParentType() {
		return TestDescriptor.class;
	}

	protected String getClassName(TestDescriptor parent, String segmentValue) {
		return segmentValue;
	}

	protected String getSegmentType() {
		return SEGMENT_TYPE;
	}

	protected String getSegmentValue(Class<?> testClass) {
		return testClass.getName();
	}

	protected boolean isPotentialCandidate(Class<?> element) {
		return isPotentialTestContainer.test(element);
	}

	protected UniqueId createUniqueId(Class<?> testClass, TestDescriptor parent) {
		return parent.getUniqueId().append(getSegmentType(), getSegmentValue(testClass));
	}

	protected Optional<TestDescriptor> resolveClass(TestDescriptor parent, Class<?> testClass, UniqueId uniqueId) {
		return Optional.of(new ClassTestDescriptor(uniqueId, testClass));
	}

}
