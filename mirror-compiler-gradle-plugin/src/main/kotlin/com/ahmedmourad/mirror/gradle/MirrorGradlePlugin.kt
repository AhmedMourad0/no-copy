package com.ahmedmourad.mirror.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val DEFAULT_ANNOTATION = "com.ahmedmourad.mirror.annotations.Mirror"

class MirrorGradlePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.create("mirror", MirrorGradleExtension::class.java)
  }
}

open class MirrorGradleExtension {
    /** FQ names of annotations that should count as mirror annotations */
    var annotation: String = DEFAULT_ANNOTATION
}

