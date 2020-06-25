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

        if (!isGeneratedCopyMethod(thisDescriptor, name)) {
            super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
            return
        }

        val generatedCopyMethodIndex = result.findGeneratedCopyMethodIndex(thisDescriptor)
        if (generatedCopyMethodIndex == null) {
            messageCollector?.error("Cannot find generated copy method!", thisDescriptor.findPsi())
            return
        }

        handleByAnnotation(thisDescriptor, name, onNoCopy = {
            handleNoCopy(generatedCopyMethodIndex, result)
        })
    }

    private fun handleByAnnotation(
            classDescriptor: ClassDescriptor,
            name: Name,
            onNoCopy: () -> Unit
    ) {

        val hasNoCopy = classDescriptor.hasNoCopy()

        if (!classDescriptor.isData) {
            val annotation = when {
                hasNoCopy -> "@NoCopy"
                else -> "no-copy annotations"
            }
            messageCollector?.error(
                    "Only data classes could be annotated with $annotation",
                    classDescriptor.findPsi()
            )
            return
        }

        if (name.asString() != "copy") {
            return
        }

        when {
            hasNoCopy -> onNoCopy()
        }
    }

    private fun handleNoCopy(
            generatedCopyMethodIndex: Int,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        result.remove(result.elementAt(generatedCopyMethodIndex))
    }
}
