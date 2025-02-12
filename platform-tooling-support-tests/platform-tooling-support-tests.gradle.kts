
import com.gradle.develocity.agent.gradle.internal.test.TestDistributionConfigurationInternal
import junitbuild.extensions.capitalized
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.jvm.toolchain.internal.NoToolchainAvailableException
import org.gradle.kotlin.dsl.support.listFilesOrdered
import java.time.Duration

plugins {
	id("junitbuild.build-parameters")
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.testing-conventions")
}

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_21
}

spotless {
	java {
		target(files(project.java.sourceSets.map { it.allJava }), "projects/**/*.java")
	}
	kotlin {
		target("projects/**/*.kt")
	}
	format("projects") {
		target("projects/**/*.gradle.kts", "projects/**/*.md")
		trimTrailingWhitespace()
		endWithNewline()
	}
}

val thirdPartyJars = configurations.dependencyScope("thirdPartyJars")
val thirdPartyJarsClasspath = configurations.resolvable("thirdPartyJarsClasspath") {
	extendsFrom(thirdPartyJars.get())
}
val antJars = configurations.dependencyScope("antJars")
val antJarsClasspath = configurations.resolvable("antJarsClasspath") {
	extendsFrom(antJars.get())
}
val mavenDistribution = configurations.dependencyScope("mavenDistribution")
val mavenDistributionClasspath = configurations.resolvable("mavenDistributionClasspath") {
	extendsFrom(mavenDistribution.get())
}

dependencies {
	implementation(libs.commons.io) {
		because("moving/deleting directory trees")
	}
	implementation(projects.platformTests) {
		capabilities {
			requireFeature("process-starter")
		}
	}
	implementation(projects.junitJupiterApi) {
		because("it uses the OS enum to support Windows")
	}

	testImplementation(libs.archunit) {
		because("checking the architecture of JUnit 5")
	}
	testImplementation(libs.apiguardian) {
		because("we validate that public classes are annotated")
	}
	testImplementation(libs.bndlib) {
		because("parsing OSGi metadata")
	}
	testRuntimeOnly(libs.slf4j.julBinding) {
		because("provide appropriate SLF4J binding")
	}
	testImplementation(libs.ant) {
		because("we reference Ant's main class")
	}
	testImplementation(libs.bundles.xmlunit)
	testImplementation(testFixtures(projects.junitJupiterApi))
	testImplementation(testFixtures(projects.junitPlatformReporting))
	testImplementation(libs.snapshotTests.junit5)
	testImplementation(libs.snapshotTests.xml)

	thirdPartyJars(libs.junit4)
	thirdPartyJars(libs.assertj)
	thirdPartyJars(libs.apiguardian)
	thirdPartyJars(libs.hamcrest)
	thirdPartyJars(libs.opentest4j)
	thirdPartyJars(libs.openTestReporting.tooling.spi)
	thirdPartyJars(libs.jimfs)

	antJars(platform(projects.junitBom))
	antJars(libs.bundles.ant)
	antJars(projects.junitPlatformConsoleStandalone)
	antJars(projects.junitPlatformLauncher)
	antJars(projects.junitPlatformReporting)

	mavenDistribution(libs.maven) {
		artifact {
			classifier = "bin"
			type = "zip"
			isTransitive = false
		}
	}
}

val mavenDistributionDir = layout.buildDirectory.dir("maven-distribution")

val unzipMavenDistribution by tasks.registering(Sync::class) {
	from(zipTree(mavenDistributionClasspath.flatMap { d -> d.elements.map { e -> e.single() } }))
	into(mavenDistributionDir)
}

val normalizeMavenRepo by tasks.registering(Sync::class) {

	val mavenizedProjects: List<Project> by rootProject
	val tempRepoDir: File by rootProject
	val tempRepoName: String by rootProject

	// All maven-aware projects must be published to the local temp repository
	(mavenizedProjects + dependencyProject(projects.junitBom))
		.map { project -> project.tasks.named("publishAllPublicationsTo${tempRepoName.capitalized()}Repository") }
		.forEach { dependsOn(it) }

	from(tempRepoDir) {
		exclude("**/maven-metadata.xml*")
		exclude("**/*.md5")
		exclude("**/*.sha*")
		exclude("**/*.module")
	}
	from(tempRepoDir) {
		include("**/*.module")
		val regex = "\"(sha\\d+|md5|size)\": (?:\".+\"|\\d+)(,)?".toRegex()
		filter { line -> regex.replace(line, "\"normalized-$1\": \"normalized-value\"$2") }
	}
	rename("(.*\\W)\\d{8}\\.\\d{6}-\\d+(\\W.*)", "$1SNAPSHOT$2")
	into(layout.buildDirectory.dir("normalized-repo"))
}

