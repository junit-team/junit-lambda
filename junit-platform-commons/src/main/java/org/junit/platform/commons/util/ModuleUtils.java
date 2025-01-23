/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.DefaultResource;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.commons.support.scanning.ClassFilter;

/**
 * Collection of utilities for working with {@code java.lang.Module}
 * and friends.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public class ModuleUtils {

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	/**
	 * Find all non-system boot modules names.
	 *
	 * @return a set of all such module names; never {@code null} but
	 * potentially empty
	 */
	public static Set<String> findAllNonSystemBootModuleNames() {
		// @formatter:off
		Set<String> systemModules = ModuleFinder.ofSystem().findAll().stream()
				.map(reference -> reference.descriptor().name())
				.collect(toSet());
		return streamResolvedModules(name -> !systemModules.contains(name))
				.map(ResolvedModule::name)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	/**
	 * Java 9+ runtime supports the Java Platform Module System.
	 *
	 * @return {@code true}
	 */
	public static boolean isJavaPlatformModuleSystemAvailable() {
		return true;
	}

	public static Optional<String> getModuleName(Class<?> type) {
		Preconditions.notNull(type, "Class type must not be null");

		return Optional.ofNullable(type.getModule().getName());
	}

	public static Optional<String> getModuleVersion(Class<?> type) {
		Preconditions.notNull(type, "Class type must not be null");

		Module module = type.getModule();
		return module.isNamed() ? module.getDescriptor().rawVersion() : Optional.empty();
	}

	/**
	 * Find all {@linkplain Class classes} for the given module name.
	 *
	 * @param moduleName the name of the module to scan; never {@code null} or
	 * <em>empty</em>
	 * @param filter the class filter to apply; never {@code null}
	 * @return an immutable list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter filter) {
		Preconditions.notBlank(moduleName, "Module name must not be null or empty");
		Preconditions.notNull(filter, "Class filter must not be null");

		logger.debug(() -> "Looking for classes in module: " + moduleName);
		// @formatter:off
		Set<ModuleReference> moduleReferences = streamResolvedModules(isEqual(moduleName))
				.map(ResolvedModule::reference)
				.collect(toSet());
		// @formatter:on
		return scan(moduleReferences, filter, ModuleUtils.class.getClassLoader());
	}

	/**
	 * Find all {@linkplain Resource resources} for the given module name.
	 *
	 * @param moduleName the name of the module to scan; never {@code null} or
	 * <em>empty</em>
	 * @param filter the class filter to apply; never {@code null}
	 * @return an immutable list of all such resources found; never {@code null}
	 * but potentially empty
	 * @since 1.11
	 */
	@API(status = INTERNAL, since = "1.11")
	public static List<Resource> findAllResourcesInModule(String moduleName, Predicate<Resource> filter) {
		Preconditions.notBlank(moduleName, "Module name must not be null or empty");
		Preconditions.notNull(filter, "Resource filter must not be null");

		logger.debug(() -> "Looking for classes in module: " + moduleName);
		// @formatter:off
		Set<ModuleReference> moduleReferences = streamResolvedModules(isEqual(moduleName))
				.map(ResolvedModule::reference)
				.collect(toSet());
		// @formatter:on
		return scan(moduleReferences, filter, ModuleUtils.class.getClassLoader());
	}

	/**
	 * Stream resolved modules from current (or boot) module layer.
	 */
	private static Stream<ResolvedModule> streamResolvedModules(Predicate<String> moduleNamePredicate) {
		Module module = ModuleUtils.class.getModule();
		ModuleLayer layer = module.getLayer();
		if (layer == null) {
			logger.config(() -> ModuleUtils.class + " is a member of " + module
					+ " - using boot layer returned by ModuleLayer.boot() as fall-back.");
			layer = ModuleLayer.boot();
		}
		return streamResolvedModules(moduleNamePredicate, layer);
	}

	/**
	 * Stream resolved modules from the supplied layer.
	 */
	private static Stream<ResolvedModule> streamResolvedModules(Predicate<String> moduleNamePredicate,
			ModuleLayer layer) {
		logger.debug(() -> "Streaming modules for layer @" + System.identityHashCode(layer) + ": " + layer);
		Configuration configuration = layer.configuration();
		logger.debug(() -> "Module layer configuration: " + configuration);
		Stream<ResolvedModule> stream = configuration.modules().stream();
		return stream.filter(module -> moduleNamePredicate.test(module.name()));
	}

	/**
	 * Scan for classes using the supplied set of module references, class
	 * filter, and loader.
	 */
	private static List<Class<?>> scan(Set<ModuleReference> references, ClassFilter filter, ClassLoader loader) {
		logger.debug(() -> "Scanning " + references.size() + " module references: " + references);
		ModuleReferenceClassScanner scanner = new ModuleReferenceClassScanner(filter, loader);
		List<Class<?>> classes = new ArrayList<>();
		for (ModuleReference reference : references) {
			classes.addAll(scanner.scan(reference));
		}
		logger.debug(() -> "Found " + classes.size() + " classes: " + classes);
		return Collections.unmodifiableList(classes);
	}

	/**
	 * Scan for classes using the supplied set of module references, class
	 * filter, and loader.
	 */
	private static List<Resource> scan(Set<ModuleReference> references, Predicate<Resource> filter,
			ClassLoader loader) {
		logger.debug(() -> "Scanning " + references.size() + " module references: " + references);
		ModuleReferenceResourceScanner scanner = new ModuleReferenceResourceScanner(filter, loader);
		List<Resource> classes = new ArrayList<>();
		for (ModuleReference reference : references) {
			classes.addAll(scanner.scan(reference));
		}
		logger.debug(() -> "Found " + classes.size() + " classes: " + classes);
		return Collections.unmodifiableList(classes);
	}

	/**
	 * {@link ModuleReference} class scanner.
	 *
	 * @since 1.1
	 */
	static class ModuleReferenceClassScanner {

		private final ClassFilter classFilter;
		private final ClassLoader classLoader;

		ModuleReferenceClassScanner(ClassFilter classFilter, ClassLoader classLoader) {
			this.classFilter = classFilter;
			this.classLoader = classLoader;
		}

		/**
		 * Scan module reference for classes that potentially contain testable methods.
		 */
		List<Class<?>> scan(ModuleReference reference) {
			try (ModuleReader reader = reference.open()) {
				try (Stream<String> names = reader.list()) {
					// @formatter:off
					return names.filter(name -> name.endsWith(".class"))
							.map(this::className)
							.filter(name -> !name.equals("module-info"))
							.filter(classFilter::match)
							.map(this::loadClassUnchecked)
							.filter(classFilter::match)
							.collect(Collectors.toList());
					// @formatter:on
				}
			}
			catch (IOException e) {
				throw new JUnitException("Failed to read contents of " + reference + ".", e);
			}
		}

		/**
		 * Convert resource name to binary class name.
		 */
		private String className(String resourceName) {
			resourceName = resourceName.substring(0, resourceName.length() - 6); // 6 = ".class".length()
			resourceName = resourceName.replace('/', '.');
			return resourceName;
		}

		/**
		 * Load class by its binary name.
		 *
		 * @see ClassLoader#loadClass(String)
		 */
		private Class<?> loadClassUnchecked(String binaryName) {
			try {
				return classLoader.loadClass(binaryName);
			}
			catch (ClassNotFoundException e) {
				throw new JUnitException("Failed to load class with name '" + binaryName + "'.", e);
			}
		}

	}

	/**
	 * {@link ModuleReference} resource class scanner.
	 *
	 * @since 1.11
	 */
	static class ModuleReferenceResourceScanner {

		private final Predicate<Resource> resourceFilter;
		private final ClassLoader classLoader;

		ModuleReferenceResourceScanner(Predicate<Resource> resourceFilter, ClassLoader classLoader) {
			this.resourceFilter = resourceFilter;
			this.classLoader = classLoader;
		}

		/**
		 * Scan module reference for resources that potentially contain testable resources.
		 */
		List<Resource> scan(ModuleReference reference) {
			try (ModuleReader reader = reference.open()) {
				try (Stream<String> names = reader.list()) {
					// @formatter:off
					return names.filter(name -> !name.endsWith(".class"))
							.map(this::loadResourceUnchecked)
							.filter(resourceFilter)
							.collect(Collectors.toList());
					// @formatter:on
				}
			}
			catch (IOException e) {
				throw new JUnitException("Failed to read contents of " + reference + ".", e);
			}
		}

		private Resource loadResourceUnchecked(String binaryName) {
			try {
				URI uri = classLoader.getResource(binaryName).toURI();
				return new DefaultResource(binaryName, uri);
			}
			catch (URISyntaxException e) {
				throw new JUnitException("Failed to load resource with name '" + binaryName + "'.", e);
			}
		}

	}

}
