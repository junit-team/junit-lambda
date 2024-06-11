import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val rootTargetDir = layout.buildDirectory.dir("generated/sources/jte")

val generateCode by tasks.registering

sourceSets.configureEach {

    val sourceSetName = name
    val sourceSetTargetDir = rootTargetDir.map { it.dir(sourceSetName) }

    val task =
        tasks.register(getTaskName("generateJreRelated", "SourceCode"), GenerateJreRelatedSourceCode::class) {
            templateResourceDir.convention("jre-templates/${project.name}/${sourceSetName}")
            targetDir.convention(sourceSetTargetDir)
            licenseHeaderFile.convention(rootProject.layout.projectDirectory.file("gradle/config/spotless/eclipse-public-license-2.0.java"))
        }

    java.srcDir(files(sourceSetTargetDir).builtBy(task))

    generateCode {
        dependsOn(task)
    }
}
