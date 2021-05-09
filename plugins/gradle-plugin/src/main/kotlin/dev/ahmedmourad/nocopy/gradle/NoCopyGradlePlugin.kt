package dev.ahmedmourad.nocopy.gradle

import dev.ahmedmourad.nocopy.core.PLUGIN_ID
import dev.ahmedmourad.nocopy.core.PLUGIN_NAME
import dev.ahmedmourad.nocopy.core.VERSION
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class NoCopyGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target.project.plugins.hasPlugin(NoCopyGradlePlugin::class.java)
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "dev.ahmedmourad.nocopy",
        artifactId = "nocopy-compiler-plugin",
        version = VERSION
    )

    override fun apply(target: Project) {
        target.extensions.create(PLUGIN_NAME, NoCopyGradleExtension::class.java)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {

        val project = kotlinCompilation.target.project

        project.dependencies.add(
            "implementation",
            "dev.ahmedmourad.nocopy:nocopy-annotations:$VERSION"
        )

        return project.provider { emptyList() }
    }
}

open class NoCopyGradleExtension
