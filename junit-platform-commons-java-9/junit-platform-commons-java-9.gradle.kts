plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons - Java 9+ specific implementations"

apply(from = "$rootDir/gradle/testing.gradle.kts")

dependencies {
	implementation(project(":junit-platform-commons")) {
		because ("using types from the base version, e.g. JUnitException and Logger")
	}
}

javaLibrary {
	// Compiles against the public, supported and documented Java 9 API.
	mainJavaVersion = JavaVersion.VERSION_1_9
}
