/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * Parameters of type {@code TestReporter} can be injected into
 * {@link BeforeEach @BeforeEach} and {@link AfterEach @AfterEach} lifecycle
 * methods as well as methods annotated with {@link Test @Test},
 * {@link RepeatedTest @RepeatedTest},
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest},
 * {@link TestFactory @TestFactory}, etc.
 *
 * <p>Within such methods the injected {@code TestReporter} can be used to
 * publish <em>report entries</em> for the current container or test to the
 * reporting infrastructure.
 *
 * @since 5.0
 * @see #publishEntry(Map)
 * @see #publishEntry(String, String)
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface TestReporter {

	/**
	 * Publish the supplied map of key-value pairs as a <em>report entry</em>.
	 *
	 * @param map the key-value pairs to be published; never {@code null};
	 * keys and values within entries in the map also must not be
	 * {@code null} or blank
	 * @see #publishEntry(String, String)
	 * @see #publishEntry(String)
	 */
	void publishEntry(Map<String, String> map);

	/**
	 * Publish the supplied key-value pair as a <em>report entry</em>.
	 *
	 * @param key the key of the entry to publish; never {@code null} or blank
	 * @param value the value of the entry to publish; never {@code null} or blank
	 * @see #publishEntry(Map)
	 * @see #publishEntry(String)
	 */
	default void publishEntry(String key, String value) {
		this.publishEntry(Collections.singletonMap(key, value));
	}

	/**
	 * Publish the supplied value as a <em>report entry</em>.
	 *
	 * <p>This method delegates to {@link #publishEntry(String, String)},
	 * supplying {@code "value"} as the key and the supplied {@code value}
	 * argument as the value.
	 *
	 * @param value the value to be published; never {@code null} or blank
	 * @since 5.3
	 * @see #publishEntry(Map)
	 * @see #publishEntry(String, String)
	 */
	@API(status = STABLE, since = "5.3")
	default void publishEntry(String value) {
		this.publishEntry("value", value);
	}

	/**
	 * Publish the supplied file and attach it to the current test or container.
	 * <p>
	 * The file will be copied to the report output directory replacing any
	 * potentially existing file with the same name.
	 *
	 * @param file the file to be attached; never {@code null} or blank
	 * @param mediaType the media type of the file; never {@code null}; use
	 * {@link MediaType#APPLICATION_OCTET_STREAM} if unknown
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	default void publishFile(Path file, MediaType mediaType) {
		Preconditions.condition(Files.exists(file), () -> "file must exist: " + file);
		Preconditions.condition(Files.isRegularFile(file), () -> "file must be a regular file: " + file);
		publishFile(file.getFileName().toString(), mediaType, path -> Files.copy(file, path, REPLACE_EXISTING));
	}

	/**
	 * Publish the supplied directory and attach it to the current test or
	 * container.
	 * <p>
	 * The entire directory will be copied to the report output directory
	 * replacing any potentially existing files with the same name.
	 *
	 * @param directory the file to be attached; never {@code null} or blank
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	default void publishDirectory(Path directory) {
		Preconditions.condition(Files.exists(directory), () -> "directory must exist: " + directory);
		Preconditions.condition(Files.isDirectory(directory), () -> "directory must be a directory: " + directory);
		publishDirectory(directory.getFileName().toString(), path -> {
			try (Stream<Path> stream = Files.walk(directory)) {
				stream.forEach(source -> {
					Path destination = path.resolve(directory.relativize(source));
					try {
						if (Files.isDirectory(source)) {
							Files.createDirectories(destination);
						}
						else {
							Files.copy(source, destination, REPLACE_EXISTING);
						}
					}
					catch (IOException e) {
						throw new UncheckedIOException("Failed to copy files to the output directory", e);
					}
				});
			}
		});
	}

	/**
	 * Publish a file or directory with the supplied name and media type written
	 * by the supplied action and attach it to the current test or container.
	 * <p>
	 * The {@link Path} passed to the supplied action will be relative to the
	 * report output directory, but it's up to the action to write the file.
	 *
	 * @param name the name of the file to be attached; never {@code null} or
	 * blank and must not contain any path separators
	 * @param mediaType the media type of the file; never {@code null}; use
	 * {@link MediaType#APPLICATION_OCTET_STREAM} if unknown
	 * @param action the action to be executed to write the file; never {@code null}
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	default void publishFile(String name, MediaType mediaType, ThrowingConsumer<Path> action) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Publish a directory with the supplied name written by the supplied action
	 * and attach it to the current test or container.
	 * <p>
	 * The {@link Path} passed to the supplied action will be relative to the
	 * report output directory and point to an existing directory, but it's up
	 * to the action to write files to it.
	 *
	 * @param name the name of the directory to be attached; never {@code null}
	 * or blank and must not contain any path separators
	 * @param action the action to be executed to write the file; never {@code null}
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	default void publishDirectory(String name, ThrowingConsumer<Path> action) {
		throw new UnsupportedOperationException();
	}

}
