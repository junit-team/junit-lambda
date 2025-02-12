@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param List<JRE> jresSortedByStringValue
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JRE.JAVA_${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JRE.OTHER;
import static org.junit.jupiter.api.condition.JRE.UNDEFINED;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledOnJre @EnabledOnJre}, generated from
 * {@code EnabledOnJreIntegrationTests.java.jte}.
 *
 * @since 5.1
 */
class EnabledOnJreIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnJre({})
	void missingVersionDeclaration() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnJre(UNDEFINED)
	void jreUndefined() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnJre(versions = 7)
	void version7() {
	}

	@Test
	@EnabledOnJre({ //
@for(var jre : jres)<%--
--%>			JAVA_${jre.getVersion()}, //
@endfor<%--
--%>			OTHER //
	})
	void enabledOnAllJavaVersions() {
	}
@for(var jre : jres)
	@Test
	@EnabledOnJre(JAVA_${jre.getVersion()})
	void jre${jre.getVersion()}() {
		assertTrue(onJava${jre.getVersion()}());
	}
@endfor<%--
--%>@for(var jre : jres)
	@Test
	@EnabledOnJre(versions = ${jre.getVersion()})
	void version${jre.getVersion()}() {
		assertTrue(onJava${jre.getVersion()}());
	}
@endfor
	@Test
	@EnabledOnJre(value = OTHER, disabledReason = "Disabled on almost every JRE")
	void other() {
		assertFalse(onKnownVersion());
	}

}
