package com.ahmedmourad.mirror.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinGradleSubplugin::class)
class MirrorGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean {
        return project.plugins.hasPlugin(MirrorGradlePlugin::class.java)
    }

    /**
     * Just needs to be consistent with the key for MirrorCommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "mirror-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.ahmedmourad.mirror",
        artifactId = "mirror-compiler-plugin",
        version = VERSION
    )

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {

        val extension = project.extensions.findByType(MirrorGradleExtension::class.java) ?: MirrorGradleExtension()
        val mirrorAnnotation = extension.mirrorAnnotation
        val shatterAnnotation = extension.shatterAnnotation

        if (mirrorAnnotation == DEFAULT_MIRROR_ANNOTATION || shatterAnnotation == DEFAULT_SHATTER_ANNOTATION) {
            project.dependencies.add("implementation", "com.ahmedmourad.mirror:mirror-compiler-plugin-annotations:$VERSION")
        }

        return listOf(
                SubpluginOption(key = "mirrorAnnotation", value = mirrorAnnotation),
                SubpluginOption(key = "shatterAnnotation", value = shatterAnnotation)
        )
    }
}
