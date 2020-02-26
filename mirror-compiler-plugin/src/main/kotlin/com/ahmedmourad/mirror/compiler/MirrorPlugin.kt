package com.ahmedmourad.mirror.compiler

import com.ahmedmourad.mirror.core.Strategy
import com.google.auto.service.AutoService
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

    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {

        val fqMirrorAnnotation = FqName(checkNotNull(configuration[KEY_MIRROR_ANNOTATION]))
        val fqShatterAnnotation = FqName(checkNotNull(configuration[KEY_SHATTER_ANNOTATION]))
        val strategy = Strategy.valueOf(checkNotNull(configuration[KEY_RESOLUTION]))

        SyntheticResolveExtension.registerExtensionAsFirst(
                project,
                MirrorSyntheticResolveExtension(fqMirrorAnnotation, fqShatterAnnotation, strategy)
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
