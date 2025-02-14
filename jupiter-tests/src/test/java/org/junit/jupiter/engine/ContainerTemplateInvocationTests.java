/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectIteration;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.legacyReportingName;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.uniqueId;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * @since 5.13
 */
public class ContainerTemplateInvocationTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@ValueSource(strings = { //
			"class:org.junit.jupiter.engine.ContainerTemplateInvocationTests$TwoInvocationsTestCase", //
			"uid:[engine:junit-jupiter]/[container-template:org.junit.jupiter.engine.ContainerTemplateInvocationTests$TwoInvocationsTestCase]" //
	})
	void executesContainerTemplateClassTwice(String selectorIdentifier) {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId1 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var invocation1MethodAId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var invocation1NestedClassId = invocationId1.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocation1NestedMethodBId = invocation1NestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var invocation2MethodAId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var invocation2NestedClassId = invocationId2.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocation2NestedMethodBId = invocation2NestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTests(DiscoverySelectors.parse(selectorIdentifier).orElseThrow());

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A of TwoInvocationsTestCase"),
				legacyReportingName("%s[1]".formatted(TwoInvocationsTestCase.class.getName()))), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1MethodAId))), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedClassId))), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1MethodAId)), started()), //
			event(test(uniqueId(invocation1MethodAId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation1NestedClassId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation1NestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of TwoInvocationsTestCase"),
				legacyReportingName("%s[2]".formatted(TwoInvocationsTestCase.class.getName()))), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2MethodAId))), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedClassId))), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2MethodAId)), started()), //
			event(test(uniqueId(invocation2MethodAId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation2NestedClassId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation2NestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesOnlySelectedMethodsDeclaredInContainerTemplate() {
		var results = executeTests(selectMethod(TwoInvocationsTestCase.class, "a"));

		results.testEvents() //
				.assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("a()")), finishedSuccessfully()));
	}

	@Test
	void executesOnlySelectedMethodsDeclaredInNestedClassOfContainerTemplate() {
		var results = executeTests(selectNestedMethod(List.of(TwoInvocationsTestCase.class),
			TwoInvocationsTestCase.NestedTestCase.class, "b"));

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("b()")), finishedSuccessfully()));
	}

	@Test
	void executesOnlyTestsPassingPostDiscoveryFilter() {
		var results = executeTests(request -> request //
				.selectors(selectClass(TwoInvocationsTestCase.class)) //
				.filters(includeTags("nested")));

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("b()")), finishedSuccessfully()));
	}

	@Test
	void prunesEmptyNestedTestClasses() {
		var results = executeTests(request -> request //
				.selectors(selectClass(TwoInvocationsTestCase.class)) //
				.filters(excludeTags("nested")));

		results.containerEvents().assertThatEvents() //
				.noneMatch(container(TwoInvocationsTestCase.NestedTestCase.class.getSimpleName())::matches);

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("a()")), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplateClassTwiceWithClassSelectorForEnclosingClass() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var classId = engineId.append(ClassTestDescriptor.SEGMENT_TYPE,
			NestedContainerTemplateWithTwoInvocationsTestCase.class.getName());
		var methodAId = classId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var nestedContainerTemplateId = classId.append(ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE,
			"NestedTestCase");
		var invocationId1 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var invocation1NestedMethodBId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var invocation2NestedMethodBId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTestsForClass(NestedContainerTemplateWithTwoInvocationsTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(classId)), started()), //

			event(test(uniqueId(methodAId)), started()), //
			event(test(uniqueId(methodAId)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A of NestedTestCase"),
				legacyReportingName("%s[1]".formatted(
					NestedContainerTemplateWithTwoInvocationsTestCase.NestedTestCase.class.getName()))), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of NestedTestCase"),
				legacyReportingName("%s[2]".formatted(
					NestedContainerTemplateWithTwoInvocationsTestCase.NestedTestCase.class.getName()))), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), finishedSuccessfully()), //

			event(container(uniqueId(classId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplateClassTwiceWithNestedClassSelector() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var classId = engineId.append(ClassTestDescriptor.SEGMENT_TYPE,
			NestedContainerTemplateWithTwoInvocationsTestCase.class.getName());
		var nestedContainerTemplateId = classId.append(ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE,
			"NestedTestCase");
		var invocationId1 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var invocation1NestedMethodBId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var invocation2NestedMethodBId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTestsForClass(NestedContainerTemplateWithTwoInvocationsTestCase.NestedTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(classId)), started()), //

			event(container(uniqueId(nestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A of NestedTestCase")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), finishedSuccessfully()), //

			event(container(uniqueId(classId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplatesTwiceEach() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var outerContainerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoTimesTwoInvocationsTestCase.class.getName());

		var outerInvocation1Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var outerInvocation1NestedContainerTemplateId = outerInvocation1Id.append(
			ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation1InnerInvocation1Id = outerInvocation1NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var outerInvocation1InnerInvocation1NestedMethodId = outerInvocation1InnerInvocation1Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
		var outerInvocation1InnerInvocation2Id = outerInvocation1NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation1InnerInvocation2NestedMethodId = outerInvocation1InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		var outerInvocation2Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var outerInvocation2NestedContainerTemplateId = outerInvocation2Id.append(
			ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation2InnerInvocation1Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var outerInvocation2InnerInvocation1NestedMethodId = outerInvocation2InnerInvocation1Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
		var outerInvocation2InnerInvocation2Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation2InnerInvocation2NestedMethodId = outerInvocation2InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		var results = executeTestsForClass(TwoTimesTwoInvocationsTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(outerContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1Id)),
				displayName("[1] A of TwoTimesTwoInvocationsTestCase")), //
			event(container(uniqueId(outerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation1Id)),
				displayName("[1] A of NestedTestCase")), //
			event(container(uniqueId(outerInvocation1InnerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation1NestedMethodId))), //
			event(test(uniqueId(outerInvocation1InnerInvocation1NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation1InnerInvocation1NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1InnerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2Id)),
				displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2Id)),
				displayName("[2] B of TwoTimesTwoInvocationsTestCase")), //
			event(container(uniqueId(outerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation1Id)),
				displayName("[1] A of NestedTestCase")), //
			event(container(uniqueId(outerInvocation2InnerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation1NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation1NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation1NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2Id)),
				displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerContainerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void invocationContextProviderCanRegisterAdditionalExtensions() {
		var results = executeTestsForClass(AdditionalExtensionRegistrationTestCase.class);

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
	}

	@Test
	void eachInvocationHasSeparateExtensionContext() {
		var results = executeTestsForClass(SeparateExtensionContextTestCase.class);

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
	}

	@Test
	void supportsTestTemplateMethodsInsideContainerTemplateClasses() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			CombinationWithTestTemplateTestCase.class.getName());
		var invocationId1 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var testTemplateId1 = invocationId1.append(TestTemplateTestDescriptor.SEGMENT_TYPE, "test(int)");
		var testTemplate1InvocationId1 = testTemplateId1.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var testTemplate1InvocationId2 = testTemplateId1.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var testTemplateId2 = invocationId2.append(TestTemplateTestDescriptor.SEGMENT_TYPE, "test(int)");
		var testTemplate2InvocationId1 = testTemplateId2.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var testTemplate2InvocationId2 = testTemplateId2.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");

		var results = executeTestsForClass(CombinationWithTestTemplateTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)),
				displayName("[1] A of CombinationWithTestTemplateTestCase")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplateId1))), //
			event(container(uniqueId(testTemplateId1)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplate1InvocationId1))), //
			event(test(uniqueId(testTemplate1InvocationId1)), started()), //
			event(test(uniqueId(testTemplate1InvocationId1)), finishedSuccessfully()), //
			event(dynamicTestRegistered(uniqueId(testTemplate1InvocationId2))), //
			event(test(uniqueId(testTemplate1InvocationId2)), started()), //
			event(test(uniqueId(testTemplate1InvocationId2)), finishedSuccessfully()), //
			event(container(uniqueId(testTemplateId1)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)),
				displayName("[2] B of CombinationWithTestTemplateTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplateId2))), //
			event(container(uniqueId(testTemplateId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplate2InvocationId1))), //
			event(test(uniqueId(testTemplate2InvocationId1)), started()), //
			event(test(uniqueId(testTemplate2InvocationId1)), finishedSuccessfully()), //
			event(dynamicTestRegistered(uniqueId(testTemplate2InvocationId2))), //
			event(test(uniqueId(testTemplate2InvocationId2)), started()), //
			event(test(uniqueId(testTemplate2InvocationId2)), finishedSuccessfully()), //
			event(container(uniqueId(testTemplateId2)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void testTemplateInvocationInsideContainerTemplateClassCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			CombinationWithTestTemplateTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var testTemplateId2 = invocationId2.append(TestTemplateTestDescriptor.SEGMENT_TYPE, "test(int)");
		var testTemplate2InvocationId2 = testTemplateId2.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");

		var results = executeTests(selectUniqueId(testTemplate2InvocationId2));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)),
				displayName("[2] B of CombinationWithTestTemplateTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplateId2))), //
			event(container(uniqueId(testTemplateId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testTemplate2InvocationId2))), //
			event(test(uniqueId(testTemplate2InvocationId2)), started()), //
			event(test(uniqueId(testTemplate2InvocationId2)), finishedSuccessfully()), //
			event(container(uniqueId(testTemplateId2)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void supportsTestFactoryMethodsInsideContainerTemplateClasses() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			CombinationWithTestFactoryTestCase.class.getName());
		var invocationId1 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var testFactoryId1 = invocationId1.append(TestFactoryTestDescriptor.SEGMENT_TYPE, "test()");
		var testFactory1DynamicTestId1 = testFactoryId1.append(TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE,
			"#1");
		var testFactory1DynamicTestId2 = testFactoryId1.append(TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE,
			"#2");
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var testFactoryId2 = invocationId2.append(TestFactoryTestDescriptor.SEGMENT_TYPE, "test()");
		var testFactory2DynamicTestId1 = testFactoryId2.append(TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE,
			"#1");
		var testFactory2DynamicTestId2 = testFactoryId2.append(TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE,
			"#2");

		var results = executeTestsForClass(CombinationWithTestFactoryTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)),
				displayName("[1] A of CombinationWithTestFactoryTestCase")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactoryId1))), //
			event(container(uniqueId(testFactoryId1)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactory1DynamicTestId1))), //
			event(test(uniqueId(testFactory1DynamicTestId1)), started()), //
			event(test(uniqueId(testFactory1DynamicTestId1)), finishedSuccessfully()), //
			event(dynamicTestRegistered(uniqueId(testFactory1DynamicTestId2))), //
			event(test(uniqueId(testFactory1DynamicTestId2)), started()), //
			event(test(uniqueId(testFactory1DynamicTestId2)), finishedSuccessfully()), //
			event(container(uniqueId(testFactoryId1)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)),
				displayName("[2] B of CombinationWithTestFactoryTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactoryId2))), //
			event(container(uniqueId(testFactoryId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactory2DynamicTestId1))), //
			event(test(uniqueId(testFactory2DynamicTestId1)), started()), //
			event(test(uniqueId(testFactory2DynamicTestId1)), finishedSuccessfully()), //
			event(dynamicTestRegistered(uniqueId(testFactory2DynamicTestId2))), //
			event(test(uniqueId(testFactory2DynamicTestId2)), started()), //
			event(test(uniqueId(testFactory2DynamicTestId2)), finishedSuccessfully()), //
			event(container(uniqueId(testFactoryId2)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void specificDynamicTestInsideContainerTemplateClassCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			CombinationWithTestFactoryTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var testFactoryId2 = invocationId2.append(TestFactoryTestDescriptor.SEGMENT_TYPE, "test()");
		var testFactory2DynamicTestId2 = testFactoryId2.append(TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE,
			"#2");

		var results = executeTests(selectUniqueId(testFactory2DynamicTestId2));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)),
				displayName("[2] B of CombinationWithTestFactoryTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactoryId2))), //
			event(container(uniqueId(testFactoryId2)), started()), //
			event(dynamicTestRegistered(uniqueId(testFactory2DynamicTestId2))), //
			event(test(uniqueId(testFactory2DynamicTestId2)), started()), //
			event(test(uniqueId(testFactory2DynamicTestId2)), finishedSuccessfully()), //
			event(container(uniqueId(testFactoryId2)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void failsIfProviderReturnsZeroInvocationContextWithoutOptIn() {
		var results = executeTestsForClass(InvalidZeroInvocationTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(InvalidZeroInvocationTestCase.class), started()), //
			event(container(InvalidZeroInvocationTestCase.class),
				finishedWithFailure(
					message("Provider [Ext] did not provide any invocation contexts, but was expected to do so. "
							+ "You may override mayReturnZeroContainerTemplateInvocationContexts() to allow this."))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void succeedsIfProviderReturnsZeroInvocationContextWithOptIn() {
		var results = executeTestsForClass(ValidZeroInvocationTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(ValidZeroInvocationTestCase.class), started()), //
			event(container(ValidZeroInvocationTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@ParameterizedTest
	@ValueSource(classes = { NoProviderRegisteredTestCase.class, NoSupportingProviderRegisteredTestCase.class })
	void failsIfNoSupportingProviderIsRegistered(Class<?> testClass) {
		var results = executeTestsForClass(testClass);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass),
				finishedWithFailure(
					message("You must register at least one ContainerTemplateInvocationContextProvider that supports "
							+ "@ContainerTemplate class [" + testClass.getName() + "]"))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void containerTemplateInvocationCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var methodAId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var nestedClassId = invocationId2.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var nestedMethodBId = nestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTests(selectUniqueId(invocationId2));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of TwoInvocationsTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(methodAId))), //
			event(dynamicTestRegistered(uniqueId(nestedClassId))), //
			event(dynamicTestRegistered(uniqueId(nestedMethodBId))), //
			event(test(uniqueId(methodAId)), started()), //
			event(test(uniqueId(methodAId)), finishedSuccessfully()), //
			event(container(uniqueId(nestedClassId)), started()), //
			event(test(uniqueId(nestedMethodBId)), started()), //
			event(test(uniqueId(nestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(nestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void containerTemplateInvocationCanBeSelectedByIteration() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var methodAId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var nestedClassId = invocationId2.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var nestedMethodBId = nestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTests(selectIteration(selectClass(TwoInvocationsTestCase.class), 1));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of TwoInvocationsTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(methodAId))), //
			event(dynamicTestRegistered(uniqueId(nestedClassId))), //
			event(dynamicTestRegistered(uniqueId(nestedMethodBId))), //
			event(test(uniqueId(methodAId)), started()), //
			event(test(uniqueId(methodAId)), finishedSuccessfully()), //
			event(container(uniqueId(nestedClassId)), started()), //
			event(test(uniqueId(nestedMethodBId)), started()), //
			event(test(uniqueId(nestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(nestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@ParameterizedTest
	@ValueSource(strings = { //
			"class:org.junit.jupiter.engine.ContainerTemplateInvocationTests$TwoInvocationsTestCase", //
			"uid:[engine:junit-jupiter]/[container-template:org.junit.jupiter.engine.ContainerTemplateInvocationTests$TwoInvocationsTestCase]" //
	})
	void executesAllInvocationsForRedundantSelectors(String containerTemplateSelectorIdentifier) {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");

		var results = executeTests(selectUniqueId(invocationId2),
			DiscoverySelectors.parse(containerTemplateSelectorIdentifier).orElseThrow());

		results.testEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
	}

	@Test
	void methodInContainerTemplateInvocationCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var methodAId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");

		var results = executeTests(selectUniqueId(methodAId));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of TwoInvocationsTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(methodAId))), //
			event(test(uniqueId(methodAId)), started()), //
			event(test(uniqueId(methodAId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void nestedMethodInContainerTemplateInvocationCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var nestedClassId = invocationId2.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var nestedMethodBId = nestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTests(selectUniqueId(nestedMethodBId));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B of TwoInvocationsTestCase")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(nestedClassId))), //
			event(dynamicTestRegistered(uniqueId(nestedMethodBId))), //
			event(container(uniqueId(nestedClassId)), started()), //
			event(test(uniqueId(nestedMethodBId)), started()), //
			event(test(uniqueId(nestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(nestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void nestedContainerTemplateInvocationCanBeSelectedByUniqueId() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var outerContainerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoTimesTwoInvocationsWithMultipleMethodsTestCase.class.getName());
		var outerInvocation2Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var outerInvocation2NestedContainerTemplateId = outerInvocation2Id.append(
			ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation2InnerInvocation2Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation2InnerInvocation2NestedMethodId = outerInvocation2InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTests(selectUniqueId(outerInvocation2InnerInvocation2NestedMethodId));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(outerContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2Id)),
				displayName("[2] B of TwoTimesTwoInvocationsWithMultipleMethodsTestCase")), //
			event(container(uniqueId(outerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2Id)),
				displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerContainerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void nestedContainerTemplateInvocationCanBeSelectedByIteration() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var outerContainerTemplateId = engineId.append(ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			TwoTimesTwoInvocationsTestCase.class.getName());
		var outerInvocation1Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var outerInvocation1NestedContainerTemplateId = outerInvocation1Id.append(
			ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation1InnerInvocation2Id = outerInvocation1NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation1InnerInvocation2NestedMethodId = outerInvocation1InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
		var outerInvocation2Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var outerInvocation2NestedContainerTemplateId = outerInvocation2Id.append(
			ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation2InnerInvocation2Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation2InnerInvocation2NestedMethodId = outerInvocation2InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		var results = executeTests(selectIteration(selectNestedClass(List.of(TwoTimesTwoInvocationsTestCase.class),
			TwoTimesTwoInvocationsTestCase.NestedTestCase.class), 1));

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(outerContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1Id)),
				displayName("[1] A of TwoTimesTwoInvocationsTestCase")), //
			event(container(uniqueId(outerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2Id)),
				displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2Id)),
				displayName("[2] B of TwoTimesTwoInvocationsTestCase")), //
			event(container(uniqueId(outerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2Id)),
				displayName("[2] B of NestedTestCase")), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerContainerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesLifecycleCallbackMethodsInNestedContainerTemplates() {
		var results = executeTestsForClass(TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase.class);

		results.containerEvents().assertStatistics(stats -> stats.started(10).succeeded(10));
		results.testEvents().assertStatistics(stats -> stats.started(4).succeeded(4));

		var callSequence = results.allEvents().reportingEntryPublished() //
				.map(event -> event.getRequiredPayload(ReportEntry.class)) //
				.map(ReportEntry::getKeyValuePairs) //
				.map(Map::values) //
				.flatMap(Collection::stream);
		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeAll: TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase",
				"beforeAll: NestedTestCase",
					"beforeEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
						"beforeEach: test [NestedTestCase]",
							"test",
						"afterEach: test [NestedTestCase]",
					"afterEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
					"beforeEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
						"beforeEach: test [NestedTestCase]",
							"test",
						"afterEach: test [NestedTestCase]",
					"afterEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
				"afterAll: NestedTestCase",
				"beforeAll: NestedTestCase",
					"beforeEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
						"beforeEach: test [NestedTestCase]",
							"test",
						"afterEach: test [NestedTestCase]",
					"afterEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
					"beforeEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
						"beforeEach: test [NestedTestCase]",
							"test",
						"afterEach: test [NestedTestCase]",
					"afterEach: test [TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase]",
				"afterAll: NestedTestCase",
			"afterAll: TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase"
		);
		// @formatter:on
	}

	// -------------------------------------------------------------------

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class TwoInvocationsTestCase {
		@Test
		void a() {
		}

		@Nested
		class NestedTestCase {
			@Test
			@Tag("nested")
			void b() {
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class NestedContainerTemplateWithTwoInvocationsTestCase {
		@Test
		void a() {
		}

		@Nested
		@ContainerTemplate
		@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
		class NestedTestCase {
			@Test
			void b() {
			}
		}
	}

	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	@ContainerTemplate
	static class TwoTimesTwoInvocationsTestCase {
		@Nested
		@ContainerTemplate
		class NestedTestCase {
			@Test
			void test() {
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class TwoInvocationsWithExtensionTestCase {
		@Test
		void a() {
		}

		@Nested
		class NestedTestCase {
			@Test
			@Tag("nested")
			void b() {
			}
		}
	}

	static class TwoInvocationsContainerTemplateInvocationContextProvider
			implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<Ctx> provideContainerTemplateInvocationContexts(ExtensionContext context) {
			var suffix = " of %s".formatted(context.getRequiredTestClass().getSimpleName());
			return Stream.of(new Ctx("A" + suffix), new Ctx("B" + suffix));
		}

		record Ctx(String displayName) implements ContainerTemplateInvocationContext {
			@Override
			public String getDisplayName(int invocationIndex) {
				var defaultDisplayName = ContainerTemplateInvocationContext.super.getDisplayName(invocationIndex);
				return "%s %s".formatted(defaultDisplayName, displayName);
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(AdditionalExtensionRegistrationTestCase.Ext.class)
	static class AdditionalExtensionRegistrationTestCase {

		@Test
		void test(Data data) {
			assertNotNull(data);
			assertNotNull(data.value());
		}

		static class Ext implements ContainerTemplateInvocationContextProvider {
			@Override
			public boolean supportsContainerTemplate(ExtensionContext context) {
				return true;
			}

			@Override
			public Stream<Ctx> provideContainerTemplateInvocationContexts(ExtensionContext context) {
				return Stream.of(new Data("A"), new Data("B")).map(Ctx::new);
			}
		}

		record Ctx(Data data) implements ContainerTemplateInvocationContext {
			@Override
			public String getDisplayName(int invocationIndex) {
				return this.data.value();
			}

			@Override
			public List<Extension> getAdditionalExtensions() {
				return List.of(new ParameterResolver() {
					@Override
					public boolean supportsParameter(ParameterContext parameterContext,
							ExtensionContext extensionContext) throws ParameterResolutionException {
						return Data.class.equals(parameterContext.getParameter().getType());
					}

					@Override
					public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
							throws ParameterResolutionException {
						return Ctx.this.data;
					}
				});
			}
		}

		record Data(String value) {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	@ExtendWith(SeparateExtensionContextTestCase.SomeResourceExtension.class)
	static class SeparateExtensionContextTestCase {

		@Test
		void test(SomeResource someResource) {
			assertFalse(someResource.closed);
		}

		static class SomeResourceExtension implements BeforeAllCallback, ParameterResolver {

			@Override
			public void beforeAll(ExtensionContext context) throws Exception {
				context.getStore(ExtensionContext.Namespace.GLOBAL).put("someResource", new SomeResource());
			}

			@Override
			public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
					throws ParameterResolutionException {
				var parentContext = extensionContext.getParent().orElseThrow();
				assertAll( //
					() -> assertEquals(SeparateExtensionContextTestCase.class, parentContext.getRequiredTestClass()), //
					() -> assertEquals(SeparateExtensionContextTestCase.class,
						parentContext.getElement().orElseThrow()), //
					() -> assertEquals(TestInstance.Lifecycle.PER_METHOD,
						parentContext.getTestInstanceLifecycle().orElseThrow()) //
				);
				return SomeResource.class.equals(parameterContext.getParameter().getType());
			}

			@Override
			public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
					throws ParameterResolutionException {
				return extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get("someResource");
			}
		}

		static class SomeResource implements CloseableResource {
			private boolean closed;

			@Override
			public void close() {
				this.closed = true;
			}
		}
	}

	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class CombinationWithTestTemplateTestCase {

		@ParameterizedTest
		@ValueSource(ints = { 1, 2 })
		void test(int i) {
			assertNotEquals(0, i);
		}
	}

	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class CombinationWithTestFactoryTestCase {

		@TestFactory
		Stream<DynamicTest> test() {
			return IntStream.of(1, 2) //
					.mapToObj(i -> dynamicTest("test" + i, () -> assertNotEquals(0, i)));
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(InvalidZeroInvocationTestCase.Ext.class)
	static class InvalidZeroInvocationTestCase {

		@Test
		void test() {
			fail("should not be called");
		}

		static class Ext implements ContainerTemplateInvocationContextProvider {

			@Override
			public boolean supportsContainerTemplate(ExtensionContext context) {
				return true;
			}

			@Override
			public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
					ExtensionContext context) {
				return Stream.empty();
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(ValidZeroInvocationTestCase.Ext.class)
	static class ValidZeroInvocationTestCase {

		@Test
		void test() {
			fail("should not be called");
		}

		static class Ext implements ContainerTemplateInvocationContextProvider {

			@Override
			public boolean supportsContainerTemplate(ExtensionContext context) {
				return true;
			}

			@Override
			public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
					ExtensionContext context) {
				return Stream.empty();
			}

			@Override
			public boolean mayReturnZeroContainerTemplateInvocationContexts(ExtensionContext context) {
				return true;
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	static class NoProviderRegisteredTestCase {

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(NoSupportingProviderRegisteredTestCase.Ext.class)
	static class NoSupportingProviderRegisteredTestCase {

		@Test
		void test() {
			fail("should not be called");
		}

		static class Ext implements ContainerTemplateInvocationContextProvider {

			@Override
			public boolean supportsContainerTemplate(ExtensionContext context) {
				return false;
			}

			@Override
			public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
					ExtensionContext context) {
				throw new RuntimeException("should not be called");
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	@ContainerTemplate
	static class TwoTimesTwoInvocationsWithMultipleMethodsTestCase {

		@Test
		void test() {
		}

		@Nested
		@ContainerTemplate
		class NestedTestCase {
			@Test
			void a() {
			}

			@Test
			void b() {
			}
		}

		@Nested
		@ContainerTemplate
		class AnotherNestedTestCase {
			@Test
			void test() {
			}
		}
	}

	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	@ContainerTemplate
	static class TwoTimesTwoInvocationsWithLifecycleCallbacksTestCase extends LifecycleCallbacks {
		@Nested
		@ContainerTemplate
		class NestedTestCase extends LifecycleCallbacks {
			@Test
			@DisplayName("test")
			void test(TestReporter testReporter) {
				testReporter.publishEntry("test");
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class LifecycleCallbacks {
		@BeforeAll
		static void beforeAll(TestReporter testReporter, TestInfo testInfo) {
			testReporter.publishEntry("beforeAll: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}

		@BeforeEach
		void beforeEach(TestReporter testReporter, TestInfo testInfo) {
			testReporter.publishEntry(
				"beforeEach: " + testInfo.getDisplayName() + " [" + getClass().getSimpleName() + "]");
		}

		@AfterEach
		void afterEach(TestReporter testReporter, TestInfo testInfo) {
			testReporter.publishEntry(
				"afterEach: " + testInfo.getDisplayName() + " [" + getClass().getSimpleName() + "]");
		}

		@AfterAll
		static void afterAll(TestReporter testReporter, TestInfo testInfo) {
			testReporter.publishEntry("afterAll: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}
	}
}
