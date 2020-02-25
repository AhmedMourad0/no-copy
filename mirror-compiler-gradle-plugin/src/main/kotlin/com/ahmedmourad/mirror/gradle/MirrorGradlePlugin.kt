package com.ahmedmourad.mirror.gradle

import com.ahmedmourad.mirror.core.Strategy
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val DEFAULT_MIRROR_ANNOTATION = "com.ahmedmourad.mirror.annotations.Mirror"
internal const val DEFAULT_SHATTER_ANNOTATION = "com.ahmedmourad.mirror.annotations.Shatter"
internal val DEFAULT_RESOLUTION = Strategy.BY_ANNOTATIONS

class MirrorGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("mirror", MirrorGradleExtension::class.java)
    }
}

open class MirrorGradleExtension {
    /** FQ name of annotation that should count as mirror annotation */
    var mirrorAnnotation: String = DEFAULT_MIRROR_ANNOTATION
    /** FQ name of annotation that should count as shatter annotation */
    var shatterAnnotation: String = DEFAULT_SHATTER_ANNOTATION
    /** Plugin behaviour */
    var strategy: Strategy = DEFAULT_RESOLUTION
}
