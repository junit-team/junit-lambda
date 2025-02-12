/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static java.util.Comparator.comparingInt;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
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
 * <p>A {@link MethodOrderer} can be configured <em>globally</em> for the entire
 * test suite via the {@value #DEFAULT_ORDER_PROPERTY_NAME} configuration
 * parameter (see the User Guide for details) or <em>locally</em> for a test
 * class via the {@link TestMethodOrder @TestMethodOrder} annotation.
 *
 * <h2>Built-in Implementations</h2>
 *
 * <p>JUnit Jupiter provides the following built-in {@code MethodOrderer}
 * implementations.
 *
 * <ul>
 * <li>{@link MethodName}</li>
 * <li>{@link OrderAnnotation}</li>
 * <li>{@link Random}</li>
 * </ul>
 *
 * @since 5.4
 * @see TestMethodOrder
 * @see MethodOrdererContext
 * @see #orderMethods(MethodOrdererContext)
 * @see ClassOrderer
 */
@API(status = STABLE, since = "5.7")
public interface MethodOrderer {

	/**
	 * Property name used to set the default method orderer class name: {@value}
	 *
	 * <h4>Supported Values</h4>
	 *
	 * <p>Supported values include fully qualified class names for types that
	 * implement {@link org.junit.jupiter.api.MethodOrderer}.
	 *
	 * <p>If not specified, test methods will be ordered using an algorithm that
	 * is deterministic but intentionally non-obvious.
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.9")
	String DEFAULT_ORDER_PROPERTY_NAME = "junit.jupiter.testmethod.order.default";

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
	 * }</pre>
	 *
	 * @param context the {@code MethodOrdererContext} containing the
	 * {@linkplain MethodDescriptor method descriptors} to order; never {@code null}
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
	 *
	 * @since 5.4
	 * @deprecated as of JUnit Jupiter 5.7 in favor of {@link MethodOrderer.MethodName};
	 * to be removed in 6.0
	 */
	@API(status = DEPRECATED, since = "5.7")
	@Deprecated
	class Alphanumeric extends MethodName {

		public Alphanumeric() {
		}
	}

	/**
	 * {@code MethodOrderer} that sorts methods alphanumerically based on their
	 * names using {@link String#compareTo(String)}.
	 *
	 * <p>If two methods have the same name, {@code String} representations of
	 * their formal parameter lists will be used as a fallback for comparing the
	 * methods.
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.10")
	class MethodName implements MethodOrderer {

		public MethodName() {
		}

		/**
		 * Sort the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} alphanumerically based on their names
		 * and formal parameter lists.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = Comparator.<MethodDescriptor, String> //
				comparing(descriptor -> descriptor.getMethod().getName())//
				.thenComparing(descriptor -> parameterList(descriptor.getMethod()));

		private static String parameterList(Method method) {
			return ClassUtils.nullSafeToString(method.getParameterTypes());
		}
	}

	/**
	 * {@code MethodOrderer} that sorts methods alphanumerically based on their
	 * display names using {@link String#compareTo(String)}
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.10")
	class DisplayName implements MethodOrderer {

		public DisplayName() {
		}

		/**
		 * Sort the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} alphanumerically based on their display
		 * names.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = Comparator.comparing(
			MethodDescriptor::getDisplayName);
	}

	/**
	 * {@code MethodOrderer} that sorts methods based on the {@link Order @Order}
	 * annotation.
	 *
	 * <p>Any methods that are assigned the same order value will be sorted
	 * arbitrarily adjacent to each other.
	 *
	 * <p>Any methods not annotated with {@code @Order} will be assigned the
	 * {@linkplain Order#DEFAULT default order} value which will effectively cause them
	 * to appear at the end of the sorted list, unless certain methods are assigned
	 * an explicit order value greater than the default order value. Any methods
	 * assigned an explicit order value greater than the default order value will
	 * appear after non-annotated methods in the sorted list.
	 */
	class OrderAnnotation implements MethodOrderer {

		public OrderAnnotation() {
		}

		/**
		 * Sort the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} based on the {@link Order @Order}
		 * annotation.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparingInt(OrderAnnotation::getOrder));
		}

		private static int getOrder(MethodDescriptor descriptor) {
			return descriptor.findAnnotation(Order.class).map(Order::value).orElse(Order.DEFAULT);
		}
	}

	/**
	 * {@code MethodOrderer} that orders methods pseudo-randomly.
	 *
	 * <h2>Custom Seed</h2>
	 *
	 * <p>By default, the random <em>seed</em> used for ordering methods is the
	 * value returned by {@link System#nanoTime()} during static class
	 * initialization. In order to support repeatable builds, the value of the
	 * default random seed is logged at {@code CONFIG} level. In addition, a
	 * custom seed (potentially the default seed from the previous test plan
	 * execution) may be specified via the {@value Random#RANDOM_SEED_PROPERTY_NAME}
	 * <em>configuration parameter</em> which can be supplied via the {@code Launcher}
	 * API, build tools (e.g., Gradle and Maven), a JVM system property, or the JUnit
	 * Platform configuration file (i.e., a file named {@code junit-platform.properties}
	 * in the root of the class path). Consult the User Guide for further information.
	 *
	 * @see Random#RANDOM_SEED_PROPERTY_NAME
	 * @see java.util.Random
	 */
	class Random implements MethodOrderer {

		private static final Logger logger = LoggerFactory.getLogger(Random.class);

		static {
			logger.config(() -> "MethodOrderer.Random default seed: " + RandomOrdererUtils.DEFAULT_SEED);
		}

		/**
		 * Property name used to set the random seed used by this
		 * {@code MethodOrderer}: {@value}
		 *
		 * <p>The same property is used by {@link ClassOrderer.Random} for
		 * consistency between the two random orderers.
		 *
		 * <h4>Supported Values</h4>
		 *
		 * <p>Supported values include any string that can be converted to a
		 * {@link Long} via {@link Long#valueOf(String)}.
		 *
		 * <p>If not specified or if the specified value cannot be converted to
		 * a {@link Long}, the default random seed will be used (see the
		 * {@linkplain Random class-level Javadoc} for details).
		 *
		 * @see ClassOrderer.Random
		 */
		public static final String RANDOM_SEED_PROPERTY_NAME = RandomOrdererUtils.RANDOM_SEED_PROPERTY_NAME;

		public Random() {
		}

		/**
		 * Order the methods encapsulated in the supplied
		 * {@link MethodOrdererContext} pseudo-randomly.
		 */
		@Override
		public void orderMethods(MethodOrdererContext context) {
			Collections.shuffle(context.getMethodDescriptors(),
				new java.util.Random(RandomOrdererUtils.getSeed(context::getConfigurationParameter, logger)));
		}

	}

}
