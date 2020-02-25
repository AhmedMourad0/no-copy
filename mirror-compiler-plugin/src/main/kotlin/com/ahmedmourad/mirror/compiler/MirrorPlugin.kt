package com.ahmedmourad.mirror.compiler

import com.google.auto.service.AutoService
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.extensions.impl.ExtensionPointImpl
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@AutoService(ComponentRegistrar::class)
class MirrorPlugin() : ComponentRegistrar {

    private var testConfiguration: CompilerConfiguration? = null

    // No way to define options yet in compile testing
    // https://github.com/tschuchortdev/kotlin-compile-testing/issues/34
    @TestOnly
    internal constructor(mirrorAnnotation: String) : this() {
        testConfiguration = CompilerConfiguration().apply {
            put(KEY_MIRROR_ANNOTATION, mirrorAnnotation)
        }
    }

    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {

        val actualConfiguration = testConfiguration ?: configuration

        val realMessageCollector = configuration.get(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                MessageCollector.NONE
        )

        val messageCollector = testConfiguration?.get(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                realMessageCollector
        ) ?: realMessageCollector

        val mirrorAnnotation = checkNotNull(actualConfiguration[KEY_MIRROR_ANNOTATION])
        val shatterAnnotation = checkNotNull(actualConfiguration[KEY_SHATTER_ANNOTATION])
        val fqMirrorAnnotation = FqName(mirrorAnnotation)
        val fqShatterAnnotation = FqName(shatterAnnotation)

        SyntheticResolveExtension.registerExtensionAsFirst(
                project,
                MirrorSyntheticResolveExtension(messageCollector, fqMirrorAnnotation, fqShatterAnnotation)
        )
    }
}

fun MessageCollector.log(message: String) {
    this.report(
            CompilerMessageSeverity.LOGGING,
            "*** MIRROR $message",
            CompilerMessageLocation.create(null)
    )
}

private fun <T> ProjectExtensionDescriptor<T>.registerExtensionAsFirst(project: Project, extension: T) {
    Extensions.getArea(project)
            .getExtensionPoint(extensionPointName)
            .let { it as ExtensionPointImpl }
            .registerExtension(extension, project)
}
