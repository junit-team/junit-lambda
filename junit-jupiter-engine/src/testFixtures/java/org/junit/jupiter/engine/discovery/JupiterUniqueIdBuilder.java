/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.UniqueId;

/**
 * Test data builder for unique IDs for JupiterTestEngine.
 *
 * Used to decouple tests from concrete unique ID strings.
 *
 * @since 5.0
 */
public class JupiterUniqueIdBuilder {

	public static UniqueId uniqueIdForClass(Class<?> clazz) {
		if (isInnerClass(clazz)) {
			var segmentType = classSegmentType(clazz, NestedClassTestDescriptor.SEGMENT_TYPE,
				ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE);
			return uniqueIdForClass(clazz.getEnclosingClass()).append(segmentType, clazz.getSimpleName());
		}
		return uniqueIdForStaticClass(clazz.getName());
	}

	public static UniqueId uniqueIdForStaticClass(String className) {
		return engineId().append(staticClassSegmentType(className), className);
	}

	private static String staticClassSegmentType(String className) {
		return ReflectionSupport.tryToLoadClass(className).toOptional() //
				.map(it -> classSegmentType(it, ClassTestDescriptor.SEGMENT_TYPE,
					ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE)) //
				.orElse(ClassTestDescriptor.SEGMENT_TYPE);
	}

	private static String classSegmentType(Class<?> clazz, String regularSegmentType,
			String containerTemplateSegmentType) {
		return AnnotationSupport.isAnnotated(clazz, ContainerTemplate.class) //
				? containerTemplateSegmentType //
				: regularSegmentType;
	}

	public static UniqueId uniqueIdForMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestMethodTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestFactoryMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestFactoryTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestTemplateMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestTemplateTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId appendTestTemplateInvocationSegment(UniqueId parentId, int index) {
		return parentId.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
	}

	public static UniqueId appendContainerTemplateInvocationSegment(UniqueId parentId, int index) {
		return parentId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
	}

}
