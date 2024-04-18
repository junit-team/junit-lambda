/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.support;

import static java.util.Arrays.asList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

/**
 * {@code AnnotationConsumerInitializer} is an internal helper class for
 * initializing {@link AnnotationConsumer AnnotationConsumers}.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public final class AnnotationConsumerInitializer {

	private static final List<AnnotationConsumingMethodSignature> annotationConsumingMethodSignatures = asList( //
		new AnnotationConsumingMethodSignature("accept", 1, 0), //
		new AnnotationConsumingMethodSignature("provideArguments", 2, 1), //
		new AnnotationConsumingMethodSignature("convert", 3, 2));

	private AnnotationConsumerInitializer() {
		/* no-op */
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T initialize(AnnotatedElement annotatedElement, T annotationConsumerInstance) {
		if (annotationConsumerInstance instanceof AnnotationConsumer) {
			Class<? extends Annotation> annotationType = findConsumedAnnotationType(annotationConsumerInstance);
			List<? extends Annotation> annotations = streamAnnotations(annotatedElement, annotationType);

			if (annotations.isEmpty()) {
				throw new JUnitException(annotationConsumerInstance.getClass().getName()
						+ " must be used with an annotation of type " + annotationType.getName());
			}

			annotations.forEach(annotation -> initializeAnnotationConsumer(
				(AnnotationConsumer) annotationConsumerInstance, annotation));
		}
		return annotationConsumerInstance;
	}

	private static List<? extends Annotation> streamAnnotations(AnnotatedElement annotatedElement,
			Class<? extends Annotation> annotationType) {

		return annotationType.isAnnotationPresent(Repeatable.class)
				? findRepeatableAnnotations(annotatedElement, annotationType)
				: Stream.of(findAnnotation(annotatedElement, annotationType)).filter(Optional::isPresent).map(
					Optional::get).collect(Collectors.toList());
	}

	private static <T> Class<? extends Annotation> findConsumedAnnotationType(T annotationConsumerInstance) {
		Predicate<Method> consumesAnnotation = annotationConsumingMethodSignatures.stream() //
				.map(signature -> (Predicate<Method>) signature::isMatchingWith) //
				.reduce(method -> false, Predicate::or);
		Method method = findMethods(annotationConsumerInstance.getClass(), consumesAnnotation, BOTTOM_UP).get(0);
		return getAnnotationType(method);
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Annotation> getAnnotationType(Method method) {
		int annotationIndex = annotationConsumingMethodSignatures.stream() //
				.filter(signature -> signature.isMatchingWith(method)) //
				.findFirst() //
				.map(AnnotationConsumingMethodSignature::getAnnotationParameterIndex) //
				.orElse(0);

		return (Class<? extends Annotation>) method.getParameterTypes()[annotationIndex];
	}

	private static <A extends Annotation> void initializeAnnotationConsumer(AnnotationConsumer<A> instance,
			A annotation) {
		try {
			instance.accept(annotation);
		}
		catch (Exception ex) {
			throw new JUnitException("Failed to initialize AnnotationConsumer: " + instance, ex);
		}
	}

	private static class AnnotationConsumingMethodSignature {

		private final String methodName;
		private final int parameterCount;
		private final int annotationParameterIndex;

		AnnotationConsumingMethodSignature(String methodName, int parameterCount, int annotationParameterIndex) {
			this.methodName = methodName;
			this.parameterCount = parameterCount;
			this.annotationParameterIndex = annotationParameterIndex;
		}

		boolean isMatchingWith(Method method) {
			return method.getName().equals(methodName) //
					&& method.getParameterCount() == parameterCount //
					&& method.getParameterTypes()[annotationParameterIndex].isAnnotation();
		}

		int getAnnotationParameterIndex() {
			return annotationParameterIndex;
		}

	}

}
