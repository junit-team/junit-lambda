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

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.getEnclosingTestClasses;
import static org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests.isTestOrTestFactoryOrTestTemplateMethod;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.findMethods;
import static org.junit.platform.commons.support.ReflectionSupport.streamNestedClasses;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.Filterable;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestClassAware;
import org.junit.jupiter.engine.discovery.predicates.IsNestedTestClass;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.NestedClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * @since 5.5
 */
class ClassSelectorResolver implements SelectorResolver {

	private static final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();
	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();
	private static final Predicate<Class<?>> isAnnotatedWithContainerTemplate = testClass -> isAnnotated(testClass,
		ContainerTemplate.class);

	private final Predicate<String> classNameFilter;
	private final JupiterConfiguration configuration;

	ClassSelectorResolver(Predicate<String> classNameFilter, JupiterConfiguration configuration) {
		this.classNameFilter = classNameFilter;
		this.configuration = configuration;
	}

	@Override
	public Resolution resolve(ClassSelector selector, Context context) {
		Class<?> testClass = selector.getJavaClass();
		if (isTestClassWithTests.test(testClass)) {
			// Nested tests are never filtered out
			if (classNameFilter.test(testClass.getName())) {
				return toResolution(
					context.addToParent(parent -> Optional.of(newStaticClassTestDescriptor(parent, testClass))));
			}
		}
		else if (isNestedTestClass.test(testClass)) {
			return toResolution(context.addToParent(() -> DiscoverySelectors.selectClass(testClass.getEnclosingClass()),
				parent -> Optional.of(newMemberClassTestDescriptor(parent, testClass))));
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(NestedClassSelector selector, Context context) {
		if (isNestedTestClass.test(selector.getNestedClass())) {
			return toResolution(context.addToParent(() -> selectClass(selector.getEnclosingClasses()),
				parent -> Optional.of(newMemberClassTestDescriptor(parent, selector.getNestedClass()))));
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {
		UniqueId uniqueId = selector.getUniqueId();
		UniqueId.Segment lastSegment = uniqueId.getLastSegment();
		if (ClassTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			return resolveStaticClassUniqueId(context, lastSegment, __ -> true, this::newClassTestDescriptor);
		}
		if (ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE.equals(lastSegment.getType())) {
			return resolveStaticClassUniqueId(context, lastSegment, isAnnotatedWithContainerTemplate,
				this::newStaticContainerTemplateTestDescriptor);
		}
		if (NestedClassTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			return resolveNestedClassUniqueId(context, uniqueId, __ -> true, this::newNestedClassTestDescriptor);
		}
		if (ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE.equals(lastSegment.getType())) {
			return resolveNestedClassUniqueId(context, uniqueId, isAnnotatedWithContainerTemplate,
				this::newNestedContainerTemplateTestDescriptor);
		}
		if (ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			Optional<ContainerTemplateInvocationTestDescriptor> testDescriptor = context.addToParent(
				() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
					int index = Integer.parseInt(lastSegment.getValue().substring(1));
					return Optional.of(newDummyContainerTemplateInvocationTestDescriptor(parent, index));
				});
			return toInvocationMatch(testDescriptor) //
					.map(Resolution::match) //
					.orElse(unresolved());
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(IterationSelector selector, Context context) {
		DiscoverySelector parentSelector = selector.getParentSelector();
		if (parentSelector instanceof ClassSelector
				&& isAnnotatedWithContainerTemplate.test(((ClassSelector) parentSelector).getJavaClass())) {
			return resolveIterations(selector, context);
		}
		if (parentSelector instanceof NestedClassSelector
				&& isAnnotatedWithContainerTemplate.test(((NestedClassSelector) parentSelector).getNestedClass())) {
			return resolveIterations(selector, context);
		}
		return unresolved();
	}

	private Resolution resolveIterations(IterationSelector selector, Context context) {
		DiscoverySelector parentSelector = selector.getParentSelector();
		Set<Match> matches = selector.getIterationIndices().stream() //
				.map(index -> context.addToParent(() -> parentSelector,
					parent -> Optional.of(newDummyContainerTemplateInvocationTestDescriptor(parent, index + 1)))) //
				.map(this::toInvocationMatch) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(toSet());
		return matches.isEmpty() ? unresolved() : Resolution.matches(matches);
	}

	private Resolution resolveStaticClassUniqueId(Context context, UniqueId.Segment lastSegment,
			Predicate<? super Class<?>> condition,
			BiFunction<TestDescriptor, Class<?>, ClassBasedTestDescriptor> factory) {

		String className = lastSegment.getValue();
		return ReflectionSupport.tryToLoadClass(className).toOptional() //
				.filter(isTestClassWithTests) //
				.filter(condition) //
				.map(testClass -> toResolution(
					context.addToParent(parent -> Optional.of(factory.apply(parent, testClass))))) //
				.orElse(unresolved());
	}

	private Resolution resolveNestedClassUniqueId(Context context, UniqueId uniqueId,
			Predicate<? super Class<?>> condition,
			BiFunction<TestDescriptor, Class<?>, ClassBasedTestDescriptor> factory) {

		String simpleClassName = uniqueId.getLastSegment().getValue();
		return toResolution(context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
			Class<?> parentTestClass = ((TestClassAware) parent).getTestClass();
			return ReflectionSupport.findNestedClasses(parentTestClass,
				isNestedTestClass.and(where(Class::getSimpleName, isEqual(simpleClassName)))).stream() //
					.findFirst() //
					.filter(condition) //
					.map(testClass -> factory.apply(parent, testClass));
		}));
	}

	private ContainerTemplateInvocationTestDescriptor newDummyContainerTemplateInvocationTestDescriptor(
			TestDescriptor parent, int index) {
		UniqueId uniqueId = parent.getUniqueId().append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#" + index);
		return new ContainerTemplateInvocationTestDescriptor(uniqueId, (ContainerTemplateTestDescriptor) parent,
			DummyContainerTemplateInvocationContext.INSTANCE, index, parent.getSource().orElse(null), configuration);
	}

	private ClassBasedTestDescriptor newStaticClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return isAnnotatedWithContainerTemplate.test(testClass) //
				? newStaticContainerTemplateTestDescriptor(parent, testClass) //
				: newClassTestDescriptor(parent, testClass);
	}

	private ContainerTemplateTestDescriptor newStaticContainerTemplateTestDescriptor(TestDescriptor parent,
			Class<?> testClass) {
		return newContainerTemplateTestDescriptor(parent, ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			newClassTestDescriptor(parent, testClass));
	}

	private ClassTestDescriptor newClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return new ClassTestDescriptor(
			parent.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass,
			configuration);
	}

	private ClassBasedTestDescriptor newMemberClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return isAnnotatedWithContainerTemplate.test(testClass) //
				? newNestedContainerTemplateTestDescriptor(parent, testClass) //
				: newNestedClassTestDescriptor(parent, testClass);
	}

