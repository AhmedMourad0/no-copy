package com.ahmedmourad.mirror.intellij

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import org.jetbrains.kotlin.idea.configuration.GradleProjectImportHandler
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.plugins.gradle.model.data.GradleSourceSetData

class MirrorGradleProjectImportHandler : GradleProjectImportHandler {
    override fun importBySourceSet(facet: KotlinFacet, sourceSetNode: DataNode<GradleSourceSetData>) {
        MirrorImportHandler.modifyCompilerArguments(facet, PLUGIN_GRADLE_JAR)
    }

    override fun importByModule(facet: KotlinFacet, moduleNode: DataNode<ModuleData>) {
        MirrorImportHandler.modifyCompilerArguments(facet, PLUGIN_GRADLE_JAR)
    }

    companion object {
        private const val PLUGIN_GRADLE_JAR = "gradle-plugin"
    }
}
