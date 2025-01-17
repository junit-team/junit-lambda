plugins {
	id("junitbuild.java-library-conventions")
}

description = "JUnit Platform Flight Recorder Support"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	if (java.toolchain.implementation.orNull == JvmImplementation.J9) {
		compileOnly(libs.jfrPolyfill) {
			because("OpenJ9 does not include JFR")
		}
	}

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	compileModule {
		options.release = 11
	}
}
