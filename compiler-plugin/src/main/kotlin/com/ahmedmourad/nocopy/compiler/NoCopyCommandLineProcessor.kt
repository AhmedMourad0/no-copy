package com.ahmedmourad.nocopy.compiler

import com.ahmedmourad.nocopy.core.PLUGIN_ID
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class NoCopyCommandLineProcessor : CommandLineProcessor {

    /**
     * Just needs to be consistent with the key for NoCopyGradleSubplugin#getCompilerPluginId
     */
    override val pluginId: String = PLUGIN_ID

    /**
     * Should match up with the options we return from our NoCopyGradleSubplugin.
     * Should also have matching when branches for each name in the [processOption] function below
     */
    override val pluginOptions: Collection<CliOption> = emptyList()

    override fun processOption(
            option: AbstractCliOption,
            value: String,
            configuration: CompilerConfiguration
    ) {

    }
}
