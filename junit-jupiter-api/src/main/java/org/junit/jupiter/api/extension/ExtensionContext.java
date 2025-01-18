/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code ExtensionContext} encapsulates the <em>context</em> in which the
 * current test or container is being executed.
 *
 * <p>{@link Extension Extensions} are provided an instance of
 * {@code ExtensionContext} to perform their work.
 *
 * @since 5.0
 * @see Store
 * @see Namespace
 */
@API(status = STABLE, since = "5.0")
public interface ExtensionContext {

	/**
	 * Get the parent extension context, if available.
	 *
	 * @return an {@code Optional} containing the parent; never {@code null} but
	 * potentially empty
	 * @see #getRoot()
	 */
	Optional<ExtensionContext> getParent();

	/**
	 * Get the <em>root</em> {@code ExtensionContext}.
	 *
	 * @return the root extension context; never {@code null} but potentially
	 * <em>this</em> {@code ExtensionContext}
	 * @see #getParent()
	 */
	ExtensionContext getRoot();

	/**
	 * Get the unique ID of the current test or container.
	 *
	 * @return the unique ID of the test or container; never {@code null} or blank
	 */
	String getUniqueId();

	/**
	 * Get the display name for the current test or container.
	 *
	 * <p>The display name is either a default name or a custom name configured
	 * via {@link org.junit.jupiter.api.DisplayName @DisplayName}.
	 *
	 * <p>For details on default display names consult the Javadoc for
	 * {@link org.junit.jupiter.api.TestInfo#getDisplayName()}.
	 *
	 * <p>Note that display names are typically used for test reporting in IDEs
	 * and build tools and may contain spaces, special characters, and even emoji.
	 *
	 * @return the display name of the test or container; never {@code null} or blank
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags for the current test or container.
	 *
	 * <p>Tags may be declared directly on the test element or <em>inherited</em>
	 * from an outer context.
	 *
	 * @return the set of tags for the test or container; never {@code null} but
	 * potentially empty
	 */
	Set<String> getTags();

	/**
	 * Get the {@link AnnotatedElement} corresponding to the current extension
	 * context, if available.
	 *
	 * <p>For example, if the current extension context encapsulates a test
	 * class, test method, test factory method, or test template method, the
	 * annotated element will be the corresponding {@link Class} or {@link Method}
	 * reference.
	 *
	 * <p>Favor this method over more specific methods whenever the
	 * {@code AnnotatedElement} API suits the task at hand &mdash; for example,
	 * when looking up annotations regardless of concrete element type.
	 *
	 * @return an {@code Optional} containing the {@code AnnotatedElement};
	 * never {@code null} but potentially empty
	 * @see #getTestClass()
	 * @see #getTestMethod()
	 */
	Optional<AnnotatedElement> getElement();

	/**
	 * Get the {@link Class} associated with the current test or container,
	 * if available.
	 *
	 * @return an {@code Optional} containing the class; never {@code null} but
	 * potentially empty
	 * @see #getRequiredTestClass()
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the <em>required</em> {@link Class} associated with the current test
	 * or container.
	 *
	 * <p>Use this method as an alternative to {@link #getTestClass()} for use
	 * cases in which the test class is required to be present.
	 *
	 * @return the test class; never {@code null}
	 * @throws PreconditionViolationException if the test class is not present
	 * in this {@code ExtensionContext}
	 */
	default Class<?> getRequiredTestClass() {
		return Preconditions.notNull(getTestClass().orElse(null),
			"Illegal state: required test class is not present in the current ExtensionContext");
	}

	/**
	 * Get the {@link Lifecycle} of the {@linkplain #getTestInstance() test
	 * instance} associated with the current test or container, if available.
	 *
	 * @return an {@code Optional} containing the test instance {@code Lifecycle};
	 * never {@code null} but potentially empty
	 * @since 5.1
	 * @see org.junit.jupiter.api.TestInstance {@code @TestInstance}
	 */
	@API(status = STABLE, since = "5.1")
	Optional<Lifecycle> getTestInstanceLifecycle();

	/**
	 * Get the test instance associated with the current test or container,
	 * if available.
	 *
	 * @return an {@code Optional} containing the test instance; never
	 * {@code null} but potentially empty
	 * @see #getRequiredTestInstance()
	 * @see #getTestInstances()
	 */
	Optional<Object> getTestInstance();

