/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.file.PathUtils;

public class Projects {

	public static final String GRAALVM_STARTER = "graalvm-starter";
	public static final String GRADLE_KOTLIN_EXTENSIONS = "gradle-kotlin-extensions";
	public static final String GRADLE_MISSING_ENGINE = "gradle-missing-engine";
	public static final String JAR_DESCRIBE_MODULE = "jar-describe-module";
	public static final String JAVA_VERSIONS = "java-versions";
	public static final String JUPITER_STARTER = "jupiter-starter";
	public static final String MAVEN_SUREFIRE_COMPATIBILITY = "maven-surefire-compatibility";
	public static final String MULTI_RELEASE_JAR = "multi-release-jar";
	public static final String REFLECTION_TESTS = "reflection-tests";
	public static final String STANDALONE = "standalone";
	public static final String VINTAGE = "vintage";

	private Projects() {
	}

	static Path copyToWorkspace(String project, Path workspace) throws IOException {
		PathUtils.copyDirectory(getSourceDirectory(project), workspace);
		return workspace;
	}

	static Path getSourceDirectory(String project) {
		return Path.of("projects").resolve(project);
	}
}
