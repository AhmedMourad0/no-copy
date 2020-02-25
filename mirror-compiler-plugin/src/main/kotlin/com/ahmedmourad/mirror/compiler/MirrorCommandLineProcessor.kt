package com.ahmedmourad.mirror.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_MIRROR_ANNOTATION = CompilerConfigurationKey<String>("mirrorAnnotation")
internal val KEY_SHATTER_ANNOTATION = CompilerConfigurationKey<String>("shatterAnnotation")
internal val KEY_RESOLUTION = CompilerConfigurationKey<String>("resolution")

@AutoService(CommandLineProcessor::class)
class MirrorCommandLineProcessor : CommandLineProcessor {
    /**
     * Just needs to be consistent with the key for MirrorGradleSubplugin#getCompilerPluginId
     */
    override val pluginId: String = "mirror-compiler-plugin"

    /**
     * Should match up with the options we return from our MirrorGradleSubplugin.
     * Should also have matching when branches for each name in the [processOption] function below
     */
    override val pluginOptions: Collection<CliOption> = listOf(
            CliOption(
                    optionName = "mirrorAnnotation",
                    valueDescription = "String",
                    description = "fully qualified name of the annotation to use instead of @Mirror",
                    required = true
            ), CliOption(
            optionName = "shatterAnnotation",
            valueDescription = "String",
            description = "fully qualified name of the annotation to use instead of @Shatter",
            required = true
    ), CliOption(
            optionName = "resolution",
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
            "mirrorAnnotation" -> configuration.put(KEY_MIRROR_ANNOTATION, value)
            "shatterAnnotation" -> configuration.put(KEY_SHATTER_ANNOTATION, value)
            "resolution" -> configuration.put(KEY_RESOLUTION, value)
            else -> error("Unexpected plugin option: ${option.optionName}")
        }
    }
}
