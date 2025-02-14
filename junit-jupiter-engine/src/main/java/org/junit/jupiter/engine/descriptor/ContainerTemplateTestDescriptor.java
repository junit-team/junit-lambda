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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.execution.ExtensionContextSupplier;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateTestDescriptor extends ClassBasedTestDescriptor implements Filterable {

	public static final String STATIC_CLASS_SEGMENT_TYPE = "container-template";
	public static final String NESTED_CLASS_SEGMENT_TYPE = "nested-container-template";

	private final Map<Integer, Collection<? extends TestDescriptor>> childrenPrototypesByIndex = new HashMap<>();
	private final List<TestDescriptor> childrenPrototypes = new ArrayList<>();
	private final ClassBasedTestDescriptor delegate;
	private final DynamicDescendantFilter dynamicDescendantFilter;

	public ContainerTemplateTestDescriptor(UniqueId uniqueId, ClassBasedTestDescriptor delegate) {
		this(uniqueId, delegate, new DynamicDescendantFilter());
	}

	private ContainerTemplateTestDescriptor(UniqueId uniqueId, ClassBasedTestDescriptor delegate,
			DynamicDescendantFilter dynamicDescendantFilter) {
		super(uniqueId, delegate.getTestClass(), delegate.getDisplayName(), delegate.configuration);
		this.delegate = delegate;
		this.dynamicDescendantFilter = dynamicDescendantFilter;
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	// --- Filterable ----------------------------------------------------------

	@Override
	public DynamicDescendantFilter getDynamicDescendantFilter() {
		return dynamicDescendantFilter;
	}

	// --- JupiterTestDescriptor -----------------------------------------------

	@Override
	protected JupiterTestDescriptor copyIncludingDescendants(UnaryOperator<UniqueId> uniqueIdTransformer) {
		ContainerTemplateTestDescriptor copy = (ContainerTemplateTestDescriptor) super.copyIncludingDescendants(
			uniqueIdTransformer);
		this.childrenPrototypes.forEach(oldChild -> {
			TestDescriptor newChild = ((JupiterTestDescriptor) oldChild).copyIncludingDescendants(uniqueIdTransformer);
			copy.childrenPrototypes.add(newChild);
		});
		this.childrenPrototypesByIndex.forEach((index, oldChildren) -> {
			List<TestDescriptor> newChildren = oldChildren.stream() //
					.map(oldChild -> ((JupiterTestDescriptor) oldChild).copyIncludingDescendants(uniqueIdTransformer)) //
					.collect(toList());
			copy.childrenPrototypesByIndex.put(index, newChildren);
		});
		return copy;
	}

	@Override
	protected ContainerTemplateTestDescriptor withUniqueId(UnaryOperator<UniqueId> uniqueIdTransformer) {
		return new ContainerTemplateTestDescriptor(uniqueIdTransformer.apply(getUniqueId()), this.delegate,
			this.dynamicDescendantFilter.copy(uniqueIdTransformer));
	}

	@Override
	public void prunePriorToFiltering() {
		// do nothing to allow PostDiscoveryFilters to be applied first
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public void prune() {
		super.prune();
		this.children.forEach(child -> child.accept(TestDescriptor::prune));
		// Second iteration to avoid processing children that were pruned in the first iteration
		this.children.forEach(child -> {
			if (child instanceof ContainerTemplateInvocationTestDescriptor) {
				int index = ((ContainerTemplateInvocationTestDescriptor) child).getIndex();
				this.dynamicDescendantFilter.allowIndex(index - 1);
				this.childrenPrototypesByIndex.put(index, child.getChildren());
			}
			else {
				this.childrenPrototypes.add(child);
			}
		});
		this.children.clear();
	}

	@Override
	public boolean mayRegisterTests() {
		return !childrenPrototypes.isEmpty() || !childrenPrototypesByIndex.isEmpty();
	}

	// --- TestClassAware ------------------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return delegate.getEnclosingTestClasses();
	}

	// --- ClassBasedTestDescriptor --------------------------------------------

	@Override
	public TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {
		return delegate.instantiateTestClass(parentExecutionContext, extensionContext, registry, context);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return delegate.getResourceLocksProviderEvaluator();
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public Set<ExclusiveResource> getExclusiveResources() {
		Set<ExclusiveResource> result = determineExclusiveResources().collect(toCollection(HashSet::new));
		Visitor visitor = testDescriptor -> {
			if (testDescriptor instanceof Node) {
				result.addAll(((Node<?>) testDescriptor).getExclusiveResources());
			}
		};
		this.childrenPrototypes.forEach(child -> child.accept(visitor));
		this.childrenPrototypesByIndex.values() //
				.forEach(prototypes -> prototypes //
						.forEach(child -> child.accept(visitor)));
		return result;
	}

	@Override
	public void cleanUp(JupiterEngineExecutionContext context) {
		this.childrenPrototypes.clear();
		this.childrenPrototypesByIndex.clear();
		this.dynamicDescendantFilter.allowAll();
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {

		new ContainerTemplateExecutor().execute(context, dynamicTestExecutor);
		return context;
	}

	class ContainerTemplateExecutor
			extends TemplateExecutor<ContainerTemplateInvocationContextProvider, ContainerTemplateInvocationContext> {

		public ContainerTemplateExecutor() {
			super(ContainerTemplateTestDescriptor.this, ContainerTemplateInvocationContextProvider.class);
		}

		@Override
		boolean supports(ContainerTemplateInvocationContextProvider provider, ExtensionContext extensionContext) {
			return provider.supportsContainerTemplate(extensionContext);
		}

		@Override
		protected String getNoRegisteredProviderErrorMessage() {
			return String.format("You must register at least one %s that supports @%s class [%s]",
				ContainerTemplateInvocationContextProvider.class.getSimpleName(),
				ContainerTemplate.class.getSimpleName(), getTestClass().getName());
		}

		@Override
		Stream<? extends ContainerTemplateInvocationContext> provideContexts(
				ContainerTemplateInvocationContextProvider provider, ExtensionContext extensionContext) {
			return provider.provideContainerTemplateInvocationContexts(extensionContext);
		}

		@Override
		boolean mayReturnZeroContexts(ContainerTemplateInvocationContextProvider provider,
				ExtensionContext extensionContext) {
			return provider.mayReturnZeroContainerTemplateInvocationContexts(extensionContext);
		}

		@Override
		protected String getZeroContextsProvidedErrorMessage(ContainerTemplateInvocationContextProvider provider) {
			return String.format(
				"Provider [%s] did not provide any invocation contexts, but was expected to do so. "
						+ "You may override mayReturnZeroContainerTemplateInvocationContexts() to allow this.",
				provider.getClass().getSimpleName());
		}

		@Override
		UniqueId createInvocationUniqueId(UniqueId parentUniqueId, int index) {
			return parentUniqueId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
		}

		@Override
		TestDescriptor createInvocationTestDescriptor(UniqueId uniqueId,
				ContainerTemplateInvocationContext invocationContext, int index) {
			ContainerTemplateInvocationTestDescriptor containerInvocationDescriptor = new ContainerTemplateInvocationTestDescriptor(
				uniqueId, ContainerTemplateTestDescriptor.this, invocationContext, index, getSource().orElse(null),
				ContainerTemplateTestDescriptor.this.configuration);

			collectChildren(index, uniqueId) //
					.forEach(containerInvocationDescriptor::addChild);

			return containerInvocationDescriptor;
		}

		private Stream<? extends TestDescriptor> collectChildren(int index, UniqueId invocationUniqueId) {
			if (ContainerTemplateTestDescriptor.this.childrenPrototypesByIndex.containsKey(index)) {
				return ContainerTemplateTestDescriptor.this.childrenPrototypesByIndex.remove(index).stream();
			}
			UnaryOperator<UniqueId> transformer = new UniqueIdPrefixTransformer(getUniqueId(), invocationUniqueId);
			return ContainerTemplateTestDescriptor.this.childrenPrototypes.stream() //
					.map(JupiterTestDescriptor.class::cast) //
					.map(it -> it.copyIncludingDescendants(transformer));
		}
	}

}
