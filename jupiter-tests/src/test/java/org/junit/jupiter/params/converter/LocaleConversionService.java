/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import java.util.Locale;

import org.junit.platform.commons.support.conversion.TypedConversionService;

// FIXME delete
public class LocaleConversionService extends TypedConversionService<String, Locale> {

	public LocaleConversionService() {
		super(String.class, Locale.class);
	}

	@Override
	protected Locale convert(String source) {
		return Locale.forLanguageTag(source);
	}

}
