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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.extension.TestInstantiationAwareExtension.ExtensionContextScope.TEST_METHOD;
import static org.junit.jupiter.api.io.CleanupMode.DEFAULT;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.ReflectionSupport.makeAccessible;
import static org.junit.platform.commons.util.ReflectionUtils.isRecordObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.engine.config.EnumConfigurationParameterConverter;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code TempDirectory} is a JUnit Jupiter extension that creates and cleans
 * up temporary directories if a field in a test class or a parameter in a
 * test class constructor, lifecycle method, or test method is annotated with
 * {@code @TempDir}.
 *
 * <p>Consult the Javadoc for {@link TempDir} for details on the contract.
 *
 * @since 5.4
 * @see TempDir @TempDir
 * @see Files#createTempDirectory
 */
class TempDirectory implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	// package-private for testing purposes
	static final Namespace NAMESPACE = Namespace.create(TempDirectory.class);
	static final String FILE_OPERATIONS_KEY = "file.operations";

	private static final String KEY = "temp.dir";
	private static final String FAILURE_TRACKER = "failure.tracker";
	private static final String CHILD_FAILED = "child.failed";

	private final JupiterConfiguration configuration;

	public TempDirectory(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return TEST_METHOD;
	}

	/**
	 * Perform field injection for non-private, {@code static} fields (i.e.,
	 * class fields) of type {@link Path} or {@link File} that are annotated with
	 * {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeAll(ExtensionContext context) {
		installFailureTracker(context);
		injectStaticFields(context, context.getRequiredTestClass());
	}

	/**
	 * Perform field injection for non-private, non-static fields (i.e.,
	 * instance fields) of type {@link Path} or {@link File} that are annotated
	 * with {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeEach(ExtensionContext context) {
		installFailureTracker(context);
		context.getRequiredTestInstances().getAllInstances() //
				.forEach(instance -> injectInstanceFields(context, instance));
	}

	private static void installFailureTracker(ExtensionContext context) {
		context.getStore(NAMESPACE).put(FAILURE_TRACKER, (CloseableResource) () -> context.getParent() //
				.ifPresent(parentContext -> {
					if (selfOrChildFailed(context)) {
						parentContext.getStore(NAMESPACE).put(CHILD_FAILED, true);
					}
				}));
	}

	private void injectStaticFields(ExtensionContext context, Class<?> testClass) {
		injectFields(context, null, testClass, ModifierSupport::isStatic);
	}

	private void injectInstanceFields(ExtensionContext context, Object instance) {
		if (!isRecordObject(instance)) {
			injectFields(context, instance, instance.getClass(), ModifierSupport::isNotStatic);
		}
	}

	private void injectFields(ExtensionContext context, Object testInstance, Class<?> testClass,
			Predicate<Field> predicate) {

		Scope scope = getScope(context);

		findAnnotatedFields(testClass, TempDir.class, predicate).forEach(field -> {
			assertNonFinalField(field);
			assertSupportedType("field", field.getType());

			try {
				CleanupMode cleanupMode = determineCleanupModeForField(field);
				TempDirFactory factory = determineTempDirFactoryForField(field, scope);
				makeAccessible(field).set(testInstance,
					getPathOrFile(field.getType(), new FieldContext(field), factory, cleanupMode, scope, context));
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	/**
	 * Determine if the {@link Parameter} in the supplied {@link ParameterContext}
	 * is annotated with {@link TempDir @TempDir}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(TempDir.class);
	}

	/**
	 * Resolve the current temporary directory for the {@link Parameter} in the
	 * supplied {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);
		CleanupMode cleanupMode = determineCleanupModeForParameter(parameterContext);
		Scope scope = getScope(extensionContext);
		TempDirFactory factory = determineTempDirFactoryForParameter(parameterContext, scope);
		return getPathOrFile(parameterType, parameterContext, factory, cleanupMode, scope, extensionContext);
	}

	private CleanupMode determineCleanupModeForField(Field field) {
		TempDir tempDir = findAnnotation(field, TempDir.class).orElseThrow(
			() -> new JUnitException("Field " + field + " must be annotated with @TempDir"));
		return determineCleanupMode(tempDir);
	}

	private CleanupMode determineCleanupModeForParameter(ParameterContext parameterContext) {
		TempDir tempDir = parameterContext.findAnnotation(TempDir.class).orElseThrow(() -> new JUnitException(
			"Parameter " + parameterContext.getParameter() + " must be annotated with @TempDir"));
		return determineCleanupMode(tempDir);
	}

	private CleanupMode determineCleanupMode(TempDir tempDir) {
		CleanupMode cleanupMode = tempDir.cleanup();
		return cleanupMode == DEFAULT ? this.configuration.getDefaultTempDirCleanupMode() : cleanupMode;
	}

	@SuppressWarnings("deprecation")
	private Scope getScope(ExtensionContext context) {
		return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent( //
			Scope.class, //
			__ -> new EnumConfigurationParameterConverter<>(Scope.class, "@TempDir scope") //
					.get(TempDir.SCOPE_PROPERTY_NAME, context::getConfigurationParameter, Scope.PER_DECLARATION), //
			Scope.class //
		);
	}

	private TempDirFactory determineTempDirFactoryForField(Field field, Scope scope) {
		TempDir tempDir = findAnnotation(field, TempDir.class).orElseThrow(
			() -> new JUnitException("Field " + field + " must be annotated with @TempDir"));
		return determineTempDirFactory(tempDir, scope);
	}

	private TempDirFactory determineTempDirFactoryForParameter(ParameterContext parameterContext, Scope scope) {
		TempDir tempDir = parameterContext.findAnnotation(TempDir.class).orElseThrow(() -> new JUnitException(
			"Parameter " + parameterContext.getParameter() + " must be annotated with @TempDir"));
		return determineTempDirFactory(tempDir, scope);
	}

	@SuppressWarnings("deprecation")
	private TempDirFactory determineTempDirFactory(TempDir tempDir, Scope scope) {
		Class<? extends TempDirFactory> factory = tempDir.factory();

		if (factory != TempDirFactory.class && scope == Scope.PER_CONTEXT) {
			throw new ExtensionConfigurationException("Custom @TempDir factory is not supported with "
					+ TempDir.SCOPE_PROPERTY_NAME + "=" + Scope.PER_CONTEXT.name().toLowerCase() + ". Use "
					+ TempDir.DEFAULT_FACTORY_PROPERTY_NAME + " instead.");
		}

		return factory == TempDirFactory.class //
				? this.configuration.getDefaultTempDirFactorySupplier().get()
				: ReflectionSupport.newInstance(factory);
	}

	private static void assertNonFinalField(Field field) {
		if (ModifierSupport.isFinal(field)) {
			throw new ExtensionConfigurationException("@TempDir field [" + field + "] must not be declared as final.");
		}
	}

	private static void assertSupportedType(String target, Class<?> type) {
		if (type != Path.class && type != File.class) {
			throw new ExtensionConfigurationException("Can only resolve @TempDir " + target + " of type "
					+ Path.class.getName() + " or " + File.class.getName() + " but was: " + type.getName());
		}
	}

	private static Object getPathOrFile(Class<?> elementType, AnnotatedElementContext elementContext,
			TempDirFactory factory, CleanupMode cleanupMode, Scope scope, ExtensionContext extensionContext) {

		Namespace namespace = scope == Scope.PER_DECLARATION //
				? NAMESPACE.append(elementContext) //
				: NAMESPACE;
		Path path = extensionContext.getStore(namespace) //
				.getOrComputeIfAbsent(KEY,
					__ -> createTempDir(factory, cleanupMode, elementType, elementContext, extensionContext),
					CloseablePath.class) //
				.get();

		return (elementType == Path.class) ? path : path.toFile();
	}

	static CloseablePath createTempDir(TempDirFactory factory, CleanupMode cleanupMode, Class<?> elementType,
			AnnotatedElementContext elementContext, ExtensionContext extensionContext) {

		try {
			return new CloseablePath(factory, cleanupMode, elementType, elementContext, extensionContext);
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Failed to create default temp directory", ex);
		}
	}

	private static boolean selfOrChildFailed(ExtensionContext context) {
		return context.getExecutionException().isPresent() //
				|| context.getStore(NAMESPACE).getOrDefault(CHILD_FAILED, Boolean.class, false);
	}

	static class CloseablePath implements CloseableResource {

		private static final Logger LOGGER = LoggerFactory.getLogger(CloseablePath.class);

		private final Path dir;
		private final TempDirFactory factory;
		private final CleanupMode cleanupMode;
		private final AnnotatedElement annotatedElement;
		private final ExtensionContext extensionContext;

		private CloseablePath(TempDirFactory factory, CleanupMode cleanupMode, Class<?> elementType,
				AnnotatedElementContext elementContext, ExtensionContext extensionContext) throws Exception {
			this.dir = factory.createTempDirectory(elementContext, extensionContext);
			this.factory = factory;
			this.cleanupMode = cleanupMode;
			this.annotatedElement = elementContext.getAnnotatedElement();
			this.extensionContext = extensionContext;

			if (this.dir == null || !Files.isDirectory(this.dir)) {
				close();
				throw new PreconditionViolationException("temp directory must be a directory");
			}

			if (elementType == File.class && !this.dir.getFileSystem().equals(FileSystems.getDefault())) {
				close();
				throw new PreconditionViolationException(
					"temp directory with non-default file system cannot be injected into " + File.class.getName()
							+ " target");
			}
		}

		Path get() {
			return this.dir;
		}

		@Override
		public void close() throws IOException {
			try {
				if (this.cleanupMode == NEVER
						|| (this.cleanupMode == ON_SUCCESS && selfOrChildFailed(this.extensionContext))) {
					LOGGER.info(() -> String.format("Skipping cleanup of temp dir %s for %s due to CleanupMode.%s.",
						this.dir, descriptionFor(this.annotatedElement), this.cleanupMode.name()));
					return;
				}

				FileOperations fileOperations = this.extensionContext.getStore(NAMESPACE) //
						.getOrDefault(FILE_OPERATIONS_KEY, FileOperations.class, FileOperations.DEFAULT);
				FileOperations loggingFileOperations = file -> {
					LOGGER.trace(() -> "Attempting to delete " + file);
					try {
						fileOperations.delete(file);
						LOGGER.trace(() -> "Successfully deleted " + file);
					}
					catch (IOException e) {
						LOGGER.trace(e, () -> "Failed to delete " + file);
						throw e;
					}
				};

				LOGGER.trace(() -> "Cleaning up temp dir " + this.dir);
				SortedMap<Path, IOException> failures = deleteAllFilesAndDirectories(loggingFileOperations);
				if (!failures.isEmpty()) {
					throw createIOExceptionWithAttachedFailures(failures);
				}
			}
			finally {
				this.factory.close();
			}
		}

		/**
		 * @since 5.12
		 */
		private static String descriptionFor(AnnotatedElement annotatedElement) {
			if (annotatedElement instanceof Field) {
				Field field = (Field) annotatedElement;
				return "field " + field.getDeclaringClass().getSimpleName() + "." + field.getName();
			}
			if (annotatedElement instanceof Parameter) {
				Parameter parameter = (Parameter) annotatedElement;
				Executable executable = parameter.getDeclaringExecutable();
				return "parameter '" + parameter.getName() + "' in " + descriptionFor(executable);
			}
			throw new IllegalStateException("Unsupported AnnotatedElement type for @TempDir: " + annotatedElement);
		}

		/**
		 * @since 5.12
		 */
		private static String descriptionFor(Executable executable) {
			boolean isConstructor = executable instanceof Constructor<?>;
			String type = isConstructor ? "constructor" : "method";
			String name = isConstructor ? executable.getDeclaringClass().getSimpleName() : executable.getName();
			return String.format("%s %s(%s)", type, name,
				ClassUtils.nullSafeToString(Class::getSimpleName, executable.getParameterTypes()));
		}

		private SortedMap<Path, IOException> deleteAllFilesAndDirectories(FileOperations fileOperations)
				throws IOException {

			Path rootDir = this.dir;
			if (rootDir == null || Files.notExists(rootDir)) {
				return Collections.emptySortedMap();
			}

			SortedMap<Path, IOException> failures = new TreeMap<>();
			Set<Path> retriedPaths = new HashSet<>();
			Path rootRealPath = rootDir.toRealPath();

			tryToResetPermissions(rootDir);
			Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					LOGGER.trace(() -> "preVisitDirectory: " + dir);
					if (isLinkWithTargetOutsideTempDir(dir)) {
						warnAboutLinkWithTargetOutsideTempDir("link", dir);
						delete(dir);
						return SKIP_SUBTREE;
					}
					if (!dir.equals(rootDir)) {
						tryToResetPermissions(dir);
					}
					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					LOGGER.trace(exc, () -> "visitFileFailed: " + file);
					if (exc instanceof NoSuchFileException && !Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
						return CONTINUE;
					}
					// IOException includes `AccessDeniedException` thrown by non-readable or non-executable flags
					resetPermissionsAndTryToDeleteAgain(file, exc);
					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					LOGGER.trace(() -> "visitFile: " + file);
					if (Files.isSymbolicLink(file) && isLinkWithTargetOutsideTempDir(file)) {
						warnAboutLinkWithTargetOutsideTempDir("symbolic link", file);
					}
					delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					LOGGER.trace(exc, () -> "postVisitDirectory: " + dir);
					delete(dir);
					return CONTINUE;
				}

				private boolean isLinkWithTargetOutsideTempDir(Path path) {
					// While `Files.walkFileTree` does not follow symbolic links, it may follow other links
					// such as "junctions" on Windows
					try {
						return !path.toRealPath().startsWith(rootRealPath);
					}
					catch (IOException e) {
						LOGGER.trace(e,
							() -> "Failed to determine real path for " + path + "; assuming it is not a link");
						return false;
					}
				}

				private void warnAboutLinkWithTargetOutsideTempDir(String linkType, Path file) throws IOException {
					Path realPath = file.toRealPath();
					LOGGER.warn(() -> String.format(
						"Deleting %s from location inside of temp dir (%s) "
								+ "to location outside of temp dir (%s) but not the target file/directory",
						linkType, file, realPath));
				}

				private void delete(Path path) {
					try {
						fileOperations.delete(path);
					}
					catch (NoSuchFileException ignore) {
						// ignore
					}
					catch (DirectoryNotEmptyException exception) {
						failures.put(path, exception);
					}
					catch (IOException exception) {
						// IOException includes `AccessDeniedException` thrown by non-readable or non-executable flags
						resetPermissionsAndTryToDeleteAgain(path, exception);
					}
				}

				private void resetPermissionsAndTryToDeleteAgain(Path path, IOException exception) {
					boolean notYetRetried = retriedPaths.add(path);
					if (notYetRetried) {
						try {
							tryToResetPermissions(path);
							if (Files.isDirectory(path)) {
								Files.walkFileTree(path, this);
							}
							else {
								fileOperations.delete(path);
							}
						}
						catch (Exception suppressed) {
							exception.addSuppressed(suppressed);
							failures.put(path, exception);
						}
					}
					else {
						failures.put(path, exception);
					}
				}
			});
			return failures;
		}

		@SuppressWarnings("ResultOfMethodCallIgnored")
		private static void tryToResetPermissions(Path path) {
			File file;
			try {
				file = path.toFile();
			}
			catch (UnsupportedOperationException ignore) {
				// Might happen when the `TempDirFactory` uses a custom `FileSystem`
				return;
			}
			file.setReadable(true);
			file.setWritable(true);
			if (Files.isDirectory(path)) {
				file.setExecutable(true);
			}
			DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
			if (dos != null) {
				try {
					dos.setReadOnly(false);
				}
				catch (IOException ignore) {
					// nothing we can do
				}
			}
		}

		private IOException createIOExceptionWithAttachedFailures(SortedMap<Path, IOException> failures) {
			Path emptyPath = Paths.get("");
			String joinedPaths = failures.keySet().stream() //
					.map(this::tryToDeleteOnExit) //
					.map(this::relativizeSafely) //
					.map(path -> emptyPath.equals(path) ? "<root>" : path.toString()) //
					.collect(joining(", "));
			IOException exception = new IOException("Failed to delete temp directory " + this.dir.toAbsolutePath()
					+ ". The following paths could not be deleted (see suppressed exceptions for details): "
					+ joinedPaths);
			failures.values().forEach(exception::addSuppressed);
			return exception;
		}

		private Path tryToDeleteOnExit(Path path) {
			try {
				path.toFile().deleteOnExit();
			}
			catch (UnsupportedOperationException ignore) {
			}
			return path;
		}

		private Path relativizeSafely(Path path) {
			try {
				return this.dir.relativize(path);
			}
			catch (IllegalArgumentException e) {
				return path;
			}
		}
	}

	enum Scope {

		PER_CONTEXT,

		PER_DECLARATION

	}

	interface FileOperations {

		FileOperations DEFAULT = Files::delete;

		void delete(Path path) throws IOException;

	}

	private static class FieldContext implements AnnotatedElementContext {

		private final Field field;

		private FieldContext(Field field) {
			this.field = Preconditions.notNull(field, "field must not be null");
		}

		@Override
		public AnnotatedElement getAnnotatedElement() {
			return this.field;
		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
					.append("field", this.field)
					.toString();
			// @formatter:on
		}

	}

}
