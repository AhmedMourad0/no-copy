package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.extensions.impl.ExtensionPointImpl
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

//TODO: use message collector instead of error
class NoCopyPlugin : ComponentRegistrar {

    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {
        SyntheticResolveExtension.registerExtensionAsFirst(
                project,
                NoCopySyntheticResolveExtension()
        )
    }
}

private fun <T> ProjectExtensionDescriptor<T>.registerExtensionAsFirst(project: Project, extension: T) {
    Extensions.getArea(project)
            .getExtensionPoint(extensionPointName)
            .let { it as ExtensionPointImpl }
            .registerExtension(extension, project)
}
