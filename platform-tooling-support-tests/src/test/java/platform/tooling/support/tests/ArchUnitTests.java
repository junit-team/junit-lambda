/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.ANONYMOUS_CLASSES;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.TOP_LEVEL_CLASSES;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameContaining;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameStartingWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.have;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.LocationProvider;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import org.apiguardian.api.API;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;

@AnalyzeClasses(locations = ArchUnitTests.AllJars.class)
class ArchUnitTests {

	@SuppressWarnings("unused")
	@ArchTest
	private final ArchRule allClassesAreInJUnitPackage = classes() //
			.should().haveNameMatching("org\\.junit\\..+");

	@SuppressWarnings("unused")
	@ArchTest
	private final ArchRule allPublicTopLevelTypesHaveApiAnnotations = classes() //
			.that(have(modifier(PUBLIC))) //
			.and(TOP_LEVEL_CLASSES) //
			.and(not(ANONYMOUS_CLASSES)) //
			.and(not(describe("are Kotlin SAM type implementations", simpleName("")))) //
			.and(not(describe("are shadowed", resideInAnyPackage("..shadow..")))) //
			.should().beAnnotatedWith(API.class);

	@SuppressWarnings("unused")
	@ArchTest // Consistency of @Documented and @Inherited is checked by the compiler but not for @Retention and @Target
	private final ArchRule repeatableAnnotationsShouldHaveMatchingContainerAnnotations = classes() //
			.that(nameStartingWith("org.junit.")) //
			.and().areAnnotations() //
			.and().areAnnotatedWith(Repeatable.class) //
			.should(haveContainerAnnotationWithSameRetentionPolicy()) //
			.andShould(haveContainerAnnotationWithSameTargetTypes());

	@ArchTest
	void allAreIn(JavaClasses classes) {
		// about 928 classes found in all jars
		assertTrue(classes.size() > 800, "expected more than 800 classes, got: " + classes.size());
	}

	@ArchTest
	void freeOfCycles(JavaClasses classes) {
		slices().matching("org.junit.(*)..").should().beFreeOfCycles().check(classes);
	}

	@ArchTest
	void avoidJavaUtilLogging(JavaClasses classes) {
		// LoggerFactory.java:80 -> sets field LoggerFactory$DelegatingLogger.julLogger
		var subset = classes.that(are(not(name("org.junit.platform.commons.logging.LoggerFactory$DelegatingLogger"))));
		GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(subset);
	}

	@ArchTest
	void avoidThrowingGenericExceptions(JavaClasses classes) {
		// LoggerFactory.java:155 -> new Throwable()
		var subset = classes.that(are(not(
			name("org.junit.platform.commons.logging.LoggerFactory$DelegatingLogger").or(nameContaining(".shadow.")))));
		GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(subset);
	}

	@ArchTest
	void avoidAccessingStandardStreams(JavaClasses classes) {
		// ConsoleLauncher, StreamInterceptor, Picocli et al...
		var subset = classes //
				.that(are(not(name("org.junit.platform.console.ConsoleLauncher")))) //
				.that(are(not(name("org.junit.platform.console.tasks.ConsoleTestExecutor")))) //
				.that(are(not(name("org.junit.platform.launcher.core.StreamInterceptor")))) //
				.that(are(not(name("org.junit.platform.runner.JUnitPlatformRunnerListener")))) //
				.that(are(not(name("org.junit.platform.testkit.engine.Events")))) //
				.that(are(not(name("org.junit.platform.testkit.engine.Executions")))) //
				//The PreInterruptThreadDumpPrinter writes to StdOut by contract to dump threads
				.that(are(not(name("org.junit.jupiter.engine.extension.PreInterruptThreadDumpPrinter")))) //
				.that(are(not(resideInAPackage("org.junit.platform.console.shadow.picocli"))));
		GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(subset);
	}

	private static ArchCondition<? super JavaClass> haveContainerAnnotationWithSameRetentionPolicy() {
		return ArchCondition.from(new RepeatableAnnotationPredicate<>(Retention.class,
			(expectedTarget, actualTarget) -> expectedTarget.value() == actualTarget.value()));
	}

	private static ArchCondition<? super JavaClass> haveContainerAnnotationWithSameTargetTypes() {
		return ArchCondition.from(new RepeatableAnnotationPredicate<>(Target.class,
			(expectedTarget, actualTarget) -> Arrays.equals(expectedTarget.value(), actualTarget.value())));
	}

	static class AllJars implements LocationProvider {

		@Override
		public Set<Location> get(Class<?> testClass) {
			return loadJarFiles().map(Location::of).collect(toSet());
		}

		private static Stream<JarFile> loadJarFiles() {
			return Helper.loadModuleDirectoryNames().stream().map(AllJars::createJarFile);
		}

		private static JarFile createJarFile(String module) {
			var path = MavenRepo.jar(module);
			try {
				return new JarFile(path.toFile());
			}
			catch (IOException e) {
				throw new UncheckedIOException("Creating JarFile for '" + path + "' failed.", e);
			}
		}
	}

	private static class RepeatableAnnotationPredicate<T extends Annotation> extends DescribedPredicate<JavaClass> {

		private final Class<T> annotationType;
		private final BiPredicate<T, T> predicate;

		public RepeatableAnnotationPredicate(Class<T> annotationType, BiPredicate<T, T> predicate) {
			super("have identical @%s annotation as container annotation", annotationType.getSimpleName());
			this.annotationType = annotationType;
			this.predicate = predicate;
		}

		@Override
		public boolean test(JavaClass annotationClass) {
			var containerAnnotationClass = (JavaClass) annotationClass.getAnnotationOfType(
				Repeatable.class.getName()).get("value").orElseThrow();
			var expectedAnnotation = annotationClass.tryGetAnnotationOfType(annotationType);
			var actualAnnotation = containerAnnotationClass.tryGetAnnotationOfType(annotationType);
			return expectedAnnotation.map(expectedTarget -> actualAnnotation //
					.map(actualTarget -> predicate.test(expectedTarget, actualTarget)) //
					.orElse(false)) //
					.orElse(actualAnnotation.isEmpty());
		}
	}
}