	private ContainerTemplateTestDescriptor newNestedContainerTemplateTestDescriptor(TestDescriptor parent,
			Class<?> testClass) {
		return newContainerTemplateTestDescriptor(parent, ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE,
			newNestedClassTestDescriptor(parent, testClass));
	}

	private NestedClassTestDescriptor newNestedClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		UniqueId uniqueId = parent.getUniqueId().append(NestedClassTestDescriptor.SEGMENT_TYPE,
			testClass.getSimpleName());
		return new NestedClassTestDescriptor(uniqueId, testClass, () -> getEnclosingTestClasses(parent), configuration);
	}

	private ContainerTemplateTestDescriptor newContainerTemplateTestDescriptor(TestDescriptor parent,
			String segmentType, ClassBasedTestDescriptor delegate) {

		delegate.setParent(parent);
		String segmentValue = delegate.getUniqueId().getLastSegment().getValue();
		UniqueId uniqueId = parent.getUniqueId().append(segmentType, segmentValue);
		return new ContainerTemplateTestDescriptor(uniqueId, delegate);
	}

	private Optional<Match> toInvocationMatch(Optional<ContainerTemplateInvocationTestDescriptor> testDescriptor) {
		return testDescriptor //
				.map(it -> Match.exact(it, expansionCallback(it,
					() -> it.getParent().map(parent -> getTestClasses((TestClassAware) parent)).orElse(emptyList()))));
	}

	private Resolution toResolution(Optional<? extends ClassBasedTestDescriptor> testDescriptor) {
		return testDescriptor //
				.map(it -> Resolution.match(Match.exact(it, expansionCallback(it)))) //
				.orElse(unresolved());
	}

	private Supplier<Set<? extends DiscoverySelector>> expansionCallback(ClassBasedTestDescriptor testDescriptor) {
		return expansionCallback(testDescriptor, () -> getTestClasses(testDescriptor));
	}

	private static List<Class<?>> getTestClasses(TestClassAware testDescriptor) {
		List<Class<?>> testClasses = new ArrayList<>(testDescriptor.getEnclosingTestClasses());
		testClasses.add(testDescriptor.getTestClass());
		return testClasses;
	}

	private Supplier<Set<? extends DiscoverySelector>> expansionCallback(TestDescriptor testDescriptor,
			Supplier<List<Class<?>>> testClassesSupplier) {
		return () -> {
			if (testDescriptor instanceof Filterable) {
				Filterable filterable = (Filterable) testDescriptor;
				filterable.getDynamicDescendantFilter().allowAll();
			}
			List<Class<?>> testClasses = testClassesSupplier.get();
			Class<?> testClass = testClasses.get(testClasses.size() - 1);
			Stream<DiscoverySelector> methods = findMethods(testClass, isTestOrTestFactoryOrTestTemplateMethod,
				TOP_DOWN).stream().map(method -> selectMethod(testClasses, method));
			Stream<NestedClassSelector> nestedClasses = streamNestedClasses(testClass, isNestedTestClass).map(
				nestedClass -> DiscoverySelectors.selectNestedClass(testClasses, nestedClass));
			return Stream.concat(methods, nestedClasses).collect(
				toCollection((Supplier<Set<DiscoverySelector>>) LinkedHashSet::new));
		};
	}

	private DiscoverySelector selectClass(List<Class<?>> classes) {
		if (classes.size() == 1) {
			return DiscoverySelectors.selectClass(classes.get(0));
		}
		int lastIndex = classes.size() - 1;
		return DiscoverySelectors.selectNestedClass(classes.subList(0, lastIndex), classes.get(lastIndex));
	}

	private DiscoverySelector selectMethod(List<Class<?>> classes, Method method) {
		if (classes.size() == 1) {
			return DiscoverySelectors.selectMethod(classes.get(0), method);
		}
		int lastIndex = classes.size() - 1;
		return DiscoverySelectors.selectNestedMethod(classes.subList(0, lastIndex), classes.get(lastIndex), method);
	}

	static class DummyContainerTemplateInvocationContext implements ContainerTemplateInvocationContext {
		private static final DummyContainerTemplateInvocationContext INSTANCE = new DummyContainerTemplateInvocationContext();
	}
}