	/**
	 * Get the <em>required</em> test instance associated with the current test
	 * or container.
	 *
	 * <p>Use this method as an alternative to {@link #getTestInstance()} for use
	 * cases in which the test instance is required to be present.
	 *
	 * @return the test instance; never {@code null}
	 * @throws PreconditionViolationException if the test instance is not present
	 * in this {@code ExtensionContext}
	 *
	 * @see #getRequiredTestInstances()
	 */
	default Object getRequiredTestInstance() {
		return Preconditions.notNull(getTestInstance().orElse(null),
			"Illegal state: required test instance is not present in the current ExtensionContext");
	}

	/**
	 * Get the test instances associated with the current test or container,
	 * if available.
	 *
	 * <p>While top-level tests only have a single test instance, nested tests
	 * have one additional instance for each enclosing test class.
	 *
	 * @return an {@code Optional} containing the test instances; never
	 * {@code null} but potentially empty
	 * @since 5.4
	 * @see #getRequiredTestInstances()
	 */
	@API(status = STABLE, since = "5.7")
	Optional<TestInstances> getTestInstances();

	/**
	 * Get the <em>required</em> test instances associated with the current test
	 * or container.
	 *
	 * <p>Use this method as an alternative to {@link #getTestInstances()} for use
	 * cases in which the test instances are required to be present.
	 *
	 * @return the test instances; never {@code null}
	 * @throws PreconditionViolationException if the test instances are not present
	 * in this {@code ExtensionContext}
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.7")
	default TestInstances getRequiredTestInstances() {
		return Preconditions.notNull(getTestInstances().orElse(null),
			"Illegal state: required test instances are not present in the current ExtensionContext");
	}

	/**
	 * Get the {@link Method} associated with the current test, if available.
	 *
	 * @return an {@code Optional} containing the method; never {@code null} but
	 * potentially empty
	 * @see #getRequiredTestMethod()
	 */
	Optional<Method> getTestMethod();

	/**
	 * Get the <em>required</em> {@link Method} associated with the current test
	 * or container.
	 *
	 * <p>Use this method as an alternative to {@link #getTestMethod()} for use
	 * cases in which the test method is required to be present.
	 *
	 * @return the test method; never {@code null}
	 * @throws PreconditionViolationException if the test method is not present
	 * in this {@code ExtensionContext}
	 */
	default Method getRequiredTestMethod() {
		return Preconditions.notNull(getTestMethod().orElse(null),
			"Illegal state: required test method is not present in the current ExtensionContext");
	}

	/**
	 * Get the exception that was thrown during execution of the test or container
	 * associated with this {@code ExtensionContext}, if available.
	 *
	 * <p>This method is typically used for logging and tracing purposes. If you
	 * wish to actually <em>handle</em> an exception thrown during test execution,
	 * implement the {@link TestExecutionExceptionHandler} API.
	 *
	 * <p>Unlike the exception passed to a {@code TestExecutionExceptionHandler},
	 * an <em>execution exception</em> returned by this method can be any
	 * exception thrown during the invocation of a {@code @Test} method, its
	 * surrounding {@code @BeforeEach} and {@code @AfterEach} methods, or a
	 * test-level {@link Extension}. Similarly, if this {@code ExtensionContext}
	 * represents a test class, the <em>execution exception</em> returned by
	 * this method can be any exception thrown in a {@code @BeforeAll} or
	 * {@code AfterAll} method or a class-level {@link Extension}.
	 *
	 * <p>Note, however, that this method will never return an exception
	 * swallowed by a {@code TestExecutionExceptionHandler}. Furthermore, if
	 * multiple exceptions have been thrown during test execution, the exception
	 * returned by this method will be the first such exception with all
	 * additional exceptions {@linkplain Throwable#addSuppressed(Throwable)
	 * suppressed} in the first one.
	 *
	 * @return an {@code Optional} containing the exception thrown; never
	 * {@code null} but potentially empty if test execution has not (yet)
	 * resulted in an exception
	 */
	Optional<Throwable> getExecutionException();

