package dev.ahmedmourad.nocopy.gradle

import dev.ahmedmourad.nocopy.core.PLUGIN_NAME
import io.github.classgraph.ClassGraph
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class NoCopyGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.extensions.create(PLUGIN_NAME, NoCopyGradleExtension::class.java)

//        project.afterEvaluate { p ->
//            p.tasks.withType(KotlinCompile::class.java).configureEach {
//                it.kotlinOptions.freeCompilerArgs += "-Xplugin=${classpathOf("compiler-plugin:$VERSION")}"
//            }
//        }
//
//        project.tasks.register("installNoCopyIdeaPlugin", InstallIdeaPlugin::class.java)
//
//        when {
//            inIdea() && pluginsDirExists() && !ideaPluginExists() -> {
//                println("NoCopy IDEA Plugin is not installed!")
//                println("Run 'installNoCopyIdeaPlugin' Gradle task from Intellij IDEA to install it.")
//            }
//        }
    }
}

private fun classpathOf(dependency: String): File {
    val regex = Regex(".*${dependency.replace(':', '-')}.*")
    return ClassGraph().classpathFiles.first { classpath -> classpath.name.matches(regex) }
}

open class NoCopyGradleExtension