tasks.test {
	// Opt-out via system property: '-Dplatform.tooling.support.tests.enabled=false'
	enabled = System.getProperty("platform.tooling.support.tests.enabled")?.toBoolean() ?: true

	// The following if-block is necessary since Gradle will otherwise
	// always publish all mavenizedProjects even if this "test" task
	// is not executed.
	if (enabled) {
		dependsOn(normalizeMavenRepo)
		jvmArgumentProviders += MavenRepo(project, normalizeMavenRepo.map { it.destinationDir })
	}
	environment.remove("JAVA_TOOL_OPTIONS")

	jvmArgumentProviders += JarPath(project, thirdPartyJarsClasspath.get(), "thirdPartyJars")
	jvmArgumentProviders += JarPath(project, antJarsClasspath.get(), "antJars")
	jvmArgumentProviders += MavenDistribution(project, unzipMavenDistribution, mavenDistributionDir)

	if (buildParameters.javaToolchain.version.getOrElse(21) < 24) {
		(options as JUnitPlatformOptions).apply {
			includeEngines("archunit")
		}
	}

	inputs.apply {
		dir("projects").withPathSensitivity(RELATIVE)
		file("${rootDir}/gradle.properties").withPathSensitivity(RELATIVE)
		file("${rootDir}/settings.gradle.kts").withPathSensitivity(RELATIVE)
		file("${rootDir}/gradlew").withPathSensitivity(RELATIVE)
		file("${rootDir}/gradlew.bat").withPathSensitivity(RELATIVE)
		dir("${rootDir}/gradle/wrapper").withPathSensitivity(RELATIVE)
		dir("${rootDir}/documentation/src/main").withPathSensitivity(RELATIVE)
		dir("${rootDir}/documentation/src/test").withPathSensitivity(RELATIVE)
	}

	// Disable capturing output since parallel execution is enabled and output of
	// external processes happens on non-test threads which can't reliably be
	// attributed to the test that started the process.
	systemProperty("junit.platform.output.capture.stdout", "false")
	systemProperty("junit.platform.output.capture.stderr", "false")

	develocity {
		testDistribution {
			requirements.add("jdk=8")
			this as TestDistributionConfigurationInternal
			preferredMaxDuration = Duration.ofMillis(500)
		}
	}
	jvmArgumentProviders += JavaHomeDir(project, 8, develocity.testDistribution.enabled)

	val gradleJavaVersion = JavaVersion.current().majorVersion.toInt()
	jvmArgumentProviders += JavaHomeDir(project, gradleJavaVersion, develocity.testDistribution.enabled)
	systemProperty("gradle.java.version", gradleJavaVersion)
}

class MavenRepo(project: Project, @get:Internal val repoDir: Provider<File>) : CommandLineArgumentProvider {

	// Track jars and non-jars separately to benefit from runtime classpath normalization
	// which ignores timestamp manifest attributes.

	@InputFiles
	@Classpath
	val jarFiles: ConfigurableFileTree = project.fileTree(repoDir) {
		include("**/*.jar")
	}

	@InputFiles
	@PathSensitive(RELATIVE)
	val nonJarFiles: ConfigurableFileTree = project.fileTree(repoDir) {
		exclude("**/*.jar")
	}

	override fun asArguments() = listOf("-Dmaven.repo=${repoDir.get().absolutePath}")
}

class JavaHomeDir(project: Project, @Input val version: Int, testDistributionEnabled: Provider<Boolean>) : CommandLineArgumentProvider {

	@Internal
	val javaLauncher: Property<JavaLauncher> = project.objects.property<JavaLauncher>()
			.value(project.provider {
				try {
					project.javaToolchains.launcherFor {
						languageVersion = JavaLanguageVersion.of(version)
					}.get()
				} catch (e: NoToolchainAvailableException) {
					null
				}
			})

	@Internal
	val enabled: Property<Boolean> = project.objects.property<Boolean>().convention(testDistributionEnabled.map { !it })

	override fun asArguments(): List<String> {
		if (!enabled.get()) {
			return emptyList()
		}
		val metadata = javaLauncher.map { it.metadata }
		val javaHome = metadata.map { it.installationPath.asFile.absolutePath }.orNull
		return javaHome?.let { listOf("-Djava.home.$version=$it") } ?: emptyList()
	}
}

class JarPath(project: Project, configuration: Configuration, @Input val key: String = configuration.name) : CommandLineArgumentProvider {
	@get:Classpath
	val files: ConfigurableFileCollection = project.objects.fileCollection().from(configuration)

	override fun asArguments() = listOf("-D${key}=${files.asPath}")
}

class MavenDistribution(project: Project, sourceTask: TaskProvider<*>, distributionDir: Provider<Directory>) : CommandLineArgumentProvider {
	@InputDirectory
	@PathSensitive(RELATIVE)
	val mavenDistribution: DirectoryProperty = project.objects.directoryProperty()
		.fileProvider(project.files(distributionDir).builtBy(sourceTask).elements.map { it.single().asFile.listFilesOrdered().single() })

	override fun asArguments() = listOf("-DmavenDistribution=${mavenDistribution.get().asFile.absolutePath}")
}
