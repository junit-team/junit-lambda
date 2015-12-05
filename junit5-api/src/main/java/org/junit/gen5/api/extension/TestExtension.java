/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for all test extensions.
 *
 * <p>{@code TestExtensions} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 * @see MethodParameterResolver
 * @see ContainerLifecycleExtension
 * @see TestLifecycleExtension
 */
public interface TestExtension {

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Inherited
	public @interface Order {
		OrderPosition value();

		boolean unique() default false;
	}

	enum OrderPosition {
		MIDDLE, OUTERMOST, INNERMOST, OUTSIDE, INSIDE
	}

}
