/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Public API for test engines.
 *
 * <p>Provides the {@link org.junit.platform.engine.TestEngine} interface,
 * test discovery, and execution reporting support.
 *
 * @since 1.0
 */
module org.junit.platform.engine {
	requires static transitive org.apiguardian.api;
	requires transitive org.junit.platform.commons;
	requires transitive org.opentest4j;

	exports org.junit.platform.engine;
	exports org.junit.platform.engine.discovery;
	exports org.junit.platform.engine.reporting;
	// exports org.junit.platform.engine.support; empty package
	exports org.junit.platform.engine.support.config;
	exports org.junit.platform.engine.support.descriptor;
	exports org.junit.platform.engine.support.discovery;
	exports org.junit.platform.engine.support.filter;
	exports org.junit.platform.engine.support.hierarchical;
	exports org.junit.platform.engine.support.store;

	uses org.junit.platform.engine.discovery.DiscoverySelectorIdentifierParser;

	provides org.junit.platform.engine.discovery.DiscoverySelectorIdentifierParser with
			org.junit.platform.engine.discovery.ClassSelector.IdentifierParser,
			org.junit.platform.engine.discovery.ClasspathResourceSelector.IdentifierParser,
			org.junit.platform.engine.discovery.ClasspathRootSelector.IdentifierParser,
			org.junit.platform.engine.discovery.DirectorySelector.IdentifierParser,
			org.junit.platform.engine.discovery.FileSelector.IdentifierParser,
			org.junit.platform.engine.discovery.IterationSelector.IdentifierParser,
			org.junit.platform.engine.discovery.MethodSelector.IdentifierParser,
			org.junit.platform.engine.discovery.ModuleSelector.IdentifierParser,
			org.junit.platform.engine.discovery.NestedClassSelector.IdentifierParser,
			org.junit.platform.engine.discovery.NestedMethodSelector.IdentifierParser,
			org.junit.platform.engine.discovery.PackageSelector.IdentifierParser,
			org.junit.platform.engine.discovery.UniqueIdSelector.IdentifierParser,
			org.junit.platform.engine.discovery.UriSelector.IdentifierParser;

}
