import aQute.bnd.gradle.BundleTaskConvention
import aQute.bnd.gradle.FileSetRepositoryConvention
import aQute.bnd.gradle.Resolve

plugins {
	`java-library`
}

// Because the current build version of apiguardian (1.1.1) doesn't have OSGi metadata, bnd
// cannot automatically determine the correct version to use in the Import-Package statement.
// Fetch the current version out of the libs.versions.toml file.
val apiguardianVersion = project.extensions.getByType(VersionCatalogsExtension::class).named("libs").findVersion("apiguardian").get()
val importAPIGuardian = "org.apiguardian.*;version=\"\${range;[==,+);${apiguardianVersion}}\";resolution:=\"optional\""

// This task enhances `jar` and `shadowJar` tasks with the bnd
// `BundleTaskConvention` convention which allows for generating OSGi
// metadata into the jar
tasks.withType<Jar>().matching {
	task: Jar -> task.name == "jar" || task.name == "shadowJar"
}.configureEach {
	val btc = BundleTaskConvention(this)

	extra["importAPIGuardian"] = importAPIGuardian

	// These are bnd instructions necessary for generating OSGi metadata.
	// We've generalized these so that they are widely applicable limiting
	// module configurations to special cases.
	btc.setBnd("""
			# Set the Bundle-SymbolicName to the archiveBaseName.
			# We don't use the archiveClassifier which Bnd will use
			# in the default Bundle-SymbolicName value.
			Bundle-SymbolicName: ${'$'}{task.archiveBaseName}

			# Set the Bundle-Name from the project description
			Bundle-Name: ${'$'}{project.description}

			# These are the general rules for package imports.
			Import-Package: \
				${importAPIGuardian},\
				org.junit.platform.commons.logging;status=INTERNAL,\
				kotlin.*;resolution:="optional",\
				*

			# This tells bnd not to complain if a module doesn't actually import
			# the kotlin and apiguardian packages, but enough modules do to make it a default.
			-fixupmessages.kotlin.import: "Unused Import-Package instructions: \\[kotlin.*\\]";is:=ignore
			-fixupmessages.apiguardian.import: "Unused Import-Package instructions: \\[org.apiguardian.*\\]";is:=ignore

			# This tells bnd to ignore classes it finds in `META-INF/versions/`
			# because bnd doesn't yet support multi-release jars.
			-fixupmessages.wrong.dir: "Classes found in the wrong directory: \\{META-INF/versions/...";is:=ignore

			# Don't scan for Class.forName package imports.
			# See https://bnd.bndtools.org/instructions/noclassforname.html
			-noclassforname: true

			# Don't add all the extra headers bnd normally adds.
			# See https://bnd.bndtools.org/instructions/noextraheaders.html
			-noextraheaders: true

			# Don't add the Private-Package header.
			# See https://bnd.bndtools.org/instructions/removeheaders.html
			-removeheaders: Private-Package

			# Instruct the APIGuardianAnnotations how to operate.
			# See https://bnd.bndtools.org/instructions/export-apiguardian.html
			-export-apiguardian: *;version=${'$'}{versionmask;===;${'$'}{version_cleanup;${'$'}{task.archiveVersion}}}
		""")

	// Add the convention to the jar task
	@Suppress("deprecation") // https://github.com/bndtools/bnd/issues/4699
	convention.plugins["bundle"] = btc

	doLast {
		// Do the actual work putting OSGi stuff in the jar.
		btc.buildBundle()
	}
}

val osgiPropertiesFile = file("$buildDir/verifyOSGiProperties.bndrun")

// Bnd's Resolve task uses a properties file for its configuration. This
// task writes out the properties necessary for it to verify the OSGi
// metadata.
val osgiProperties by tasks.registering(WriteProperties::class) {
	outputFile = osgiPropertiesFile
	property("-standalone", true)
	project.extensions.getByType(JavaLibraryExtension::class.java).let { javaLibrary ->
		property("-runee", "JavaSE-${javaLibrary.mainJavaVersion}")
	}
	property("-runrequires", "osgi.identity;filter:='(osgi.identity=${project.name})'")
	property("-runsystempackages", "jdk.internal.misc,jdk.jfr,sun.misc")
	// API Guardian should be optional -> instruct resolver to ignore it
	// during resolution. Resolve should still pass.
	property("-runblacklist", "org.apiguardian.api")
}

val osgiVerification by configurations.creating {
	extendsFrom(configurations.runtimeClasspath.get())
}

// Bnd's Resolve task is what verifies that a jar can be used in OSGi and
// that its metadata is valid. If the metadata is invalid this task will
// fail.
val verifyOSGi by tasks.registering(Resolve::class) {
	dependsOn(osgiProperties)
	setBndrun(osgiPropertiesFile)
	isReportOptional = false
	@Suppress("deprecation") // https://github.com/bndtools/bnd/issues/4699
	withConvention(FileSetRepositoryConvention::class) {

		// By default bnd will use jars found in:
		// 1. project.sourceSets.main.runtimeClasspath
		// 2. project.configurations.archives.artifacts.files
		// to validate the metadata.
		// This adds jars defined in `osgiVerification` also so that bnd
		// can use them to validate the metadata without causing those to
		// end up in the dependencies of those projects.
		bundles(osgiVerification)
	}
}

tasks.check {
	dependsOn(verifyOSGi)
}
