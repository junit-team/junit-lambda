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
 * Unit tests for {@link DisabledOnJreCondition}, generated from
 * {@code DisabledOnJreConditionTests.java.jte}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledOnJreConditionTests extends AbstractExecutionConditionTests {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledOnJreIntegrationTests.class;
	}

	/**
	 * @see DisabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledOnJre is not present");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#missingVersionDeclaration()
	 */
	@Test
	void missingVersionDeclaration() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("You must declare at least one JRE or version in @DisabledOnJre");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#jreUndefined()
	 */
	@Test
	void jreUndefined() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("JRE.UNDEFINED is not supported in @DisabledOnJre");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#version7()
	 */
	@Test
	void version7() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(this::evaluateCondition)//
				.withMessage("Version [7] in @DisabledOnJre must be greater than or equal to 8");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#disabledOnAllJavaVersions()
	 */
	@Test
	void disabledOnAllJavaVersions() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(true);
		assertCustomDisabledReasonIs("Disabled on every JRE");
	}
@for(var jre : jres)
	/**
	 * @see DisabledOnJreIntegrationTests#jre${jre.getVersion()}()
	 */
	@Test
	void jre${jre.getVersion()}() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor<%--
--%>@for(var jre : jres)
	/**
	 * @see DisabledOnJreIntegrationTests#version${jre.getVersion()}()
	 */
	@Test
	void version${jre.getVersion()}() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor
	/**
	 * @see DisabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!onKnownVersion());
	}

	private void assertDisabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + JAVA_VERSION);
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + JAVA_VERSION);
		}
	}

}
