package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@OptIn(ExperimentalCompilerApi::class)
class NoCopyPlugin : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false
    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        NoCopySyntheticResolveExtension(messageCollector)
        SyntheticResolveExtension.registerExtension(NoCopySyntheticResolveExtension(messageCollector))
    }
}
