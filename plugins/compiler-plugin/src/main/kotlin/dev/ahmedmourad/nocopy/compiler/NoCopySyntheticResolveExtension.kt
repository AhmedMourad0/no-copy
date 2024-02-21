package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

open class NoCopySyntheticResolveExtension(
        private val messageCollector: MessageCollector?
) : SyntheticResolveExtension {

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {

        val hasNoCopy = thisDescriptor.hasNoCopy()

        if (!thisDescriptor.isData) {

            val annotation = when {
                hasNoCopy -> "@NoCopy"
                else -> null
            }

            if (annotation != null) {
                messageCollector?.error(
                        "Only data classes could be annotated with $annotation!",
                        thisDescriptor.findPsi()
                )
            } else {
                super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
            }

            return
        }

        if (!isGeneratedCopyMethod(name)) {
            super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
            return
        }

        val generatedCopyMethodIndex = result.findGeneratedCopyMethodIndex(thisDescriptor)
        if (generatedCopyMethodIndex == null) {
            messageCollector?.error("Cannot find generated copy method!", thisDescriptor.findPsi())
            return
        }

        when {
            hasNoCopy -> handleNoCopy(generatedCopyMethodIndex, result)
        }
    }

    private fun handleNoCopy(
            generatedCopyMethodIndex: Int,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        result.remove(result.elementAt(generatedCopyMethodIndex))
    }
}
