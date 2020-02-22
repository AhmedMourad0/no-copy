package com.ahmedmourad.mirror.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val DEFAULT_MIRROR_ANNOTATION = "com.ahmedmourad.mirror.annotations.Mirror"
internal const val DEFAULT_SHATTER_ANNOTATION = "com.ahmedmourad.mirror.annotations.Shatter"

class MirrorGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("mirror", MirrorGradleExtension::class.java)
    }
}

open class MirrorGradleExtension {
    /** FQ name of annotation that should count as mirror annotations */
    var mirrorAnnotation: String = DEFAULT_MIRROR_ANNOTATION
    /** FQ name of annotation that should count as shatter annotations */
    var shatterAnnotation: String = DEFAULT_SHATTER_ANNOTATION
}

