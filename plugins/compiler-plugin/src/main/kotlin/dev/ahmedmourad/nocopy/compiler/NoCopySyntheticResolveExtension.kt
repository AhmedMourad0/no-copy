package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.coroutines.createCustomCopy
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.Visibility
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

        handleByAnnotation(thisDescriptor, name, onLeastVisibleCopy = {
            super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
            handleLeastVisibleCopy(
                    thisDescriptor,
                    it,
                    generatedCopyMethodIndex,
                    result
            )
        }, onNoCopy = {
            handleNoCopy(generatedCopyMethodIndex, result)
        })
    }

    private fun handleByAnnotation(
            classDescriptor: ClassDescriptor,
            name: Name,
            onLeastVisibleCopy: (Visibility) -> Unit,
            onNoCopy: () -> Unit
    ) {

        val hasNoCopy = classDescriptor.hasNoCopy()
        val hasLeastVisibleCopy = classDescriptor.hasLeastVisibleCopy()

        if (!classDescriptor.isData) {
            val annotation = when {
                hasNoCopy -> "@NoCopy"
                hasLeastVisibleCopy -> "@LeastVisibleCopy"
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

        if (hasLeastVisibleCopy && hasNoCopy) {
            messageCollector?.error(
                    "You cannot have @NoCopy and @LeastVisibleCopy on the same data class",
                    classDescriptor.findPsi()
            )
            return
        }

        when {
            hasNoCopy -> onNoCopy()
            hasLeastVisibleCopy -> classDescriptor.constructors
                    .findLeastVisible()
                    ?.visibility
                    ?.let(onLeastVisibleCopy)
        }
    }

    private fun handleLeastVisibleCopy(
            classDescriptor: ClassDescriptor,
            visibility: Visibility,
            generatedCopyMethodIndex: Int,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {

//        if (visibility == Visibilities.INTERNAL) {
//            messageCollector?.error(
//                    "Mirroring internal constructors is not currently supported, try @NoCopy instead",
//                    classDescriptor.findPsi()
//            )
//            return
//        }

        result.elementAt(generatedCopyMethodIndex).let { descriptor ->
            val newCopy = descriptor.createCustomCopy { it.newCopyBuilder().setVisibility(visibility) }
            result.remove(descriptor)
            result.add(newCopy)
        }
    }

    private fun handleNoCopy(
            generatedCopyMethodIndex: Int,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        result.remove(result.elementAt(generatedCopyMethodIndex))
    }
}
