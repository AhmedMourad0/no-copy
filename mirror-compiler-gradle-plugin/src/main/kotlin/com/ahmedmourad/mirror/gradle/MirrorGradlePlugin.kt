package com.ahmedmourad.mirror.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val DEFAULT_MIRROR_ANNOTATION = "com.ahmedmourad.mirror.annotations.Mirror"
internal const val DEFAULT_SHATTER_ANNOTATION = "com.ahmedmourad.mirror.annotations.Shatter"
internal val DEFAULT_RESOLUTION = Resolution.BY_ANNOTATION

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
    var resolution: Resolution = DEFAULT_RESOLUTION
}

enum class Resolution {
    /** The plugin will only mirror or shatter `copy` of the data classes marked with specified annotations */
    BY_ANNOTATION,
    /** The plugin will shatter all `copy` methods of all data classes (no annotations needed) */
    SHATTER_ALL,
    /** The plugin will mirror the least visible constructor for all copy methods of all data classes (no annotations needed) */
    MIRROR_ALL_BY_PRIMARY,
    /** The plugin will mirror the primary constructor for all copy methods of all data classes (no annotations needed) */
    MIRROR_ALL_BY_LEAST_VISIBLE
}
