package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.impl.ExtensionPointImpl
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class NoCopyPlugin : ComponentRegistrar {

    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {

        val messageCollector = configuration.get(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                MessageCollector.NONE
        )

        SyntheticResolveExtension.registerExtensionAsFirst(
                project,
                NoCopySyntheticResolveExtension(messageCollector)
        )
    }
}

private fun <T : Any> ProjectExtensionDescriptor<T>.registerExtensionAsFirst(project: Project, extension: T) {
    project.extensionArea
            .getExtensionPoint(extensionPointName)
            .let { it as ExtensionPointImpl }
            .registerExtension(extension, project)
}
