package com.ahmedmourad.mirror.gradle

import com.ahmedmourad.mirror.core.PLUGIN_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project

class MirrorGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(PLUGIN_NAME, MirrorGradleExtension::class.java)
    }
}

open class MirrorGradleExtension
