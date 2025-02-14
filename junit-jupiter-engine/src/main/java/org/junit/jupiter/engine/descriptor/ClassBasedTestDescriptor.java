/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.registerExtensionsFromConstructorParameters;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.registerExtensionsFromExecutableParameters;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.registerExtensionsFromInstanceFields;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.registerExtensionsFromStaticFields;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterEachMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeEachMethods;
import static org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils.getTestInstanceLifecycle;
import static org.junit.jupiter.engine.support.JupiterThrowableCollectorFactory.createThrowableCollector;
import static org.junit.platform.commons.util.CollectionUtils.forEachInReverseOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.DefaultTestInstances;
import org.junit.jupiter.engine.execution.ExtensionContextSupplier;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.ReflectiveInterceptorCall;
import org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.ReflectiveInterceptorCall.VoidMethodInterceptorCall;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.TestInstancesProvider;
import org.junit.jupiter.engine.extension.ExtensionRegistrar;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * @since 5.5
 */
@API(status = INTERNAL, since = "5.5")
public abstract class ClassBasedTestDescriptor extends JupiterTestDescriptor
		implements ResourceLockAware, TestClassAware {

	private static final InterceptingExecutableInvoker executableInvoker = new InterceptingExecutableInvoker();

	private final Class<?> testClass;
	protected final Set<TestTag> tags;
	protected final Lifecycle lifecycle;
	private final ExclusiveResourceCollector exclusiveResourceCollector;

	private ExecutionMode defaultChildExecutionMode;
	private TestInstanceFactory testInstanceFactory;
	private List<Method> beforeAllMethods;
	private List<Method> afterAllMethods;

	ClassBasedTestDescriptor(UniqueId uniqueId, Class<?> testClass, Supplier<String> displayNameSupplier,
			JupiterConfiguration configuration) {
		super(uniqueId, testClass, displayNameSupplier, ClassSource.from(testClass), configuration);

		this.testClass = testClass;
		this.tags = getTags(testClass);
		this.lifecycle = getTestInstanceLifecycle(testClass, configuration);
		this.defaultChildExecutionMode = (this.lifecycle == Lifecycle.PER_CLASS ? ExecutionMode.SAME_THREAD : null);
		this.exclusiveResourceCollector = ExclusiveResourceCollector.from(testClass);
	}

	ClassBasedTestDescriptor(UniqueId uniqueId, Class<?> testClass, String displayName,
			JupiterConfiguration configuration) {
		super(uniqueId, displayName, ClassSource.from(testClass), configuration);

		this.testClass = testClass;
		this.tags = getTags(testClass);
		this.lifecycle = getTestInstanceLifecycle(testClass, configuration);
		this.defaultChildExecutionMode = (this.lifecycle == Lifecycle.PER_CLASS ? ExecutionMode.SAME_THREAD : null);
		this.exclusiveResourceCollector = ExclusiveResourceCollector.from(testClass);
	}

	// --- TestClassAware ------------------------------------------------------

	@Override
	public final Class<?> getTestClass() {
		return this.testClass;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public final String getLegacyReportingName() {
		return this.testClass.getName();
	}

	// --- Node ----------------------------------------------------------------

	@Override
	protected final Optional<ExecutionMode> getExplicitExecutionMode() {
		return getExecutionModeFromAnnotation(getTestClass());
	}

	@Override
	protected final Optional<ExecutionMode> getDefaultChildExecutionMode() {
		return Optional.ofNullable(this.defaultChildExecutionMode);
	}

	public final void setDefaultChildExecutionMode(ExecutionMode defaultChildExecutionMode) {
		this.defaultChildExecutionMode = defaultChildExecutionMode;
	}

	@Override
	public final ExclusiveResourceCollector getExclusiveResourceCollector() {
		return exclusiveResourceCollector;
	}

	@Override
	public final JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		MutableExtensionRegistry registry = populateNewExtensionRegistryFromExtendWithAnnotation(
			context.getExtensionRegistry(), this.testClass);

		// Register extensions from static fields here, at the class level but
		// after extensions registered via @ExtendWith.
		registerExtensionsFromStaticFields(registry, this.testClass);

		// Resolve the TestInstanceFactory at the class level in order to fail
		// the entire class in case of configuration errors (e.g., more than
		// one factory registered per class).
		this.testInstanceFactory = resolveTestInstanceFactory(registry);

		if (this.testInstanceFactory == null) {
			registerExtensionsFromConstructorParameters(registry, this.testClass);
		}

		this.beforeAllMethods = findBeforeAllMethods(this.testClass, this.lifecycle == Lifecycle.PER_METHOD);
		this.afterAllMethods = findAfterAllMethods(this.testClass, this.lifecycle == Lifecycle.PER_METHOD);

		this.beforeAllMethods.forEach(method -> registerExtensionsFromExecutableParameters(registry, method));
		// Since registerBeforeEachMethodAdapters() and registerAfterEachMethodAdapters() also
		// invoke registerExtensionsFromExecutableParameters(), we invoke those methods before
		// invoking registerExtensionsFromExecutableParameters() for @AfterAll methods,
		// thereby ensuring proper registration order for extensions registered via @ExtendWith
		// on parameters in lifecycle methods.
		registerBeforeEachMethodAdapters(registry);
		registerAfterEachMethodAdapters(registry);
		this.afterAllMethods.forEach(method -> registerExtensionsFromExecutableParameters(registry, method));
		registerExtensionsFromInstanceFields(registry, this.testClass);

		ThrowableCollector throwableCollector = createThrowableCollector();
		ClassExtensionContext extensionContext = new ClassExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, this.lifecycle, context.getConfiguration(), registry,
			throwableCollector);

		// @formatter:off
		return context.extend()
				.withTestInstancesProvider(testInstancesProvider(context, extensionContext))
				.withExtensionRegistry(registry)
				.withExtensionContext(extensionContext)
				.withThrowableCollector(throwableCollector)
				.build();
		// @formatter:on
	}

	@Override
	public final JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		if (isPerClassLifecycle(context)) {
			// Eagerly load test instance for BeforeAllCallbacks, if necessary,
			// and store the instance in the ExtensionContext.
			ClassExtensionContext extensionContext = (ClassExtensionContext) context.getExtensionContext();
			throwableCollector.execute(() -> {
				TestInstances testInstances = context.getTestInstancesProvider().getTestInstances(context);
				extensionContext.setTestInstances(testInstances);
			});
		}

		if (throwableCollector.isEmpty()) {
			context.beforeAllCallbacksExecuted(true);
			invokeBeforeAllCallbacks(context);

			if (throwableCollector.isEmpty()) {
				context.beforeAllMethodsExecuted(true);
				invokeBeforeAllMethods(context);
			}
		}

		throwableCollector.assertEmpty();

		return context;
	}

	@Override
	public final void after(JupiterEngineExecutionContext context) {

		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Throwable previousThrowable = throwableCollector.getThrowable();

		if (context.beforeAllMethodsExecuted()) {
			invokeAfterAllMethods(context);
		}

		if (context.beforeAllCallbacksExecuted()) {
			invokeAfterAllCallbacks(context);
		}

		if (isPerClassLifecycle(context) && context.getExtensionContext().getTestInstance().isPresent()) {
			invokeTestInstancePreDestroyCallbacks(context);
		}

		// If the previous Throwable was not null when this method was called,
		// that means an exception was already thrown either before or during
		// the execution of this Node. If an exception was already thrown, any
		// later exceptions were added as suppressed exceptions to that original
		// exception unless a more severe exception occurred in the meantime.
		if (previousThrowable != throwableCollector.getThrowable()) {
			throwableCollector.assertEmpty();
		}
	}

	private TestInstanceFactory resolveTestInstanceFactory(ExtensionRegistry registry) {
		List<TestInstanceFactory> factories = registry.getExtensions(TestInstanceFactory.class);

		if (factories.size() == 1) {
			return factories.get(0);
		}

		if (factories.size() > 1) {
			String factoryNames = factories.stream()//
					.map(factory -> factory.getClass().getName())//
					.collect(joining(", "));

			String errorMessage = String.format(
				"The following TestInstanceFactory extensions were registered for test class [%s], but only one is permitted: %s",
				testClass.getName(), factoryNames);

			throw new ExtensionConfigurationException(errorMessage);
		}

		return null;
	}

	private TestInstancesProvider testInstancesProvider(JupiterEngineExecutionContext parentExecutionContext,
			ClassExtensionContext ourExtensionContext) {

		// For Lifecycle.PER_CLASS, ourExtensionContext.getTestInstances() is used to store the instance.
		// Otherwise, extensionContext.getTestInstances() is always empty and we always create a new instance.
		return (registry, context) -> ourExtensionContext.getTestInstances().orElseGet(
			() -> instantiateAndPostProcessTestInstance(parentExecutionContext, ourExtensionContext, registry,
				context));
	}

	private TestInstances instantiateAndPostProcessTestInstance(JupiterEngineExecutionContext parentExecutionContext,
			ClassExtensionContext ourExtensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {

		ExtensionContextSupplier extensionContext = ExtensionContextSupplier.create(context.getExtensionContext(),
			ourExtensionContext, configuration);
		TestInstances instances = instantiateTestClass(parentExecutionContext, extensionContext, registry, context);
		context.getThrowableCollector().execute(() -> {
			invokeTestInstancePostProcessors(instances.getInnermostInstance(), registry, extensionContext);
			// In addition, we initialize extension registered programmatically from instance fields here
			// since the best time to do that is immediately following test class instantiation
			// and post-processing.
			context.getExtensionRegistry().initializeExtensions(this.testClass, instances.getInnermostInstance());
		});
		return instances;
	}

	protected abstract TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context);

	protected final TestInstances instantiateTestClass(Optional<TestInstances> outerInstances,
			ExtensionRegistry registry, ExtensionContextSupplier extensionContext) {

		Optional<Object> outerInstance = outerInstances.map(TestInstances::getInnermostInstance);
		invokeTestInstancePreConstructCallbacks(new DefaultTestInstanceFactoryContext(this.testClass, outerInstance),
			registry, extensionContext);
		Object instance = this.testInstanceFactory != null //
				? invokeTestInstanceFactory(outerInstance, extensionContext) //
				: invokeTestClassConstructor(outerInstance, registry, extensionContext);
		return outerInstances.map(instances -> DefaultTestInstances.of(instances, instance)).orElse(
			DefaultTestInstances.of(instance));
	}

	private Object invokeTestInstanceFactory(Optional<Object> outerInstance,
			ExtensionContextSupplier extensionContext) {
		Object instance;

		try {
			ExtensionContext actualExtensionContext = extensionContext.get(this.testInstanceFactory);
			instance = this.testInstanceFactory.createTestInstance(
				new DefaultTestInstanceFactoryContext(this.testClass, outerInstance), actualExtensionContext);
		}
		catch (Throwable throwable) {
			UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);

			if (throwable instanceof TestInstantiationException) {
				throw (TestInstantiationException) throwable;
			}

			String message = String.format("TestInstanceFactory [%s] failed to instantiate test class [%s]",
				this.testInstanceFactory.getClass().getName(), this.testClass.getName());
			if (StringUtils.isNotBlank(throwable.getMessage())) {
				message += ": " + throwable.getMessage();
			}
			throw new TestInstantiationException(message, throwable);
		}

		if (!this.testClass.isInstance(instance)) {
			String testClassName = this.testClass.getName();
			Class<?> instanceClass = (instance == null ? null : instance.getClass());
			String instanceClassName = (instanceClass == null ? "null" : instanceClass.getName());

			// If the test instance was loaded via a different ClassLoader, append
			// the identity hash codes to the type names to help users disambiguate
			// between otherwise identical "fully qualified class names".
			if (testClassName.equals(instanceClassName)) {
				testClassName += "@" + Integer.toHexString(System.identityHashCode(this.testClass));
				instanceClassName += "@" + Integer.toHexString(System.identityHashCode(instanceClass));
			}
			String message = String.format(
				"TestInstanceFactory [%s] failed to return an instance of [%s] and instead returned an instance of [%s].",
				this.testInstanceFactory.getClass().getName(), testClassName, instanceClassName);

			throw new TestInstantiationException(message);
		}

		return instance;
	}

	private Object invokeTestClassConstructor(Optional<Object> outerInstance, ExtensionRegistry registry,
			ExtensionContextSupplier extensionContext) {

		Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(this.testClass);
		return executableInvoker.invoke(constructor, outerInstance, extensionContext, registry,
			InvocationInterceptor::interceptTestClassConstructor);
	}

	private void invokeTestInstancePreConstructCallbacks(TestInstanceFactoryContext factoryContext,
			ExtensionRegistry registry, ExtensionContextSupplier context) {
		registry.stream(TestInstancePreConstructCallback.class).forEach(extension -> executeAndMaskThrowable(
			() -> extension.preConstructTestInstance(factoryContext, context.get(extension))));
	}

	private void invokeTestInstancePostProcessors(Object instance, ExtensionRegistry registry,
			ExtensionContextSupplier context) {

		registry.stream(TestInstancePostProcessor.class).forEach(extension -> executeAndMaskThrowable(
			() -> extension.postProcessTestInstance(instance, context.get(extension))));
	}

	private void executeAndMaskThrowable(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable throwable) {
			throw ExceptionUtils.throwAsUncheckedException(throwable);
		}
	}

	private void invokeBeforeAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		for (BeforeAllCallback callback : registry.getExtensions(BeforeAllCallback.class)) {
			throwableCollector.execute(() -> callback.beforeAll(extensionContext));
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	private void invokeBeforeAllMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = extensionContext.getTestInstance().orElse(null);

		for (Method method : this.beforeAllMethods) {
			throwableCollector.execute(() -> {
				try {
					executableInvoker.invoke(method, testInstance, extensionContext, registry,
						ReflectiveInterceptorCall.ofVoidMethod(InvocationInterceptor::interceptBeforeAllMethod));
				}
				catch (Throwable throwable) {
					invokeBeforeAllMethodExecutionExceptionHandlers(registry, extensionContext, throwable);
				}
			});
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	private void invokeBeforeAllMethodExecutionExceptionHandlers(ExtensionRegistry registry, ExtensionContext context,
			Throwable throwable) {

		invokeExecutionExceptionHandlers(LifecycleMethodExecutionExceptionHandler.class, registry, throwable,
			(handler, handledThrowable) -> handler.handleBeforeAllMethodExecutionException(context, handledThrowable));
	}

	private void invokeAfterAllMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = extensionContext.getTestInstance().orElse(null);

		this.afterAllMethods.forEach(method -> throwableCollector.execute(() -> {
			try {
				executableInvoker.invoke(method, testInstance, extensionContext, registry,
					ReflectiveInterceptorCall.ofVoidMethod(InvocationInterceptor::interceptAfterAllMethod));
			}
			catch (Throwable throwable) {
				invokeAfterAllMethodExecutionExceptionHandlers(registry, extensionContext, throwable);
			}
		}));
	}

	private void invokeAfterAllMethodExecutionExceptionHandlers(ExtensionRegistry registry, ExtensionContext context,
			Throwable throwable) {

		invokeExecutionExceptionHandlers(LifecycleMethodExecutionExceptionHandler.class, registry, throwable,
			(handler, handledThrowable) -> handler.handleAfterAllMethodExecutionException(context, handledThrowable));
	}

	private void invokeAfterAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		forEachInReverseOrder(registry.getExtensions(AfterAllCallback.class), //
			extension -> throwableCollector.execute(() -> extension.afterAll(extensionContext)));
	}

	private void invokeTestInstancePreDestroyCallbacks(JupiterEngineExecutionContext context) {
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		forEachInReverseOrder(context.getExtensionRegistry().getExtensions(TestInstancePreDestroyCallback.class), //
			extension -> throwableCollector.execute(() -> extension.preDestroyTestInstance(extensionContext)));
	}

	private boolean isPerClassLifecycle(JupiterEngineExecutionContext context) {
		return context.getExtensionContext().getTestInstanceLifecycle().orElse(
			Lifecycle.PER_METHOD) == Lifecycle.PER_CLASS;
	}

	private void registerBeforeEachMethodAdapters(ExtensionRegistrar registrar) {
		List<Method> beforeEachMethods = findBeforeEachMethods(this.testClass);
		registerMethodsAsExtensions(beforeEachMethods, registrar, this::synthesizeBeforeEachMethodAdapter);
	}

	private void registerAfterEachMethodAdapters(ExtensionRegistrar registrar) {
		// Make a local copy since findAfterEachMethods() returns an immutable list.
		List<Method> afterEachMethods = new ArrayList<>(findAfterEachMethods(this.testClass));

		// Since the bottom-up ordering of afterEachMethods will later be reversed when the
		// synthesized AfterEachMethodAdapters are executed within TestMethodTestDescriptor,
		// we have to reverse the afterEachMethods list to put them in top-down order before
		// we register them as synthesized extensions.
		Collections.reverse(afterEachMethods);

		registerMethodsAsExtensions(afterEachMethods, registrar, this::synthesizeAfterEachMethodAdapter);
	}

	private void registerMethodsAsExtensions(List<Method> methods, ExtensionRegistrar registrar,
			Function<Method, Extension> extensionSynthesizer) {

		methods.forEach(method -> {
			registerExtensionsFromExecutableParameters(registrar, method);
			registrar.registerSyntheticExtension(extensionSynthesizer.apply(method), method);
		});
	}

	private BeforeEachMethodAdapter synthesizeBeforeEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInExtensionContext(method, extensionContext, registry,
			InvocationInterceptor::interceptBeforeEachMethod);
	}

	private AfterEachMethodAdapter synthesizeAfterEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInExtensionContext(method, extensionContext, registry,
			InvocationInterceptor::interceptAfterEachMethod);
	}

	private void invokeMethodInExtensionContext(Method method, ExtensionContext context, ExtensionRegistry registry,
			VoidMethodInterceptorCall interceptorCall) {
		TestInstances testInstances = context.getRequiredTestInstances();
		Object target = testInstances.findInstance(this.testClass).orElseThrow(
			() -> new JUnitException("Failed to find instance for method: " + method.toGenericString()));

		executableInvoker.invoke(method, target, context, registry,
			ReflectiveInterceptorCall.ofVoidMethod(interceptorCall));
	}

}
