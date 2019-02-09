/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;

/**
 * {@code MethodOrderer} defines the API for ordering the <em>test methods</em>
 * in a given test class.
 *
 * <p>In this context, the term "test method" refers to any method annotated with
 * {@code @Test}, {@code @RepeatedTest}, {@code @ParameterizedTest},
 * {@code @TestFactory}, or {@code @TestTemplate}.
 *
 * <h4>Built-in Implementations</h4>
 *
 * <p>JUnit Jupiter provides the following built-in {@code MethodOrderer}
 * implementations.
 *
 * <ul>
 * <li>{@link Alphanumeric}</li>
 * <li>{@link OrderAnnotation}</li>
 * <li>{@link Random}</li>
 * </ul>
 *
 * @since 5.4
 * @see TestMethodOrder
 * @see MethodOrdererContext
 * @see #orderMethods(MethodOrdererContext)
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface MethodOrderer {

	/**
	 * Order the methods encapsulated in the supplied {@link MethodOrdererContext}.
	 *
	 * <p>The methods to order or sort are made indirectly available via
	 * {@link MethodOrdererContext#getMethodDescriptors()}. Since this method
	 * has a {@code void} return type, the list of method descriptors must be
	 * modified directly.
	 *
	 * <p>For example, a simplified implementation of the {@link Random}
	 * {@code MethodOrderer} might look like the following.
	 *
	 * <pre class="code">
	 * public void orderMethods(MethodOrdererContext context) {
	 *     Collections.shuffle(context.getMethodDescriptors());
	 * }
	 * </pre>
	 *
	 * @param context the {@code MethodOrdererContext} containing the
	 * {@link MethodDescriptor method descriptors} to order; never {@code null}
	 * @see #getDefaultExecutionMode()
	 */
	void orderMethods(MethodOrdererContext context);

	/**
	 * Get the <em>default</em> {@link ExecutionMode} for the test class
	 * configured with this {@link MethodOrderer}.
	 *
	 * <p>This method is guaranteed to be invoked after
	 * {@link #orderMethods(MethodOrdererContext)} which allows implementations
	 * of this method to determine the appropriate return value programmatically,
	 * potentially based on actions that were taken in {@code orderMethods()}.
	 *
	 * <p>Defaults to {@link ExecutionMode#SAME_THREAD SAME_THREAD}, since
	 * ordered methods are typically sorted in a fashion that would conflict
	 * with concurrent execution.
	 *
	 * <p>In case the ordering does not conflict with concurrent execution,
	 * implementations should return an empty {@link Optional} to signal that
	 * the engine should decide which execution mode to use.
	 *
	 * <p>Can be overridden via an explicit
	 * {@link org.junit.jupiter.api.parallel.Execution @Execution} declaration
	 * on the test class or in concrete implementations of the
	 * {@code MethodOrderer} API.
	 *
	 * @return the default {@code ExecutionMode}; never {@code null} but
	 * potentially empty
	 * @see #orderMethods(MethodOrdererContext)
	 */
	default Optional<ExecutionMode> getDefaultExecutionMode() {
		return Optional.of(ExecutionMode.SAME_THREAD);
	}

	/**
	 * {@code MethodOrderer} that sorts methods alphanumerically based on their
	 * names using {@link String#compareTo(String)}.
	 *
	 * <p>If two methods have the same name, {@code String} representations of
	 * their formal parameter lists will be used as a fallback for comparing the
	 * methods.
	 */
	class Alphanumeric implements MethodOrderer {

		/**
		 * Sort the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} alphanumerically based on their names
		 * and formal parameter lists.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = (descriptor1, descriptor2) -> {
			Method method1 = descriptor1.getMethod();
			Method method2 = descriptor2.getMethod();

			int result = method1.getName().compareTo(method2.getName());
			if (result != 0) {
				return result;
			}

			// else
			return parameterList(method1).compareTo(parameterList(method2));
		};

		private static String parameterList(Method method) {
			return ClassUtils.nullSafeToString(method.getParameterTypes());
		}
	}

	/**
	 * {@code MethodOrderer} that sorts methods based on the {@link Order @Order}
	 * annotation.
	 *
	 * <p>Any methods that are assigned the same order value will be sorted
	 * arbitrarily adjacent to each other.
	 *
	 * <p>Any methods not annotated with {@code @Order} will be assigned a default
	 * order value of {@link Integer#MAX_VALUE} which will effectively cause them to
	 * appear at the end of the sorted list.
	 */
	class OrderAnnotation implements MethodOrderer {

		/**
		 * Sort the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} based on the {@link Order @Order}
		 * annotation.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = //
			(descriptor1, descriptor2) -> Integer.compare(getOrder(descriptor1), getOrder(descriptor2));

		private static int getOrder(MethodDescriptor descriptor) {
			return descriptor.findAnnotation(Order.class).map(Order::value).orElse(Integer.MAX_VALUE);
		}
	}

	/**
	 * {@code MethodOrderer} that sorts methods based on the {@link DependsOn @DependsOn}
	 * annotation.
	 */
	class DependsOnAnnotation implements MethodOrderer {
		private static final Logger logger = LoggerFactory.getLogger(Random.class);

		@Override
		public void orderMethods(MethodOrdererContext context) {
			// Directed Acyclic Graph to represent order relationship between methods
			// An edge from A -> B means method A should run after B
			Map<String, String[]> digraph = context.getMethodDescriptors().stream().filter(
				descriptor -> descriptor.isAnnotated(DependsOn.class)).collect(
					toMap(descriptor -> descriptor.getMethod().getName(),
						descriptor -> descriptor.findAnnotation(DependsOn.class).map(DependsOn::value).get()));

			// Give each an @Order's value equivalent to its number of previous dependencies
			Map<String, Integer> dependencySize = new HashMap<>();

			try {
				// loop through all vertexes (methods) in graph
				digraph.keySet().forEach(name -> {
					if (!dependencySize.containsKey(name)) {
						depthFirstSearch(name, digraph, dependencySize);
					}
				});
			}
			catch (IllegalArgumentException exception) {
				logger.error(exception,
					() -> "ERROR - Some arguments from @DependsOn annotations form cyclic dependencies, which would cause undefined behavior!");
			}

			// default value = 0 to make independent test run first, give it MAX_INT to make it run last
			context.getMethodDescriptors().sort(
				Comparator.comparing(descriptor -> dependencySize.getOrDefault(descriptor.getMethod().getName(), 0)));
		}

		/**
		 * This method will compute the (number of methods that must be run before the annotated method) + 1 (+1 to simplify computation)
		 */
		private int depthFirstSearch(String name, Map<String, String[]> digraph, Map<String, Integer> dependencySize) {
			if (dependencySize.containsKey(name)) {
				return dependencySize.get(name);
			}
			// mark entering this node
			dependencySize.put(name, -1);

			String[] ancestors = digraph.get(name);
			int total = 1;

			if (ancestors != null) {
				for (String ancestor : ancestors) {
					int sz = depthFirstSearch(ancestor, digraph, dependencySize);
					// cycle detected, -1 means in process but not yet finished -> should not appear
					if (sz == -1) {
						throw new IllegalArgumentException(
							String.format("Cycle detected between %s and %s", name, ancestor));
					}
					total += sz;
				}
			}

			// update with correct value
			dependencySize.put(name, total);
			return total;
		}
	}

	/**
	 * {@code MethodOrderer} that orders methods pseudo-randomly and allows for
	 * concurrent execution by default.
	 *
	 * <h4>Custom Seed</h4>
	 *
	 * <p>By default, the random <em>seed</em> used for ordering methods is the
	 * value returned by {@link System#nanoTime()}. In order to produce repeatable
	 * builds, a custom seed may be specified via the
	 * {@link Random#RANDOM_SEED_PROPERTY_NAME junit.jupiter.execution.order.random.seed}
	 * <em>configuration parameter</em> which can be supplied via the
	 * {@code Launcher} API, build tools (e.g., Gradle and Maven), a JVM system
	 * property, or the JUnit Platform configuration file (i.e., a file named
	 * {@code junit-platform.properties} in the root of the class path). Consult
	 * the User Guide for further information.
	 *
	 * @see #getDefaultExecutionMode()
	 * @see Random#RANDOM_SEED_PROPERTY_NAME
	 * @see java.util.Random
	 */
	class Random implements MethodOrderer {

		private static final Logger logger = LoggerFactory.getLogger(Random.class);

		/**
		 * Property name used to set the random seed used by this
		 * {@code MethodOrderer}: {@value}
		 *
		 * <h3>Supported Values</h3>
		 *
		 * <p>Supported values include any string that can be converted to a
		 * {@link Long} via {@link Long#valueOf(String)}.
		 *
		 * <p>If not specified or if the specified value cannot be converted to
		 * a {@code Long}, {@link System#nanoTime()} will be used as the random
		 * seed.
		 */
		public static final String RANDOM_SEED_PROPERTY_NAME = "junit.jupiter.execution.order.random.seed";

		private boolean usingCustomSeed = false;

		/**
		 * Order the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} pseudo-randomly.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			Long seed = null;

			Optional<String> configurationParameter = context.getConfigurationParameter(RANDOM_SEED_PROPERTY_NAME);
			if (configurationParameter.isPresent()) {
				String value = configurationParameter.get();
				try {
					seed = Long.valueOf(value);
					this.usingCustomSeed = true;
					logger.config(
						() -> String.format("Using custom seed for configuration parameter [%s] with value [%s].",
							RANDOM_SEED_PROPERTY_NAME, value));
				}
				catch (NumberFormatException ex) {
					logger.warn(ex,
						() -> String.format("Failed to convert configuration parameter [%s] with value [%s] to a long. "
								+ "Using System.nanoTime() as fallback.",
							RANDOM_SEED_PROPERTY_NAME, value));
				}
			}

			if (seed == null) {
				seed = System.nanoTime();
			}

			Collections.shuffle(context.getMethodDescriptors(), new java.util.Random(seed));
		}

		/**
		 * Get the <em>default</em> {@link ExecutionMode} for the test class.
		 *
		 * <p>If a custom seed has been specified, this method returns
		 * {@link ExecutionMode#SAME_THREAD SAME_THREAD} in order to ensure that
		 * the results are repeatable across executions of the test plan.
		 * Otherwise, this method returns {@link ExecutionMode#CONCURRENT
		 * CONCURRENT} to allow concurrent execution of randomly ordered methods
		 * by default.
		 *
		 * @return {@code SAME_THREAD} if a custom seed has been configured;
		 * otherwise, {@code CONCURRENT}
		 */
		@Override
		public Optional<ExecutionMode> getDefaultExecutionMode() {
			return this.usingCustomSeed ? Optional.of(ExecutionMode.SAME_THREAD) : Optional.empty();
		}
	}

}
