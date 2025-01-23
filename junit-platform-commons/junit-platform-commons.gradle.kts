plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.native-image-properties")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)
}
