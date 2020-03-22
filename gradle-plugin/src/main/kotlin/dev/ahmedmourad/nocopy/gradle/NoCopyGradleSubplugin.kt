package dev.ahmedmourad.nocopy.gradle

import dev.ahmedmourad.nocopy.core.PLUGIN_ID
import dev.ahmedmourad.nocopy.core.VERSION
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class NoCopyGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean {
        return project.plugins.hasPlugin(NoCopyGradlePlugin::class.java)
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
            groupId = "dev.ahmedmourad.nocopy",
            artifactId = "nocopy-compiler-plugin",
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

        project.dependencies.add(
                "implementation",
                "dev.ahmedmourad.nocopy:nocopy-annotations:$VERSION"
        )

        return emptyList()
    }
}
