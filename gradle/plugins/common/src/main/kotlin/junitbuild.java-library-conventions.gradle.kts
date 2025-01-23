import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import junitbuild.java.ModuleCompileOptions
import junitbuild.java.ModulePathArgumentProvider
import junitbuild.java.PatchModuleArgumentProvider
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Library
import org.gradle.plugins.ide.eclipse.model.ProjectDependency

plugins {
	`java-library`
	eclipse
	idea
	id("junitbuild.base-conventions")
	id("junitbuild.build-parameters")
	id("junitbuild.checkstyle-conventions")
	id("junitbuild.jacoco-java-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra
val modularProjects: List<Project> by rootProject.extra
val buildDate: String by rootProject.extra
val buildTime: String by rootProject.extra
val buildRevision: Any by rootProject.extra

val extension = extensions.create<JavaLibraryExtension>("javaLibrary")

val moduleSourceDir = layout.projectDirectory.dir("src/module/$javaModuleName")
val combinedModuleSourceDir = layout.buildDirectory.dir("module")
val moduleOutputDir = layout.buildDirectory.dir("classes/java/module")

eclipse {
	jdt {
		file {
			// Set properties for org.eclipse.jdt.core.prefs
			withProperties {
				// Configure Eclipse projects with -parameters compiler flag.
				setProperty("org.eclipse.jdt.core.compiler.codegen.methodParameters", "generate")
			}
		}
	}
	classpath.file.whenMerged {
		this as Classpath
		// Remove classpath entries for non-existent libraries added by various
		// plugins, such as "junit-jupiter-api/build/classes/kotlin/testFixtures".
		entries.removeIf { it is Library && !file(it.path).exists() }
		// Remove classpath entries for the code generator model used by the
		// Java Template Engine (JTE) which is used to generate the JRE enum and
		// dependent tests.
		entries.removeIf { it is ProjectDependency && it.path.equals("/code-generator-model") }
	}
}

java {
	modularity.inferModulePath = false
}

if (project in mavenizedProjects) {

	apply(plugin = "junitbuild.publishing-conventions")
	apply(plugin = "junitbuild.osgi-conventions")

	java {
		withJavadocJar()
		withSourcesJar()
	}

	tasks.javadoc {
		options {
			memberLevel = JavadocMemberLevel.PROTECTED
			header = project.name
			encoding = "UTF-8"
			locale = "en"
			(this as StandardJavadocDocletOptions).apply {
				addBooleanOption("Xdoclint:all,-missing,-reference", true)
				addBooleanOption("XD-Xlint:none", true)
				addBooleanOption("html5", true)
				addMultilineStringsOption("tag").value = listOf(
						"apiNote:a:API Note:",
						"implNote:a:Implementation Note:"
				)
				use(true)
				noTimestamp(true)
			}
		}
	}

	tasks.named<Jar>("javadocJar").configure {
		from(tasks.javadoc.map { File(it.destinationDir, "element-list") }) {
			// For compatibility with older tools, e.g. NetBeans 11
			rename { "package-list" }
		}
	}

	tasks.named<Jar>("sourcesJar").configure {
		from(moduleSourceDir) {
			include("module-info.java")
		}
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}

	pluginManager.withPlugin("java-test-fixtures") {
		val javaComponent = components["java"] as AdhocComponentWithVariants
		javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
		javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
	}

	configure<PublishingExtension> {
		publications {
			named<MavenPublication>("maven") {
				from(components["java"])
				versionMapping {
					allVariants {
						fromResolutionResult()
					}
				}
				pom {
					description = provider { "Module \"${project.name}\" of JUnit 5." }
				}
			}
		}
	}

	if (!project.version.isSnapshot()) {
		configurations {
			compileClasspath {
				resolutionStrategy.failOnChangingVersions()
			}
			runtimeClasspath {
				resolutionStrategy.failOnChangingVersions()
			}
		}
	}

} else {
	tasks {
		jar {
			enabled = false
		}
		javadoc {
			enabled = false
		}
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	dirPermissions {
		unix("rwxr-xr-x")
	}
	filePermissions {
		unix("rw-r--r--")
	}
}

normalization {
	runtimeClasspath {
		metaInf {
			// Ignore inconsequential JAR manifest attributes such as timestamps and the commit checksum.
			// This is used when checking whether runtime classpaths, e.g. of test tasks, have changed and
			// improves cacheability of such tasks.
			ignoreAttribute("Built-By")
			ignoreAttribute("Build-Date")
			ignoreAttribute("Build-Time")
			ignoreAttribute("Build-Revision")
			ignoreAttribute("Created-By")
		}
	}
}

val allMainClasses by tasks.registering {
	dependsOn(tasks.classes)
}

val prepareModuleSourceDir by tasks.registering(Sync::class) {
	from(moduleSourceDir)
	from(sourceSets.named { it.startsWith("main") }.map { it.allJava })
	into(combinedModuleSourceDir.map { it.dir(javaModuleName) })
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val compileModule by tasks.registering(JavaCompile::class) {
	dependsOn(allMainClasses)
	enabled = project in modularProjects
	source = fileTree(combinedModuleSourceDir).builtBy(prepareModuleSourceDir)
	destinationDirectory = moduleOutputDir
	classpath = files()
	options.release = 17
	options.compilerArgs.addAll(listOf(
			// Suppress warnings for automatic modules: org.apiguardian.api, org.opentest4j
			"-Xlint:all,-requires-automatic,-requires-transitive-automatic",
			"-Werror", // Terminates compilation when warnings occur.
			"--module-version", "${project.version}",
	))

	val moduleOptions = objects.newInstance(ModuleCompileOptions::class)
	extensions.add("moduleOptions", moduleOptions)
	moduleOptions.modulePath.from(configurations.compileClasspath)

	options.compilerArgumentProviders.add(objects.newInstance(ModulePathArgumentProvider::class, project, combinedModuleSourceDir, modularProjects).apply {
		modulePath.from(moduleOptions.modulePath)
	})
	options.compilerArgumentProviders.addAll(modularProjects.map { objects.newInstance(PatchModuleArgumentProvider::class, project, it) })

	modularity.inferModulePath = false

	doFirst {
		options.allCompilerArgs.forEach {
			logger.info(it)
		}
	}
}

tasks.withType<Jar>().configureEach {
	from(rootDir) {
		include("LICENSE.md")
		into("META-INF")
	}
	from(rootDir) {
		include("NOTICE.md")
		rename {
			"LICENSE-notice.md"
		}
		into("META-INF")
	}
	val suffix = archiveClassifier.getOrElse("")
	if (suffix.isBlank() || this is ShadowJar) {
		dependsOn(allMainClasses, compileModule)
		from(moduleOutputDir.map { it.dir(javaModuleName) }) {
			include("module-info.class")
		}
	}
}

tasks.jar {
	manifest {
		attributes(
				"Created-By" to (buildParameters.manifest.createdBy.orNull
					?: "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})"),
				"Built-By" to buildParameters.manifest.builtBy.orElse("JUnit Team"),
				"Build-Date" to buildDate,
				"Build-Time" to buildTime,
				"Build-Revision" to buildRevision,
				"Specification-Title" to project.name,
				"Specification-Version" to (project.version as String).substringBefore('-'),
				"Specification-Vendor" to "junit.org",
				"Implementation-Title" to project.name,
				"Implementation-Version" to project.version,
				"Implementation-Vendor" to "junit.org"
		)
	}
}

tasks.withType<ShadowJar>().configureEach {
	outputs.doNotCacheIf("Shadow jar contains a Manifest with Build-Time") { true }
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.compileJava {
	options.compilerArgs.addAll(listOf(
			"-Xlint:all", // Enables all recommended warnings.
			"-Werror", // Terminates compilation when warnings occur.
			"-parameters", // Generates metadata for reflection on method parameters.
	))
}

tasks.compileTestJava {
	options.compilerArgs.addAll(listOf(
			"-Xlint", // Enables all recommended warnings.
			"-Xlint:-overrides", // Disables "method overrides" warnings.
			"-Werror", // Terminates compilation when warnings occur.
			"-parameters" // Generates metadata for reflection on method parameters.
	))
}

afterEvaluate {
	configurations {
		apiElements {
			attributes {
				attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.majorVersion.toInt())
			}
		}
		runtimeElements {
			attributes {
				attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.majorVersion.toInt())
			}
		}
	}
	tasks {
		compileJava {
			if (extension.configureRelease) {
				options.release = extension.mainJavaVersion.majorVersion.toInt()
			} else {
				sourceCompatibility = extension.mainJavaVersion.majorVersion
				targetCompatibility = extension.mainJavaVersion.majorVersion
			}
		}
		compileTestJava {
			if (extension.configureRelease) {
				options.release = extension.testJavaVersion.majorVersion.toInt()
			} else {
				sourceCompatibility = extension.testJavaVersion.majorVersion
				targetCompatibility = extension.testJavaVersion.majorVersion
			}
		}
	}
	pluginManager.withPlugin("groovy") {
		tasks.named<GroovyCompile>("compileGroovy").configure {
			// Groovy compiler does not support the --release flag.
			sourceCompatibility = extension.mainJavaVersion.majorVersion
			targetCompatibility = extension.mainJavaVersion.majorVersion
		}
		tasks.named<GroovyCompile>("compileTestGroovy").configure {
			// Groovy compiler does not support the --release flag.
			sourceCompatibility = extension.testJavaVersion.majorVersion
			targetCompatibility = extension.testJavaVersion.majorVersion
		}
	}
}

tasks {
	checkstyleMain {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleMain.xml"))
	}
	checkstyleTest {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleTest.xml"))
	}
}

pluginManager.withPlugin("java-test-fixtures") {
	tasks.named<Checkstyle>("checkstyleTestFixtures") {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleTest.xml"))
	}
	tasks.named<JavaCompile>("compileTestFixturesJava") {
		options.release = extension.testJavaVersion.majorVersion.toInt()
	}
}
