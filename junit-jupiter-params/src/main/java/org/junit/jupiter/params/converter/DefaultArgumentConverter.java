/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

@API(Internal)
public class DefaultArgumentConverter extends SimpleArgumentConverter {

	public static final DefaultArgumentConverter INSTANCE = new DefaultArgumentConverter();

	private final List<StringConversion> stringConversions = unmodifiableList(
		asList(new PrimitiveStringConversion(), new EnumStringConversion(), new JavaTimeStringConversion()));

	private DefaultArgumentConverter() {
		// nothing to initialize
	}

	protected Object convert(Object input, Class<?> targetClass) {
		if (input == null) {
			if (targetClass.isPrimitive()) {
				throw new ArgumentConversionException(
					"Cannot convert null to primitive value of type " + targetClass.getName());
			}
			return null;
		}
		return convertToReferenceType(input, toWrapperType(targetClass));
	}

	private Class<?> toWrapperType(Class<?> targetClass) {
		Class<?> wrapperType = ReflectionUtils.getWrapperType(targetClass);
		return wrapperType != null ? wrapperType : targetClass;
	}

	private Object convertToReferenceType(Object input, Class<?> targetClass) {
		if (targetClass.isInstance(input)) {
			return input;
		}
		if (input instanceof String) {
			Optional<StringConversion> conversion = stringConversions.stream().filter(
				candidate -> candidate.isResponsible(targetClass)).findFirst();
			if (conversion.isPresent()) {
				try {
					return conversion.get().convert((String) input, targetClass);
				}
				catch (Exception ex) {
					throw new ArgumentConversionException(
						"Error converting String to type " + targetClass.getName() + ": " + input, ex);
				}
			}
		}
		throw new ArgumentConversionException("No implicit conversion to convert object of type "
				+ input.getClass().getName() + " to type " + targetClass.getName());
	}

	interface StringConversion {

		boolean isResponsible(Class<?> targetClass);

		Object convert(String input, Class<?> targetClass) throws Exception;

	}

	static class PrimitiveStringConversion implements StringConversion {

		private static final Map<Class<?>, Function<String, ?>> CONVERTERS;
		static {
			Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
			converters.put(Boolean.class, Boolean::valueOf);
			converters.put(Character.class, input -> {
				Preconditions.condition(input.length() == 1, () -> "String must have length of 1: " + input);
				return input.charAt(0);
			});
			converters.put(Byte.class, Byte::valueOf);
			converters.put(Short.class, Short::valueOf);
			converters.put(Integer.class, Integer::valueOf);
			converters.put(Long.class, Long::valueOf);
			converters.put(Float.class, Float::valueOf);
			converters.put(Double.class, Double::valueOf);
			CONVERTERS = unmodifiableMap(converters);
		}

		@Override
		public boolean isResponsible(Class<?> targetClass) {
			return CONVERTERS.containsKey(targetClass);
		}

		@Override
		public Object convert(String input, Class<?> targetClass) {
			return CONVERTERS.get(targetClass).apply(input);
		}
	}

	static class EnumStringConversion implements StringConversion {

		@Override
		public boolean isResponsible(Class<?> targetClass) {
			return targetClass.isEnum();
		}

		@Override
		public Object convert(String input, Class<?> targetClass) throws Exception {
			return valueOf(targetClass, input);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Object valueOf(Class targetClass, String input) {
			return Enum.valueOf(targetClass, input);
		}
	}

	static class JavaTimeStringConversion implements StringConversion {

		private static final Map<Class<?>, Function<CharSequence, ?>> CONVERTERS;
		static {
			Map<Class<?>, Function<CharSequence, ?>> converters = new LinkedHashMap<>();
			converters.put(Instant.class, Instant::parse);
			converters.put(LocalDate.class, LocalDate::parse);
			converters.put(LocalDateTime.class, LocalDateTime::parse);
			converters.put(LocalTime.class, LocalTime::parse);
			converters.put(OffsetDateTime.class, OffsetDateTime::parse);
			converters.put(OffsetTime.class, OffsetTime::parse);
			converters.put(Year.class, Year::parse);
			converters.put(YearMonth.class, YearMonth::parse);
			converters.put(ZonedDateTime.class, ZonedDateTime::parse);
			CONVERTERS = Collections.unmodifiableMap(converters);
		}

		@Override
		public boolean isResponsible(Class<?> targetClass) {
			return CONVERTERS.containsKey(targetClass);
		}

		@Override
		public Object convert(String input, Class<?> targetClass) throws Exception {
			return CONVERTERS.get(targetClass).apply(input);
		}
	}
}
