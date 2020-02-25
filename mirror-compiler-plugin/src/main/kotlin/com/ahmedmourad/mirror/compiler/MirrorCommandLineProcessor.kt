package com.ahmedmourad.mirror.compiler

import com.ahmedmourad.mirror.core.OPTION_MIRROR_ANNOTATION
import com.ahmedmourad.mirror.core.OPTION_SHATTER_ANNOTATION
import com.ahmedmourad.mirror.core.OPTION_STRATEGY
import com.ahmedmourad.mirror.core.PLUGIN_ID
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_MIRROR_ANNOTATION = CompilerConfigurationKey<String>(OPTION_MIRROR_ANNOTATION)
internal val KEY_SHATTER_ANNOTATION = CompilerConfigurationKey<String>(OPTION_SHATTER_ANNOTATION)
internal val KEY_RESOLUTION = CompilerConfigurationKey<String>(OPTION_STRATEGY)

@AutoService(CommandLineProcessor::class)
class MirrorCommandLineProcessor : CommandLineProcessor {
    /**
     * Just needs to be consistent with the key for MirrorGradleSubplugin#getCompilerPluginId
     */
    override val pluginId: String = PLUGIN_ID

    /**
     * Should match up with the options we return from our MirrorGradleSubplugin.
     * Should also have matching when branches for each name in the [processOption] function below
     */
    override val pluginOptions: Collection<CliOption> = listOf(
            CliOption(
                    optionName = OPTION_MIRROR_ANNOTATION,
                    valueDescription = "String",
                    description = "fully qualified name of the annotation to use instead of @Mirror",
                    required = true
            ), CliOption(
            optionName = OPTION_SHATTER_ANNOTATION,
            valueDescription = "String",
            description = "fully qualified name of the annotation to use instead of @Shatter",
            required = true
    ), CliOption(
            optionName = OPTION_STRATEGY,
            valueDescription = "String",
            description = "Plugin behaviour",
            required = true
    )
    )

    override fun processOption(
            option: AbstractCliOption,
            value: String,
            configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            OPTION_MIRROR_ANNOTATION -> configuration.put(KEY_MIRROR_ANNOTATION, value)
            OPTION_SHATTER_ANNOTATION -> configuration.put(KEY_SHATTER_ANNOTATION, value)
            OPTION_STRATEGY -> configuration.put(KEY_RESOLUTION, value)
            else -> error("Unexpected plugin option: ${option.optionName}")
        }
    }
}
