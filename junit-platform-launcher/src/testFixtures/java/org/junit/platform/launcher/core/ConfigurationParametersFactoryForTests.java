/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.Map;

import org.junit.platform.engine.ConfigurationParameters;

public class ConfigurationParametersFactoryForTests {

	private ConfigurationParametersFactoryForTests() {
	}

	public static ConfigurationParameters create(Map<String, String> configParams) {
		return LauncherConfigurationParameters.builder() //
				.explicitParameters(configParams) //
				.enableImplicitProviders(false) //
				.build();
	}
}
