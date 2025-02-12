@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param List<JRE> jresSortedByStringValue
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledOnJreCondition}, generated from
 * {@code EnabledOnJreConditionTests.java.jte}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledOnJreConditionTests extends AbstractExecutionConditionTests {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledOnJreIntegrationTests.class;
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledOnJre is not present");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#missingVersionDeclaration()
	 */
	@Test
	void missingVersionDeclaration() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("You must declare at least one JRE or version in @EnabledOnJre");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#jreUndefined()
	 */
	@Test
	void jreUndefined() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("JRE.UNDEFINED is not supported in @EnabledOnJre");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#version7()
	 */
	@Test
	void version7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("Version [7] in @EnabledOnJre must be greater than or equal to 8");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledOnAllJavaVersions()
	 */
	@Test
	void enabledOnAllJavaVersions() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(true);
	}
@for(var jre : jres)
	/**
	 * @see EnabledOnJreIntegrationTests#jre${jre.getVersion()}()
	 */
	@Test
	void jre${jre.getVersion()}() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor<%--
--%>@for(var jre : jres)
	/**
	 * @see EnabledOnJreIntegrationTests#version${jre.getVersion()}()
	 */
	@Test
	void version${jre.getVersion()}() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor
	/**
	 * @see EnabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!onKnownVersion());
		assertCustomDisabledReasonIs("Disabled on almost every JRE");
	}

	private void assertEnabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + JAVA_VERSION);
		}
		else {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + JAVA_VERSION);
		}
	}

}
