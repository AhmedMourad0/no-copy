package dev.ahmedmourad.nocopy.gradle

import dev.ahmedmourad.nocopy.core.PLUGIN_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project

class NoCopyGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(PLUGIN_NAME, NoCopyGradleExtension::class.java)
    }
}

open class NoCopyGradleExtension
