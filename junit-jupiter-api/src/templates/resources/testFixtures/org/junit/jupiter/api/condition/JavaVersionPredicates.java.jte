@import java.util.List
@import gg.jte.support.ForSupport
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

public class JavaVersionPredicates {

	private static final int JAVA_VERSION = Runtime.version().feature();
@for(JRE jre : jres)
	static boolean onJava${jre.getVersion()}() {
		return JAVA_VERSION == ${jre.getVersion()};
	}
@endfor
	static boolean onKnownVersion() {
		return @for(var jre : ForSupport.of(jres))onJava${jre.get().getVersion()}()@if(!jre.isLast()) //
				|| @endif@endfor;
	}
}
