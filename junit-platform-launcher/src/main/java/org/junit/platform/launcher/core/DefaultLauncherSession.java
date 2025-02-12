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

import java.util.List;
import java.util.function.Supplier;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.8
 */
class DefaultLauncherSession implements LauncherSession {

	private static final LauncherInterceptor NOOP_INTERCEPTOR = new LauncherInterceptor() {
		@Override
		public <T> T intercept(Invocation<T> invocation) {
			return invocation.proceed();
		}

		@Override
		public void close() {
			// do nothing
		}
	};

	private final LauncherInterceptor interceptor;
	private final LauncherSessionListener listener;
	private final DelegatingLauncher launcher;
	private final NamespacedHierarchicalStore<Namespace> store;

	DefaultLauncherSession(List<LauncherInterceptor> interceptors, Supplier<LauncherSessionListener> listenerSupplier,
			Function<NamespacedHierarchicalStore<Namespace>, Launcher> launcherFactory) {
		interceptor = composite(interceptors);
		Launcher launcher;
		if (interceptor == NOOP_INTERCEPTOR) {
			this.listener = listenerSupplier.get();
			launcher = launcherSupplier.get();
		}
		else {
			this.listener = interceptor.intercept(listenerSupplier::get);
			launcher = new InterceptingLauncher(interceptor.intercept(launcherSupplier::get), interceptor);
		}
		this.launcher = new DelegatingLauncher(launcher);
		listener.launcherSessionOpened(this);
		this.store = new NamespacedHierarchicalStore<>(null);
	}

	@Override
	public Launcher getLauncher() {
		return launcher;
	}

	LauncherSessionListener getListener() {
		return listener;
	}

	@Override
	public void close() {
		if (launcher.delegate != ClosedLauncher.INSTANCE) {
			launcher.delegate = ClosedLauncher.INSTANCE;
			listener.launcherSessionClosed(this);
			interceptor.close();
		}
	}

	@Override
	public NamespacedHierarchicalStore<Namespace> getStore() {
		return store;
	}

	private static class ClosedLauncher implements Launcher {

		static final ClosedLauncher INSTANCE = new ClosedLauncher();

		private ClosedLauncher() {
		}

		@Override
		public void registerLauncherDiscoveryListeners(LauncherDiscoveryListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void registerTestExecutionListeners(TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}
	}

	private static LauncherInterceptor composite(List<LauncherInterceptor> interceptors) {
		if (interceptors.isEmpty()) {
			return NOOP_INTERCEPTOR;
		}
		return interceptors.stream() //
				.skip(1) //
				.reduce(interceptors.get(0), (a, b) -> new LauncherInterceptor() {
					@Override
					public void close() {
						try {
							a.close();
						}
						finally {
							b.close();
						}
					}

					@Override
					public <T> T intercept(Invocation<T> invocation) {
						return a.intercept(() -> b.intercept(invocation));
					}
				});
	}
}
