package junitbuild.generator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.FileOutput
import gg.jte.resolve.ResourceCodeResolver
import io.github.classgraph.ClassGraph
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateJreRelatedSourceCode : DefaultTask() {

    @get:Input
    abstract val templateResourceDir: Property<String>

    @get:OutputDirectory
    abstract val targetDir: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val licenseHeaderFile: RegularFileProperty

    // Generate java file based on YAML file
    @TaskAction
    fun generateSourceCode() {
        val mainTargetDir = targetDir.get().asFile
        mainTargetDir.deleteRecursively()

        val codeResolver = ResourceCodeResolver(templateResourceDir.get(), javaClass.classLoader)
        val templateEngine =
            TemplateEngine.create(codeResolver, temporaryDir.toPath(), ContentType.Plain, javaClass.classLoader)

        ClassGraph()
            .overrideClassLoaders(javaClass.classLoader)
            .ignoreParentClassLoaders()
            .acceptPaths(templateResourceDir.get())
            .scan()
            .use { result ->
                val templates = result.getResourcesWithExtension("jte")
                if (templates.isNotEmpty()) {
                    val params = mapOf(
                        "jres" to javaClass.getResourceAsStream("/jre.yaml").use { input ->
                            val mapper = ObjectMapper(YAMLFactory())
                            mapper.registerModule(KotlinModule.Builder().build())
                            mapper.readValue(input, object : TypeReference<List<JRE>>() {})
                        },
                        "licenseHeader" to licenseHeaderFile.asFile.get().readText()
                    )
                    templates.forEach {
                        val relativeResourcePath = it.path.removePrefix("${templateResourceDir.get()}/")
                        val targetFile = mainTargetDir.toPath().resolve(relativeResourcePath.removeSuffix(".jte"))

                        FileOutput(targetFile).use { output ->
                            templateEngine.render(relativeResourcePath, params, output)
                        }
                    }
                }
            }
    }

}
