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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DisplayNameGenerator.IndicativeSentences;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;
import org.junit.jupiter.api.DisplayNameGenerator.Standard;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * Collection of utilities for working with display names.
 *
 * @since 5.4
 * @see DisplayName
 * @see DisplayNameGenerator
 * @see DisplayNameGeneration
 */
final class DisplayNameUtils {

	private static final Logger logger = LoggerFactory.getLogger(DisplayNameUtils.class);

	/**
	 * Pre-defined standard display name generator instance.
	 */
	private static final DisplayNameGenerator standardGenerator = DisplayNameGenerator.getDisplayNameGenerator(
		Standard.class);

	/**
	 * Pre-defined simple display name generator instance.
	 */
	private static final DisplayNameGenerator simpleGenerator = DisplayNameGenerator.getDisplayNameGenerator(
		Simple.class);

	/**
	 * Pre-defined display name generator instance replacing underscores.
	 */
	private static final DisplayNameGenerator replaceUnderscoresGenerator = DisplayNameGenerator.getDisplayNameGenerator(
		ReplaceUnderscores.class);

	/**
	 * Pre-defined display name generator instance producing indicative sentences.
	 */
	private static final DisplayNameGenerator indicativeSentencesGenerator = DisplayNameGenerator.getDisplayNameGenerator(
		IndicativeSentences.class);

	static String determineDisplayName(AnnotatedElement element, Supplier<String> displayNameSupplier) {
		Preconditions.notNull(element, "Annotated element must not be null");
		Optional<DisplayName> displayNameAnnotation = findAnnotation(element, DisplayName.class);
		if (displayNameAnnotation.isPresent()) {
			String displayName = displayNameAnnotation.get().value().trim();

			// TODO [#242] Replace logging with precondition check once we have a proper mechanism for
			// handling validation exceptions during the TestEngine discovery phase.
			if (StringUtils.isBlank(displayName)) {
				logger.warn(() -> String.format(
					"Configuration error: @DisplayName on [%s] must be declared with a non-empty value.", element));
			}
			else {
				return displayName;
			}
		}
		// else let a 'DisplayNameGenerator' generate a display name
		return displayNameSupplier.get();
	}

	static String determineDisplayNameForMethod(Supplier<List<Class<?>>> enclosingInstanceTypes, Class<?> testClass,
			Method testMethod, JupiterConfiguration configuration) {
		return determineDisplayName(testMethod,
			createDisplayNameSupplierForMethod(enclosingInstanceTypes, testClass, testMethod, configuration));
	}

	static Supplier<String> createDisplayNameSupplierForClass(Class<?> testClass, JupiterConfiguration configuration) {
		return createDisplayNameSupplier(Collections::emptyList, testClass, configuration,
			(generator, __) -> generator.generateDisplayNameForClass(testClass));
	}

	static Supplier<String> createDisplayNameSupplierForNestedClass(
			Supplier<List<Class<?>>> enclosingInstanceTypesSupplier, Class<?> testClass,
			JupiterConfiguration configuration) {
		return createDisplayNameSupplier(enclosingInstanceTypesSupplier, testClass, configuration,
			(generator, enclosingInstanceTypes) -> generator.generateDisplayNameForNestedClass(enclosingInstanceTypes,
				testClass));
	}

	private static Supplier<String> createDisplayNameSupplierForMethod(
			Supplier<List<Class<?>>> enclosingInstanceTypesSupplier, Class<?> testClass, Method testMethod,
			JupiterConfiguration configuration) {
		return createDisplayNameSupplier(enclosingInstanceTypesSupplier, testClass, configuration,
			(generator, enclosingInstanceTypes) -> generator.generateDisplayNameForMethod(enclosingInstanceTypes,
				testClass, testMethod));
	}

	private static Supplier<String> createDisplayNameSupplier(Supplier<List<Class<?>>> enclosingInstanceTypesSupplier,
			Class<?> testClass, JupiterConfiguration configuration,
			BiFunction<DisplayNameGenerator, List<Class<?>>, String> generatorFunction) {
		return () -> {
			List<Class<?>> enclosingInstanceTypes = makeUnmodifiable(enclosingInstanceTypesSupplier.get());
			return findDisplayNameGenerator(enclosingInstanceTypes, testClass) //
					.map(it -> generatorFunction.apply(it, enclosingInstanceTypes)) //
					.orElseGet(() -> generatorFunction.apply(configuration.getDefaultDisplayNameGenerator(),
						enclosingInstanceTypes));
		};
	}

	private static <T> List<T> makeUnmodifiable(List<T> list) {
		return list.isEmpty() ? emptyList() : unmodifiableList(list);
	}

	private static Optional<DisplayNameGenerator> findDisplayNameGenerator(List<Class<?>> enclosingInstanceTypes,
			Class<?> testClass) {
		Preconditions.notNull(testClass, "Test class must not be null");

		return findAnnotation(testClass, DisplayNameGeneration.class, enclosingInstanceTypes) //
				.map(DisplayNameGeneration::value) //
				.map(displayNameGeneratorClass -> {
					if (displayNameGeneratorClass == Standard.class) {
						return standardGenerator;
					}
					if (displayNameGeneratorClass == Simple.class) {
						return simpleGenerator;
					}
					if (displayNameGeneratorClass == ReplaceUnderscores.class) {
						return replaceUnderscoresGenerator;
					}
					if (displayNameGeneratorClass == IndicativeSentences.class) {
						return indicativeSentencesGenerator;
					}
					return ReflectionSupport.newInstance(displayNameGeneratorClass);
				});
	}

}
