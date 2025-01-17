plugins {
	java
}

val jupiterVersion: String by project
val platformVersion: String by project

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion") {
		exclude(group = "org.junit.jupiter", module = "junit-jupiter-engine")
	}
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$platformVersion")}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.test {
	useJUnitPlatform()
}
