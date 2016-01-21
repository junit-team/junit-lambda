/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

public class MethodResolver extends JUnit5TestResolver {
	private static final Logger LOG = Logger.getLogger(MethodResolver.class.getName());

	private Pattern uniqueIdRegExPattern = Pattern.compile("^(.+?):([^#]+)#([^(]+)\\(((?:[^,)]+,?)*)\\)$");
	private ClassResolver classResolver = null;

	@Override
	public void initialize(TestEngine testEngine, TestResolverRegistry testResolverRegistry) {
		super.initialize(testEngine, testResolverRegistry);
		/*this.classResolver = testResolverRegistry.lookupTestResolver(ClassResolver.class).orElseThrow(
			() -> new IllegalStateException("Could not find ClassResolver in registry!"));*/
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		/*
				Preconditions.notNull(parent, "parent must not be null!");
				Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

				if (parent.isRoot()) {
					List<TestDescriptor> methodBasedTestMethods = resolveAllMethodsFromSpecification(parent, discoveryRequest);
					getTestResolverRegistry().notifyResolvers(methodBasedTestMethods, discoveryRequest);

					List<TestDescriptor> uniqueIdBasedTestMethods = resolveUniqueIdsFromSpecification(parent, discoveryRequest);
					getTestResolverRegistry().notifyResolvers(uniqueIdBasedTestMethods, discoveryRequest);
				}
				else if (parent instanceof ClassTestDescriptor) {
					List<TestDescriptor> resolvedTests = resolveTestMethodsOfTestClass((ClassTestDescriptor) parent);
					getTestResolverRegistry().notifyResolvers(resolvedTests, discoveryRequest);
				}
			}

			private List<TestDescriptor> resolveAllMethodsFromSpecification(TestDescriptor parent,
					EngineDiscoveryRequest discoveryRequest) {
				List<TestDescriptor> result = new LinkedList<>();
				for (MethodSelector method : discoveryRequest.getMethods()) {
					ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(classResolver.getTestEngine(),
						method.getTestClass());
					result.add(getTestDescriptorForTestMethod(classTestDescriptor, method.getTestMethod()));
				}
				return result;
			}

			private List<TestDescriptor> resolveUniqueIdsFromSpecification(TestDescriptor parent,
					EngineDiscoveryRequest discoveryRequest) {
				List<String> uniqueIds = discoveryRequest.getUniqueIds();
				List<TestDescriptor> result = new LinkedList<>();

				for (String uniqueId : uniqueIds) {
					Matcher matcher = uniqueIdRegExPattern.matcher(uniqueId);
					if (matcher.matches()) {
						try {
							String className = matcher.group(2);
							String methodName = matcher.group(3);
							String parameterTypeNames = matcher.group(4);

							Class<?> testClass = Class.forName(className);
							Optional<Method> testMethodOptional;
							if (parameterTypeNames.isEmpty()) {
								testMethodOptional = ReflectionUtils.findMethod(testClass, methodName);
							}
							else {
								Class<?>[] parameterTypes = getParameterTypes(parameterTypeNames.split(","));
								testMethodOptional = ReflectionUtils.findMethod(testClass, methodName, parameterTypes);
							}

							if (testMethodOptional.isPresent()) {
								ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(classResolver.getTestEngine(),
									testClass);
								result.add(getTestDescriptorForTestMethod(classTestDescriptor, testMethodOptional.get()));
							}
							else {
								LOG.fine(() -> "Skipping uniqueId " + uniqueId
										+ ": UniqueId does not seem to represent a valid test method.");
							}
						}
						catch (ClassNotFoundException e) {
							LOG.fine(() -> "Skipping uniqueId " + uniqueId
									+ ": UniqueId does not seem to represent a valid test method.");
						}
					}
				}

				return result;
			}

			private List<TestDescriptor> resolveTestMethodsOfTestClass(ClassTestDescriptor parent) {
				Class<?> testClass = parent.getTestClass();
				List<Method> methods = ReflectionUtils.findMethods(testClass,
					(method) -> AnnotationUtils.isAnnotated(method, Test.class));

				List<TestDescriptor> result = new LinkedList<>();
				for (Method method : methods) {
					result.add(getTestDescriptorForTestMethod(parent, method));
				}
				return result;
			}

			private TestDescriptor getTestDescriptorForTestMethod(ClassTestDescriptor parent, Method method) {
				return getTestDescriptorForTestMethod(parent, parent.getTestClass(), method);
			}

			private TestDescriptor getTestDescriptorForTestMethod(TestDescriptor parent, Class<?> testClass, Method method) {
				MethodTestDescriptor testDescriptor = new MethodTestDescriptor(getTestEngine(), testClass, method);
				parent.addChild(testDescriptor);
				return testDescriptor;
			}

			private Class<?>[] getParameterTypes(String[] parameterTypeNames) throws ClassNotFoundException {
				Class<?>[] parameterTypes = new Class[parameterTypeNames.length];
				for (int i = 0; i < parameterTypeNames.length; i++) {
					parameterTypes[i] = Class.forName(parameterTypeNames[i]);
				}
				return parameterTypes;
		*/
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}
}
