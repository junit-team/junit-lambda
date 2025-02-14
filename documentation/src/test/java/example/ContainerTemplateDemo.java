/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import example.ContainerTemplateDemo.MyContainerTemplateInvocationContextProvider;

import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

// tag::user_guide[]
@ContainerTemplate
@ExtendWith(MyContainerTemplateInvocationContextProvider.class)
class ContainerTemplateDemo {

	static final List<String> WELL_KNOWN_FRUITS
	// tag::custom_line_break[]
		= unmodifiableList(Arrays.asList("apple", "banana", "lemon"));

	private String fruit;

	@Test
	void notNull() {
		assertNotNull(fruit);
	}

	@Test
	void wellKnown() {
		assertTrue(WELL_KNOWN_FRUITS.contains(fruit));
	}

	// end::user_guide[]
	static
	// tag::user_guide[]
	public class MyContainerTemplateInvocationContextProvider
			// tag::custom_line_break[]
			implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<ContainerTemplateInvocationContext>
				// tag::custom_line_break[]
				provideContainerTemplateInvocationContexts(ExtensionContext context) {

			return Stream.of(invocationContext("apple"), invocationContext("banana"));
		}

		private ContainerTemplateInvocationContext invocationContext(String parameter) {
			return new ContainerTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return parameter;
				}

				// end::user_guide[]
				@SuppressWarnings("Convert2Lambda")
				// tag::user_guide[]
				@Override
				public List<Extension> getAdditionalExtensions() {
					return singletonList(new TestInstancePostProcessor() {
						@Override
						public void postProcessTestInstance(
								// tag::custom_line_break[]
								Object testInstance, ExtensionContext context) {
							((ContainerTemplateDemo) testInstance).fruit = parameter;
						}
					});
				}
			};
		}
	}
}
// end::user_guide[]
