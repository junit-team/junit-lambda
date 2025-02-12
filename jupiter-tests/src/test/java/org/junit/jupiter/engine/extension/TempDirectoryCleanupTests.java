/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Test that {@linkplain TempDir temporary directories} are not deleted with
 * {@link CleanupMode#NEVER}, are deleted with {@link CleanupMode#ON_SUCCESS}
 * but only if the test passes, and are always deleted with {@link CleanupMode#ALWAYS}.
 *
 * @see CleanupMode
 * @see TempDir
 * @since 5.9
 */
class TempDirectoryCleanupTests extends AbstractJupiterTestEngineTests {

	@Nested
	class TempDirFieldTests {

		private static Path defaultFieldDir;
		private static Path neverFieldDir;
		private static Path alwaysFieldDir;
		private static Path onSuccessFailingFieldDir;
		private static Path onSuccessPassingFieldDir;

		/**
		 * Ensure the cleanup mode defaults to ALWAYS for fields.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeDefaultField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(DefaultFieldCase.class, "testDefaultField"))//
					.build();
			executeTests(request);

			assertThat(defaultFieldDir).doesNotExist();
		}

		/**
		 * Ensure that a custom, global cleanup mode is used for fields.
		 * <p/>
		 * Expect the TempDir NOT to be cleaned up if set to NEVER.
		 */
		@Test
		void cleanupModeCustomDefaultField() {
			LauncherDiscoveryRequest request = request()//
					.configurationParameter(TempDir.DEFAULT_CLEANUP_MODE_PROPERTY_NAME, "never")//
					.selectors(selectMethod(DefaultFieldCase.class, "testDefaultField"))//
					.build();
			executeTests(request);

			assertThat(defaultFieldDir).exists();
		}

		/**
		 * Ensure that NEVER cleanup modes are obeyed for fields.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeNeverField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(NeverFieldCase.class, "testNeverField"))//
					.build();
			executeTests(request);

			assertThat(neverFieldDir).exists();
		}

		/**
		 * Ensure that ALWAYS cleanup modes are obeyed for fields.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeAlwaysField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(AlwaysFieldCase.class, "testAlwaysField"))//
					.build();
			executeTests(request);

			assertThat(alwaysFieldDir).doesNotExist();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing field tests.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessPassingField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(OnSuccessPassingFieldCase.class, "testOnSuccessPassingField"))//
					.build();
			executeTests(request);

			assertThat(onSuccessPassingFieldDir).doesNotExist();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing field tests.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessFailingField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(OnSuccessFailingFieldCase.class, "testOnSuccessFailingField"))//
					.build();
			executeTests(request);

			assertThat(onSuccessFailingFieldDir).exists();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for static fields when tests are failing.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessFailingStaticField() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectClass(OnSuccessFailingStaticFieldCase.class))//
					.build();
			executeTests(request);

			assertThat(onSuccessFailingFieldDir).exists();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for static fields when nested tests are failing.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessFailingStaticFieldWithNesting() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectClass(OnSuccessFailingStaticFieldWithNestingCase.class))//
					.build();
			executeTests(request);

			assertThat(onSuccessFailingFieldDir).exists();
		}

		@AfterAll
		static void afterAll() throws IOException {
			deleteIfNotNullAndExists(defaultFieldDir);
			deleteIfNotNullAndExists(neverFieldDir);
			deleteIfNotNullAndExists(alwaysFieldDir);
			deleteIfNotNullAndExists(onSuccessFailingFieldDir);
			deleteIfNotNullAndExists(onSuccessPassingFieldDir);
		}

		static void deleteIfNotNullAndExists(Path dir) throws IOException {
			if (dir != null) {
				deleteIfExists(dir);
			}
		}

		// -------------------------------------------------------------------

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class DefaultFieldCase {

			@TempDir
			Path defaultFieldDir;

			@Test
			void testDefaultField() {
				TempDirFieldTests.defaultFieldDir = defaultFieldDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class NeverFieldCase {

			@TempDir(cleanup = NEVER)
			Path neverFieldDir;

			@Test
			void testNeverField() {
				TempDirFieldTests.neverFieldDir = neverFieldDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class AlwaysFieldCase {

			@TempDir(cleanup = ALWAYS)
			Path alwaysFieldDir;

			@Test
			void testAlwaysField() {
				TempDirFieldTests.alwaysFieldDir = alwaysFieldDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class OnSuccessPassingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			Path onSuccessPassingFieldDir;

			@Test
			void testOnSuccessPassingField() {
				TempDirFieldTests.onSuccessPassingFieldDir = onSuccessPassingFieldDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class OnSuccessFailingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			Path onSuccessFailingFieldDir;

			@Test
			void testOnSuccessFailingField() {
				TempDirFieldTests.onSuccessFailingFieldDir = onSuccessFailingFieldDir;
				fail();
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		static class OnSuccessFailingStaticFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			static Path onSuccessFailingFieldDir;

			@Test
			@Order(1)
			void failing() {
				TempDirFieldTests.onSuccessFailingFieldDir = onSuccessFailingFieldDir;
				fail();
			}

			@Test
			@Order(2)
			void passing() {
			}
		}

		static class OnSuccessFailingStaticFieldWithNestingCase {

			@TempDir(cleanup = ON_SUCCESS)
			static Path onSuccessFailingFieldDir;

			@Nested
			class NestedTestCase {

				@Test
				void test() {
					TempDirFieldTests.onSuccessFailingFieldDir = onSuccessFailingFieldDir;
					fail();
				}
			}
		}

	}

	@Nested
	class TempDirParameterTests {

		private static Path defaultParameterDir;
		private static Path neverParameterDir;
		private static Path alwaysParameterDir;
		private static Path onSuccessFailingParameterDir;
		private static Path onSuccessPassingParameterDir;

		/**
		 * Ensure the cleanup mode defaults to ALWAYS for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeDefaultParameter() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(DefaultParameterCase.class, "testDefaultParameter", "java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(defaultParameterDir).doesNotExist();
		}

		/**
		 * Ensure that a custom, global cleanup mode is used for parameters.
		 * <p/>
		 * Expect the TempDir NOT to be cleaned up if set to NEVER.
		 */
		@Test
		void cleanupModeCustomDefaultParameter() {
			LauncherDiscoveryRequest request = request()//
					.configurationParameter(TempDir.DEFAULT_CLEANUP_MODE_PROPERTY_NAME, "never")//
					.selectors(selectMethod(DefaultParameterCase.class, "testDefaultParameter", "java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(defaultParameterDir).exists();
		}

		/**
		 * Ensure that NEVER cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeNeverParameter() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(NeverParameterCase.class, "testNeverParameter", "java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(neverParameterDir).exists();
		}

		/**
		 * Ensure that ALWAYS cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeAlwaysParameter() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(AlwaysParameterCase.class, "testAlwaysParameter", "java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(alwaysParameterDir).doesNotExist();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing parameter tests.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessPassingParameter() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(OnSuccessPassingParameterCase.class, "testOnSuccessPassingParameter",
						"java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(onSuccessPassingParameterDir).doesNotExist();
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing parameter tests.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void cleanupModeOnSuccessFailingParameter() {
			LauncherDiscoveryRequest request = request()//
					.selectors(selectMethod(OnSuccessFailingParameterCase.class, "testOnSuccessFailingParameter",
						"java.nio.file.Path"))//
					.build();
			executeTests(request);

			assertThat(onSuccessFailingParameterDir).exists();
		}

		@AfterAll
		static void afterAll() throws IOException {
			TempDirFieldTests.deleteIfNotNullAndExists(defaultParameterDir);
			TempDirFieldTests.deleteIfNotNullAndExists(neverParameterDir);
			TempDirFieldTests.deleteIfNotNullAndExists(alwaysParameterDir);
			TempDirFieldTests.deleteIfNotNullAndExists(onSuccessFailingParameterDir);
			TempDirFieldTests.deleteIfNotNullAndExists(onSuccessPassingParameterDir);
		}

		// -------------------------------------------------------------------

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class DefaultParameterCase {

			@Test
			void testDefaultParameter(@TempDir Path defaultParameterDir) {
				TempDirParameterTests.defaultParameterDir = defaultParameterDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class NeverParameterCase {

			@Test
			void testNeverParameter(@TempDir(cleanup = NEVER) Path neverParameterDir) {
				TempDirParameterTests.neverParameterDir = neverParameterDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class AlwaysParameterCase {

			@Test
			void testAlwaysParameter(@TempDir(cleanup = ALWAYS) Path alwaysParameterDir) {
				TempDirParameterTests.alwaysParameterDir = alwaysParameterDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class OnSuccessPassingParameterCase {

			@Test
			void testOnSuccessPassingParameter(@TempDir(cleanup = ON_SUCCESS) Path onSuccessPassingParameterDir) {
				TempDirParameterTests.onSuccessPassingParameterDir = onSuccessPassingParameterDir;
			}
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class OnSuccessFailingParameterCase {

			@Test
			void testOnSuccessFailingParameter(@TempDir(cleanup = ON_SUCCESS) Path onSuccessFailingParameterDir) {
				TempDirParameterTests.onSuccessFailingParameterDir = onSuccessFailingParameterDir;
				fail();
			}
		}

	}

	@Nested
	@EnabledOnOs(WINDOWS)
	class WindowsTests {

		@Test
		void deletesBrokenJunctions(@TempDir Path dir) throws Exception {
			var test = Files.createDirectory(dir.resolve("test"));
			createWindowsJunction(dir.resolve("link"), test);
			// The error might also occur without the source folder being deleted
			// but it depends on the order that the TempDir cleanup does its work,
			// so the following line forces the error to occur always
			Files.delete(test);
		}

		@Test
		void doesNotFollowJunctions(@TempDir Path tempDir, @TrackLogRecords LogRecordListener listener)
				throws IOException {
			var outsideDir = Files.createDirectory(tempDir.resolve("outside"));
			var testFile = Files.writeString(outsideDir.resolve("test.txt"), "test");

			JunctionTestCase.target = outsideDir;
			try {
				executeTestsForClass(JunctionTestCase.class).testEvents() //
						.assertStatistics(stats -> stats.started(1).succeeded(1));
			}
			finally {
				JunctionTestCase.target = null;
			}

			assertThat(outsideDir).exists();
			assertThat(testFile).exists();
			assertThat(listener.stream(Level.WARNING)) //
					.map(LogRecord::getMessage) //
					.anyMatch(m -> m.matches(
						"Deleting link from location inside of temp dir \\(.+\\) to location outside of temp dir \\(.+\\) but not the target file/directory"));
		}

		@SuppressWarnings("JUnitMalformedDeclaration")
		static class JunctionTestCase {
			public static Path target;

			@Test
			void createJunctionToTarget(@TempDir Path dir) throws Exception {
				var link = createWindowsJunction(dir.resolve("link"), target);
				try (var files = Files.list(link)) {
					files.forEach(it -> System.out.println("- " + it));
				}
			}
		}

		private static Path createWindowsJunction(Path link, Path target) throws Exception {
			// This creates a Windows "junction" which you can't do with Java code
			String[] command = { "cmd.exe", "/c", "mklink", "/j", link.toString(), target.toString() };
			Runtime.getRuntime().exec(command).waitFor();
			return link;
		}
	}

}
