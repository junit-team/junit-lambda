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

import static de.skuzzle.test.snapshots.data.xml.XmlSnapshot.xml;
import static org.junit.platform.reporting.testutil.FileUtils.findPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import de.skuzzle.test.snapshots.Snapshot;
import de.skuzzle.test.snapshots.SnapshotSerializer;
import de.skuzzle.test.snapshots.StructuredData;
import de.skuzzle.test.snapshots.StructuredDataProvider;

class XmlAssertions {

	static void verifyContainsExpectedStartedOpenTestReport(Path testResultsDir, Snapshot snapshot) throws IOException {
		var xmlFile = findPath(testResultsDir, "glob:**/open-test-report.xml");
		verifyContent(xmlFile, snapshot);
	}

	private static void verifyContent(Path xmlFile, Snapshot snapshot) throws IOException {
		snapshot.named("open-test-report.xml") //
				.assertThat(Files.readString(xmlFile)) //
				.as(obfuscated( //
					xml() //
							.withXPathNamespaceContext(Map.of( //
								"c", "https://schemas.opentest4j.org/reporting/core/0.2.0", //
								"e", "https://schemas.opentest4j.org/reporting/events/0.2.0", //
								"java", "https://schemas.opentest4j.org/reporting/java/0.2.0" //
							)) //
							.withComparisonRules(rules -> rules //
									.pathAt("//@time").mustMatch(
										Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z")) //
									.pathAt("//c:infrastructure/c:hostName/text()").ignore() //
									.pathAt("//c:infrastructure/c:userName/text()").ignore() //
									.pathAt("//c:infrastructure/c:operatingSystem/text()").ignore() //
									.pathAt("//c:infrastructure/c:cpuCores/text()").ignore() //
									.pathAt("//c:infrastructure/java:javaVersion/text()").ignore() //
									.pathAt("//c:infrastructure/java:fileEncoding/text()").ignore() //
									.pathAt("//c:infrastructure/java:heapSize/@max").ignore() //
							), //
					text -> text //
							.replaceAll("<hostName>.+?</hostName>", "<hostName>obfuscated</hostName>") //
							.replaceAll("<userName>.+?</userName>", "<userName>obfuscated</userName>") //
				)) //
				.matchesSnapshotStructure();
	}

	private static StructuredDataProvider obfuscated(StructuredDataProvider provider,
			UnaryOperator<String> obfuscator) {
		return () -> {
			var structuredData = provider.build();
			var snapshotSerializer = obfuscatingSnapshotSerializer(structuredData.snapshotSerializer(), obfuscator);
			return StructuredData.with(snapshotSerializer, structuredData.structuralAssertions());
		};
	}

	private static SnapshotSerializer obfuscatingSnapshotSerializer(SnapshotSerializer delegate,
			UnaryOperator<String> obfuscator) {
		return testResult -> {
			Object obfuscatedTestResult = testResult;
			if (testResult instanceof String) {
				obfuscatedTestResult = obfuscator.apply((String) testResult);
			}
			return delegate.serialize(obfuscatedTestResult);
		};
	}
}
