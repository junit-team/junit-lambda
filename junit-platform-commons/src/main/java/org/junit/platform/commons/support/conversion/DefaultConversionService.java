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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.junit.platform.commons.util.ReflectionUtils.getWrapperType;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.platform.commons.util.ClassLoaderUtils;

/**
 * {@code DefaultConversionService} is the default implementation of the
 * {@link ConversionService} API.
 *
 * <p>The {@code DefaultConversionService} is able to convert from strings to a
 * number of primitive types and their corresponding wrapper types (Byte, Short,
 * Integer, Long, Float, and Double), date and time types from the
 * {@code java.time} package, and some additional common Java types such as
 * {@link File}, {@link BigDecimal}, {@link BigInteger}, {@link Currency},
 * {@link Locale}, {@link URI}, {@link URL}, {@link UUID}, etc.
 *
 * <p>If the source and target types are identical, the source object will not
 * be modified.
 *
 * @since 1.12
 */
class DefaultConversionService implements ConversionService {

	static final DefaultConversionService INSTANCE = new DefaultConversionService();

	private static final List<StringToObjectConverter> stringToObjectConverters = unmodifiableList(asList( //
		new StringToBooleanConverter(), //
		new StringToCharacterConverter(), //
		new StringToNumberConverter(), //
		new StringToClassConverter(), //
		new StringToEnumConverter(), //
		new StringToJavaTimeConverter(), //
		new StringToCommonJavaTypesConverter(), //
		new FallbackStringToObjectConverter() //
	));

	private DefaultConversionService() {
		// nothing to initialize
	}

	/**
	 * Determine if the supplied source object can be converted into an instance
	 * of the specified target type.
	 *
	 * @param source the source object to convert; may be {@code null} but only
	 * if the target type is a reference type
	 * @param targetType the target type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @return {@code true} if the supplied source can be converted
	 */
	@Override
	public boolean canConvert(Object source, Class<?> targetType, ClassLoader classLoader) {
		return source instanceof String;
	}

	/**
	 * Convert the supplied source object into an instance of the specified
	 * target type.
	 *
	 * <p>If the target type is {@code String}, the source {@code String} will not
	 * be modified.
	 *
	 * <p>Some forms of conversion require a {@link ClassLoader}. If none is
	 * provided, the {@linkplain ClassLoaderUtils#getDefaultClassLoader() default
	 * ClassLoader} will be used.
	 *
	 * <p>This method is able to convert strings into primitive types and their
	 * corresponding wrapper types ({@link Boolean}, {@link Character}, {@link Byte},
	 * {@link Short}, {@link Integer}, {@link Long}, {@link Float}, and
	 * {@link Double}), enum constants, date and time types from the
	 * {@code java.time} package, as well as common Java types such as {@link Class},
	 * {@link java.io.File}, {@link java.nio.file.Path}, {@link java.nio.charset.Charset},
	 * {@link java.math.BigDecimal}, {@link java.math.BigInteger},
	 * {@link java.util.Currency}, {@link java.util.Locale}, {@link java.util.UUID},
	 * {@link java.net.URI}, and {@link java.net.URL}.
	 *
	 * <p>If the target type is not covered by any of the above, a convention-based
	 * conversion strategy will be used to convert the source {@code String} into the
	 * given target type by invoking a static factory method or factory constructor
	 * defined in the target type. The search algorithm used in this strategy is
	 * outlined below.
	 *
	 * <h4>Search Algorithm</h4>
	 *
	 * <ol>
	 * <li>Search for a single, non-private static factory method in the target
	 * type that converts from a String to the target type. Use the factory method
	 * if present.</li>
	 * <li>Search for a single, non-private constructor in the target type that
	 * accepts a String. Use the constructor if present.</li>
	 * </ol>
	 *
	 * <p>If multiple suitable factory methods are discovered, they will be ignored.
	 * If neither a single factory method nor a single constructor is found, the
	 * convention-based conversion strategy will not apply.
	 *
	 * @param source the source object to convert; may be {@code null} but only
	 * if the target type is a reference type
	 * @param targetType the target type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ConversionException if an error occurs during the conversion
	 */
	@Override
	public Object convert(Object source, Class<?> targetType, ClassLoader classLoader) {
		if (source == null) {
			if (targetType.isPrimitive()) {
				throw new ConversionException(
					"Cannot convert null to primitive value of type " + targetType.getTypeName());
			}
			return null;
		}

		if (String.class.equals(targetType)) {
			return source;
		}

		// FIXME move/copy next three lines to canConvert?
		Class<?> targetTypeToUse = toWrapperType(targetType);
		Optional<StringToObjectConverter> converter = stringToObjectConverters.stream().filter(
			candidate -> candidate.canConvertTo(targetTypeToUse)).findFirst();
		if (converter.isPresent()) {
			try {
				return converter.get().convert((String) source, targetTypeToUse, classLoader);
			}
			catch (Exception ex) {
				if (ex instanceof ConversionException) {
					// simply rethrow it
					throw (ConversionException) ex;
				}
				// else
				throw new ConversionException(
					String.format("Failed to convert String \"%s\" to type %s", source, targetType.getTypeName()), ex);
			}
		}

		throw new ConversionException(
			"No built-in converter for source type java.lang.String and target type " + targetType.getTypeName());
	}

	private static Class<?> toWrapperType(Class<?> targetType) {
		Class<?> wrapperType = getWrapperType(targetType);
		return wrapperType != null ? wrapperType : targetType;
	}

}
