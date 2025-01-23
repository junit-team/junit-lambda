import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	java
}

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

val platformVersion: String by project
val vintageVersion: String by project

dependencies {
	val junit4Version = System.getProperty("junit4Version", "4.12")
	testImplementation("junit:junit:$junit4Version")

	testImplementation("org.junit.vintage:junit-vintage-engine:$vintageVersion") {
		exclude(group = "junit")
		because("we want to override it to test against different versions")
	}

	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$platformVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED)
		afterSuite(KotlinClosure2<TestDescriptor, TestResult, Any>({ _, result ->
			result.exception?.printStackTrace(System.out)
		}))
	}
}
