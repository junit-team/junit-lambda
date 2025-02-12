@import java.util.List
@import gg.jte.support.ForSupport
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.StringUtils;

/**
 * Enumeration of Java Runtime Environment (JRE) versions.
 *
 * <p>If the current JRE version can be detected but is not one of the predefined
 * constants in this enum, {@link #OTHER} will be considered to be the
 * {@linkplain #isCurrentVersion current JRE version}. If the current JRE version
 * cannot be detected &mdash; for example, if the {@code java.version} JVM system
 * property is undefined &mdash; {@link #UNDEFINED} will be considered to be the
 * current JRE version.
 *
 * @since 5.1
@for(JRE jre : jres)<%--
--%> * @see #JAVA_${jre.getVersion()}
@endfor<%--
--%> * @see #OTHER
 * @see EnabledOnJre
 * @see DisabledOnJre
 * @see EnabledForJreRange
 * @see DisabledForJreRange
 */
@API(status = STABLE, since = "5.1")
public enum JRE {

	/**
	 * An undefined JRE version.
	 *
	 * <p>This constant is used by JUnit as a default configuration value but is
	 * not intended to be used by users.
	 *
	 * <p>This constant returns {@code -1} for its {@linkplain #version() version}.
	 *
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	UNDEFINED(-1),
@for(var jre : jres)
	/**
	 * Java ${jre.getVersion()}.
	@if(jre.getSince() != null)<%--
--%> *
	 * @since ${jre.getSince()}
	@endif<%--
--%> */
	@if(jre.getSince() != null)<%--
--%>@API(status = STABLE, since = "${jre.getSince()}")
	@endif<%--
--%>JAVA_${jre.getVersion()}(${jre.getVersion()}),
@endfor
	/**
	 * A JRE version other than <%--
--%>@for(var jre : ForSupport.of(jres))<%--
	--%>@if(jre.isLast())or @endif<%--
	--%>{@link #JAVA_${jre.get().getVersion()}}<%--
	--%>@if(jre.isLast()).@else,@endif<%--
	--%>@if(jre.getIndex() % 3 == 1 && !jre.isLast())
	 * @elseif(!jre.isLast()) @endif<%--
--%>@endfor
	 *
	 * <p>This constant returns {@link Integer#MAX_VALUE} for its
	 * {@linkplain #version() version}. To retrieve the actual version number,
	 * use {@link #currentVersionNumber()}.
	 */
	OTHER(Integer.MAX_VALUE);

	static final int UNDEFINED_VERSION = -1;

	static final int MINIMUM_VERSION = 8;

	private static final Logger logger = LoggerFactory.getLogger(JRE.class);

	private static final int CURRENT_VERSION = determineCurrentVersion();

	private static final JRE CURRENT_JRE = determineCurrentJre(CURRENT_VERSION);

	private static int determineCurrentVersion() {
		String javaVersion = System.getProperty("java.version");
		boolean javaVersionIsBlank = StringUtils.isBlank(javaVersion);

		if (javaVersionIsBlank) {
			logger.debug(
				() -> "JVM system property 'java.version' is undefined. It is therefore not possible to detect Java 8.");
		}

		if (!javaVersionIsBlank && javaVersion.startsWith("1.8")) {
			return 8;
		}

		try {
			// java.lang.Runtime.version() is a static method available on Java 9+
			// that returns an instance of java.lang.Runtime.Version which has the
			// following method: public int major()
			Method versionMethod = Runtime.class.getMethod("version");
			Object version = ReflectionSupport.invokeMethod(versionMethod, null);
			Method majorMethod = version.getClass().getMethod("major");
			return (int) ReflectionSupport.invokeMethod(majorMethod, version);
		}
		catch (Exception ex) {
			logger.debug(ex, () -> "Failed to determine the current JRE version via java.lang.Runtime.Version.");
		}

		return UNDEFINED_VERSION;
	}

	private static JRE determineCurrentJre(int currentVersion) {
		switch (currentVersion) {
			case UNDEFINED_VERSION:
				return UNDEFINED;<%--
			--%>@for(var jre : jres)
			case ${jre.getVersion()}:
				return JAVA_${jre.getVersion()};<%--
			--%>@endfor
			default:
				return OTHER;
		}
	}

	private final int version;

	private JRE(int version) {
		this.version = version;
	}

	/**
	 * Get the version of <em>this</em> {@code JRE}.
	 *
	 * <p>If this {@code JRE} is {@link #UNDEFINED}, this method returns
	 * {@code -1}. If this {@code JRE} is {@link #OTHER}, this method returns
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @return the version of this {@code JRE}
	 * @since 5.12
	 * @see Runtime.Version#feature()
	 * @see #currentVersionNumber()
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public int version() {
		return this.version;
	}

	/**
	 * @return {@code true} if <em>this</em> {@code JRE} is known to be the
	 * Java Runtime Environment version for the currently executing JVM or if
	 * the version is {@link #OTHER} or {@link #UNDEFINED}
	 *
	 * @see #currentJre()
	 * @see #currentVersionNumber()
	 */
	public boolean isCurrentVersion() {
		return this == CURRENT_JRE;
	}

	/**
	 * @return the {@link JRE} for the currently executing JVM, potentially
	 * {@link #OTHER} or {@link #UNDEFINED}
	 *
	 * @since 5.7
	 * @see #currentVersionNumber()
	 * @deprecated in favor of {@link #currentJre()}
	 */
	@API(status = DEPRECATED, since = "5.12")
	@Deprecated
	public static JRE currentVersion() {
		return currentJre();
	}

	/**
	 * @return the {@link JRE} for the currently executing JVM, potentially
	 * {@link #OTHER} or {@link #UNDEFINED}
	 *
	 * @since 5.12
	 * @see #currentVersionNumber()
	 */
	@API(status = STABLE, since = "5.12")
	public static JRE currentJre() {
		return CURRENT_JRE;
	}

	/**
	 * @return the version number for the currently executing JVM, or {@code -1}
	 * if the current JVM version could not be determined
	 *
	 * @since 5.12
	 * @see Runtime.Version#feature()
	 * @see #currentJre()
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public static int currentVersionNumber() {
		return CURRENT_VERSION;
	}

	/**
	 * @return {@code true} if the supplied version number is known to be the
	 * Java Runtime Environment version for the currently executing JVM or if
	 * the supplied version number is {@code -1} and the current JVM version
	 * could not be determined
	 *
	 * @since 5.12
	 * @see Runtime.Version#feature()
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public static boolean isCurrentVersion(int version) {
		return version == CURRENT_VERSION;
	}

	static boolean isCurrentVersionWithinRange(int min, int max) {
		return CURRENT_VERSION >= min && CURRENT_VERSION <= max;
	}

}
