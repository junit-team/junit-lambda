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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code TypedConversionService} is an abstract base class for
 * {@link ConversionService} implementations that always convert objects of a
 * given source type into a given target type.
 *
 * @param <S> the type of the source argument to convert
 * @param <T> the type of the target object to create from the source
 * @since 1.12
 */
@API(status = EXPERIMENTAL, since = "1.12")
public abstract class TypedConversionService<S, T> implements ConversionService {

	private final Class<S> sourceType;
	private final Class<T> targetType;

	/**
	 * Create a new {@code TypedConversionService}.
	 *
	 * @param sourceType the type of the argument to convert; never {@code null}
	 * @param targetType the type of the target object to create from the source;
	 * never {@code null}
	 */
	protected TypedConversionService(Class<S> sourceType, Class<T> targetType) {
		this.sourceType = Preconditions.notNull(sourceType, "sourceType must not be null");
		this.targetType = Preconditions.notNull(targetType, "targetType must not be null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canConvert(Object source, Class<?> targetType, ClassLoader classLoader) {
		return sourceType.isInstance(source) && ReflectionUtils.isAssignableTo(this.targetType, targetType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object convert(Object source, Class<?> targetType, ClassLoader classLoader) {
		return source == null ? convert(null) : convert(this.sourceType.cast(source));
	}

	/**
	 * Convert the supplied {@code source} object of type {@code S} into an object
	 * of type {@code T}.
	 *
	 * @param source the source object to convert; may be {@code null}
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ConversionException if an error occurs during the conversion
	 */
	protected abstract T convert(S source) throws ConversionException;

}
