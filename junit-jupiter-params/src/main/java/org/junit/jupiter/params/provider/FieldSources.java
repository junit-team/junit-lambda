/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @FieldSources} is a simple container for one or more
 * {@link FieldSource} annotations.
 *
 * <p>Note, however, that use of the {@code @FieldSources} container is completely
 * optional since {@code @FieldSource} is a {@linkplain java.lang.annotation.Repeatable
 * repeatable} annotation.
 *
 * @since 5.11
 * @see FieldSource
 * @see java.lang.annotation.Repeatable
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.11")
public @interface FieldSources {

	/**
	 * An array of one or more {@link FieldSource @FieldSource}
	 * annotations.
	 */
	FieldSource[] value();
}
