/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassLoaderUtils;

/**
 * {@code ConversionSupport} provides static utility methods for converting a
 * given object into an instance of a specified type.
 *
 * @since 1.11
 */
@API(status = EXPERIMENTAL, since = "1.11")
public final class ConversionSupport {

	private ConversionSupport() {
		/* no-op */
	}

	/**
	 * Convert the supplied source {@code String} into an instance of the specified
	 * target type.
	 *
	 * @param source the source {@code String} to convert; may be {@code null}
	 * but only if the target type is a reference type
	 * @param targetType the target type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; may be {@code null} to
	 * use the default {@code ClassLoader}
	 * @param <T> the type of the target
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 *
	 * @since 1.11
	 * @see DefaultConversionService
	 * @deprecated Use {@link #convert(Object, Class, ClassLoader)} instead.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@API(status = DEPRECATED, since = "5.12")
	public static <T> T convert(String source, Class<T> targetType, ClassLoader classLoader) {
		return (T) DefaultConversionService.INSTANCE.convert(source, targetType, getClassLoader(classLoader));
	}

	/**
	 * Convert the supplied source object into an instance of the specified
	 * target type.
	 *
	 * @param source the source object to convert; may be {@code null}
	 * but only if the target type is a reference type
	 * @param targetType the target type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; may be {@code null} to
	 * use the default {@code ClassLoader}
	 * @param <T> the type of the target
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 *
	 * @since 1.12
	 */
	@API(status = EXPERIMENTAL, since = "1.12")
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object source, Class<T> targetType, ClassLoader classLoader) {
		ClassLoader classLoaderToUse = getClassLoader(classLoader);
		ServiceLoader<ConversionService> serviceLoader = ServiceLoader.load(ConversionService.class, classLoaderToUse);

		return (T) Stream.concat( //
			StreamSupport.stream(serviceLoader.spliterator(), false), //
			Stream.of(DefaultConversionService.INSTANCE)) //
				.filter(candidate -> candidate.canConvert(source, targetType, classLoader)) //
				.findFirst() //
				.map(candidate -> candidate.convert(source, targetType, classLoaderToUse)) //
				.orElseThrow(() -> new ConversionException("No registered or built-in converter for source type "
						+ source.getClass().getTypeName() + " and target type " + targetType.getTypeName()));
	}

	private static ClassLoader getClassLoader(ClassLoader classLoader) {
		return classLoader != null ? classLoader : ClassLoaderUtils.getDefaultClassLoader();
	}

}