	/**
	 * Get the configuration parameter stored under the specified {@code key}.
	 *
	 * <p>If no such key is present in the {@code ConfigurationParameters} for
	 * the JUnit Platform, an attempt will be made to look up the value as a
	 * JVM system property. If no such system property exists, an attempt will
	 * be made to look up the value in the JUnit Platform properties file.
	 *
	 * @param key the key to look up; never {@code null} or blank
	 * @return an {@code Optional} containing the value; never {@code null}
	 * but potentially empty
	 *
	 * @since 5.1
	 * @see System#getProperty(String)
	 * @see org.junit.platform.engine.ConfigurationParameters
	 */
	@API(status = STABLE, since = "5.1")
	Optional<String> getConfigurationParameter(String key);

	/**
	 * Get and transform the configuration parameter stored under the specified
	 * {@code key} using the specified {@code transformer}.
	 *
	 * <p>If no such key is present in the {@code ConfigurationParameters} for
	 * the JUnit Platform, an attempt will be made to look up the value as a
	 * JVM system property. If no such system property exists, an attempt will
	 * be made to look up the value in the JUnit Platform properties file.
	 *
	 * <p>In case the transformer throws an exception, it will be wrapped in a
	 * {@link org.junit.platform.commons.JUnitException} with a helpful message.
	 *
	 * @param key the key to look up; never {@code null} or blank
	 * @param transformer the transformer to apply in case a value is found;
	 * never {@code null}
	 * @return an {@code Optional} containing the value; never {@code null}
	 * but potentially empty
	 *
	 * @since 5.7
	 * @see System#getProperty(String)
	 * @see org.junit.platform.engine.ConfigurationParameters
	 */
	@API(status = STABLE, since = "5.10")
	<T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer);

	/**
	 * Publish a map of key-value pairs to be consumed by an
	 * {@code org.junit.platform.engine.EngineExecutionListener} in order to
	 * supply additional information to the reporting infrastructure.
	 *
	 * @param map the key-value pairs to be published; never {@code null};
	 * keys and values within entries in the map also must not be
	 * {@code null} or blank
	 * @see #publishReportEntry(String, String)
	 * @see #publishReportEntry(String)
	 * @see org.junit.platform.engine.EngineExecutionListener#reportingEntryPublished
	 */
	void publishReportEntry(Map<String, String> map);

	/**
	 * Publish the specified key-value pair to be consumed by an
	 * {@code org.junit.platform.engine.EngineExecutionListener} in order to
	 * supply additional information to the reporting infrastructure.
	 *
	 * @param key the key of the published pair; never {@code null} or blank
	 * @param value the value of the published pair; never {@code null} or blank
	 * @see #publishReportEntry(Map)
	 * @see #publishReportEntry(String)
	 * @see org.junit.platform.engine.EngineExecutionListener#reportingEntryPublished
	 */
	default void publishReportEntry(String key, String value) {
		this.publishReportEntry(Collections.singletonMap(key, value));
	}

	/**
	 * Publish the specified value to be consumed by an
	 * {@code org.junit.platform.engine.EngineExecutionListener} in order to
	 * supply additional information to the reporting infrastructure.
	 *
	 * <p>This method delegates to {@link #publishReportEntry(String, String)},
	 * supplying {@code "value"} as the key and the supplied {@code value}
	 * argument as the value.
	 *
	 * @param value the value to be published; never {@code null} or blank
	 * @since 5.3
	 * @see #publishReportEntry(Map)
	 * @see #publishReportEntry(String, String)
	 * @see org.junit.platform.engine.EngineExecutionListener#reportingEntryPublished
	 */
	@API(status = STABLE, since = "5.3")
	default void publishReportEntry(String value) {
		this.publishReportEntry("value", value);
	}

	/**
	 * Publish a file with the supplied name written by the supplied action and
	 * attach it to the current test or container.
	 * <p>
	 * The file will be resolved in the report output directory prior to
	 * invoking the supplied action.
	 *
	 * @param name      the name of the file to be attached; never {@code null}
	 *                  or blank and must not contain any path separators
	 * @param mediaType the media type of the file; never {@code null}; use
	 *                  {@link MediaType#APPLICATION_OCTET_STREAM} if unknown
	 * @param action    the action to be executed to write the file; never {@code null}
	 * @since 5.12
	 * @see org.junit.platform.engine.EngineExecutionListener#fileEntryPublished
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	void publishFile(String name, MediaType mediaType, ThrowingConsumer<Path> action);

	/**
	 * Publish a directory with the supplied name written by the supplied action
	 * and attach it to the current test or container.
	 * <p>
	 * The directory will be resolved and created in the report output directory
	 * prior to invoking the supplied action.
	 *
	 * @param name   the name of the directory to be attached; never {@code null}
	 *               or blank and must not contain any path separators
	 * @param action the action to be executed to write the file; never {@code null}
	 * @since 5.12
	 * @see org.junit.platform.engine.EngineExecutionListener#fileEntryPublished
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	void publishDirectory(String name, ThrowingConsumer<Path> action);

	/**
	 * Get the {@link Store} for the supplied {@link Namespace}.
	 *
	 * <p>Use {@code getStore(Namespace.GLOBAL)} to get the default, global {@link Namespace}.
	 *
	 * <p>A store is bound to its extension context lifecycle. When an extension
	 * context lifecycle ends it closes its associated store. All stored values
	 * that are instances of {@link ExtensionContext.Store.CloseableResource} are
	 * notified by invoking their {@code close()} methods.
	 *
	 * @param namespace the {@code Namespace} to get the store for; never {@code null}
	 * @return the store in which to put and get objects for other invocations
	 * working in the same namespace; never {@code null}
	 * @see Namespace#GLOBAL
	 */
	Store getStore(Namespace namespace);

	/**
	 * Returns the store for session-level data.
	 *
	 * <p>This store is used to store data that is scoped to the session level.
	 * The data stored in this store will be available throughout the entire session.
	 *
	 * @return the store for session-level data
	 * @since 5.13
	 */
	Store getSessionLevelStore();

	/**
	 * Returns the store for request-level data.
	 *
	 * <p>This store is used to store data that is scoped to the request level.
	 * The data stored in this store will be available only for the duration of the current request.
	 *
	 * @return the store for request-level data
	 * @since 5.13
	 */
	Store getRequestLevelStore();

	/**
	 * Get the {@link ExecutionMode} associated with the current test or container.
	 *
	 * @return the {@code ExecutionMode} of the test; never {@code null}
	 *
	 * @since 5.8.1
	 * @see org.junit.jupiter.api.parallel.ExecutionMode {@code @ExecutionMode}
	 */
	@API(status = STABLE, since = "5.8.1")
	ExecutionMode getExecutionMode();

	/**
	 * Get an {@link ExecutableInvoker} to invoke methods and constructors
	 * with support for dynamic resolution of parameters.
	 *
	 * @since 5.9
	 */
	@API(status = STABLE, since = "5.11")
	ExecutableInvoker getExecutableInvoker();

	/**
	 * {@code Store} provides methods for extensions to save and retrieve data.
	 */
	interface Store {

		/**
		 * Classes implementing this interface indicate that they want to {@link #close}
		 * some underlying resource or resources when the enclosing {@link Store Store}
		 * is closed.
		 *
		 * <p>Note that the {@code CloseableResource} API is only honored for
		 * objects stored within an extension context {@link Store Store}.
		 *
		 * <p>The resources stored in a {@link Store Store} are closed in the
		 * inverse order they were added in.
		 *
		 * @since 5.1
		 */
		@API(status = STABLE, since = "5.1")
		interface CloseableResource {

			/**
			 * Close underlying resources.
			 *
			 * @throws Throwable any throwable will be caught and rethrown
			 */
			void close() throws Throwable;

		}

		/**
		 * Get the value that is stored under the supplied {@code key}.
		 *
		 * <p>If no value is stored in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the {@code Namespace} used
		 * to create this store.
		 *
		 * <p>For greater type safety, consider using {@link #get(Object, Class)}
		 * instead.
		 *
		 * @param key the key; never {@code null}
		 * @return the value; potentially {@code null}
		 * @see #get(Object, Class)
		 * @see #getOrDefault(Object, Class, Object)
		 */
		Object get(Object key);

		/**
		 * Get the value of the specified required type that is stored under
		 * the supplied {@code key}.
		 *
		 * <p>If no value is stored in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the {@code Namespace} used
		 * to create this store.
		 *
		 * @param key the key; never {@code null}
		 * @param requiredType the required type of the value; never {@code null}
		 * @param <V> the value type
		 * @return the value; potentially {@code null}
		 * @see #get(Object)
		 * @see #getOrDefault(Object, Class, Object)
		 */
		<V> V get(Object key, Class<V> requiredType);

		/**
		 * Get the value of the specified required type that is stored under
		 * the supplied {@code key}, or the supplied {@code defaultValue} if no
		 * value is found for the supplied {@code key} in this store or in an
		 * ancestor.
		 *
		 * <p>If no value is stored in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the {@code Namespace} used
		 * to create this store.
		 *
		 * @param key the key; never {@code null}
		 * @param requiredType the required type of the value; never {@code null}
		 * @param defaultValue the default value
		 * @param <V> the value type
		 * @return the value; potentially {@code null}
		 * @since 5.5
		 * @see #get(Object, Class)
		 */
		@API(status = STABLE, since = "5.5")
		default <V> V getOrDefault(Object key, Class<V> requiredType, V defaultValue) {
			V value = get(key, requiredType);
			return (value != null ? value : defaultValue);
		}

		/**
		 * Get the object of type {@code type} that is present in this
		 * {@code Store} (<em>keyed</em> by {@code type}); and otherwise invoke
		 * the default constructor for {@code type} to generate the object,
		 * store it, and return it.
		 *
		 * <p>This method is a shortcut for the following, where {@code X} is
		 * the type of object we wish to retrieve from the store.
		 *
		 * <pre style="code">
		 * X x = store.getOrComputeIfAbsent(X.class, key -&gt; new X(), X.class);
		 * // Equivalent to:
		 * // X x = store.getOrComputeIfAbsent(X.class);
		 * </pre>
		 *
		 * <p>See {@link #getOrComputeIfAbsent(Object, Function, Class)} for
		 * further details.
		 *
		 * <p>If {@code type} implements {@link ExtensionContext.Store.CloseableResource}
		 * the {@code close()} method will be invoked on the stored object when
		 * the store is closed.
		 *
		 * @param type the type of object to retrieve; never {@code null}
		 * @param <V> the key and value type
		 * @return the object; never {@code null}
		 * @since 5.1
		 * @see #getOrComputeIfAbsent(Object, Function)
		 * @see #getOrComputeIfAbsent(Object, Function, Class)
		 * @see CloseableResource
		 */
		@API(status = STABLE, since = "5.1")
		default <V> V getOrComputeIfAbsent(Class<V> type) {
			return getOrComputeIfAbsent(type, ReflectionSupport::newInstance, type);
		}

		/**
		 * Get the value that is stored under the supplied {@code key}.
		 *
		 * <p>If no value is stored in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the {@code Namespace} used
		 * to create this store. If no value is found for the supplied {@code key},
		 * a new value will be computed by the {@code defaultCreator} (given
		 * the {@code key} as input), stored, and returned.
		 *
		 * <p>For greater type safety, consider using
		 * {@link #getOrComputeIfAbsent(Object, Function, Class)} instead.
		 *
		 * <p>If the created value is an instance of {@link ExtensionContext.Store.CloseableResource}
		 * the {@code close()} method will be invoked on the stored object when
		 * the store is closed.
		 *
		 * @param key the key; never {@code null}
		 * @param defaultCreator the function called with the supplied {@code key}
		 * to create a new value; never {@code null} but may return {@code null}
		 * @param <K> the key type
		 * @param <V> the value type
		 * @return the value; potentially {@code null}
		 * @see #getOrComputeIfAbsent(Class)
		 * @see #getOrComputeIfAbsent(Object, Function, Class)
		 * @see CloseableResource
		 */
		<K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator);

		/**
		 * Get the value of the specified required type that is stored under the
		 * supplied {@code key}.
		 *
		 * <p>If no value is stored in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the {@code Namespace} used
		 * to create this store. If no value is found for the supplied {@code key},
		 * a new value will be computed by the {@code defaultCreator} (given
		 * the {@code key} as input), stored, and returned.
		 *
		 * <p>If {@code requiredType} implements {@link ExtensionContext.Store.CloseableResource}
		 * the {@code close()} method will be invoked on the stored object when
		 * the store is closed.
		 *
		 * @param key the key; never {@code null}
		 * @param defaultCreator the function called with the supplied {@code key}
		 * to create a new value; never {@code null} but may return {@code null}
		 * @param requiredType the required type of the value; never {@code null}
		 * @param <K> the key type
		 * @param <V> the value type
		 * @return the value; potentially {@code null}
		 * @see #getOrComputeIfAbsent(Class)
		 * @see #getOrComputeIfAbsent(Object, Function)
		 * @see CloseableResource
		 */
		<K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType);

		/**
		 * Store a {@code value} for later retrieval under the supplied {@code key}.
		 *
		 * <p>A stored {@code value} is visible in child {@link ExtensionContext
		 * ExtensionContexts} for the store's {@code Namespace} unless they
		 * overwrite it.
		 *
		 * <p>If the {@code value} is an instance of {@link ExtensionContext.Store.CloseableResource}
		 * the {@code close()} method will be invoked on the stored object when
		 * the store is closed.
		 *
		 * @param key the key under which the value should be stored; never
		 * {@code null}
		 * @param value the value to store; may be {@code null}
		 * @see CloseableResource
		 */
		void put(Object key, Object value);

		/**
		 * Remove the value that was previously stored under the supplied {@code key}.
		 *
		 * <p>The value will only be removed in the current {@link ExtensionContext},
		 * not in ancestors. In addition, the {@link CloseableResource} API will not
		 * be honored for values that are manually removed via this method.
		 *
		 * <p>For greater type safety, consider using {@link #remove(Object, Class)}
		 * instead.
		 *
		 * @param key the key; never {@code null}
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 * @see #remove(Object, Class)
		 */
		Object remove(Object key);

		/**
		 * Remove the value of the specified required type that was previously stored
		 * under the supplied {@code key}.
		 *
		 * <p>The value will only be removed in the current {@link ExtensionContext},
		 * not in ancestors. In addition, the {@link CloseableResource} API will not
		 * be honored for values that are manually removed via this method.
		 *
		 * @param key the key; never {@code null}
		 * @param requiredType the required type of the value; never {@code null}
		 * @param <V> the value type
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 * @see #remove(Object)
		 */
		<V> V remove(Object key, Class<V> requiredType);

	}

	/**
	 * A {@code Namespace} is used to provide a <em>scope</em> for data saved by
	 * extensions within a {@link Store}.
	 *
	 * <p>Storing data in custom namespaces allows extensions to avoid accidentally
	 * mixing data between extensions or across different invocations within the
	 * lifecycle of a single extension.
	 */
	class Namespace {

		/**
		 * The default, global namespace which allows access to stored data from
		 * all extensions.
		 */
		public static final Namespace GLOBAL = Namespace.create(new Object());

		/**
		 * Create a namespace which restricts access to data to all extensions
		 * which use the same sequence of {@code parts} for creating a namespace.
		 *
		 * <p>The order of the {@code parts} is significant.
		 *
		 * <p>Internally the {@code parts} are compared using {@link Object#equals(Object)}.
		 */
		public static Namespace create(Object... parts) {
			Preconditions.notEmpty(parts, "parts array must not be null or empty");
			Preconditions.containsNoNullElements(parts, "individual parts must not be null");
			return new Namespace(new ArrayList<>(Arrays.asList(parts)));
		}

		private final List<Object> parts;

		private Namespace(List<Object> parts) {
			this.parts = parts;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Namespace that = (Namespace) o;
			return this.parts.equals(that.parts);
		}

		@Override
		public int hashCode() {
			return this.parts.hashCode();
		}

		/**
		 * Create a new namespace by appending the supplied {@code parts} to the
		 * existing sequence of parts in this namespace.
		 *
		 * @return new namespace; never {@code null}
		 * @since 5.8
		 */
		@API(status = STABLE, since = "5.10")
		public Namespace append(Object... parts) {
			Preconditions.notEmpty(parts, "parts array must not be null or empty");
			Preconditions.containsNoNullElements(parts, "individual parts must not be null");
			ArrayList<Object> newParts = new ArrayList<>(this.parts.size() + parts.length);
			newParts.addAll(this.parts);
			Collections.addAll(newParts, parts);
			return new Namespace(newParts);
		}
	}

}
