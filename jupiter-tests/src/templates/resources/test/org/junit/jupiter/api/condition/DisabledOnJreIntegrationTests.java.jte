@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param List<JRE> jresSortedByStringValue
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
 * Integration tests for {@link DisabledOnJre @DisabledOnJre}, generated from
 * {@code DisabledOnJreIntegrationTests.java.jte}.
 *
 * @since 5.1
 */
class DisabledOnJreIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledOnJre({})
	void missingVersionDeclaration() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledOnJre(UNDEFINED)
	void jreUndefined() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledOnJre(versions = 7)
	void version7() {
	}

	@Test
	@DisabledOnJre(disabledReason = "Disabled on every JRE", value = { //
@for(var jre : jres)<%--
--%>			JAVA_${jre.getVersion()}, //
@endfor<%--
--%>			OTHER //
	})
	void disabledOnAllJavaVersions() {
		fail("should be disabled");
	}
@for(var jre : jres)
	@Test
	@DisabledOnJre(JAVA_${jre.getVersion()})
	void jre${jre.getVersion()}() {
		assertFalse(onJava${jre.getVersion()}());
	}
@endfor<%--
--%>@for(var jre : jres)
	@Test
	@DisabledOnJre(versions = ${jre.getVersion()})
	void version${jre.getVersion()}() {
		assertFalse(onJava${jre.getVersion()}());
	}
@endfor
	@Test
	@DisabledOnJre(OTHER)
	void other() {
		assertTrue(onKnownVersion());
	}

}
