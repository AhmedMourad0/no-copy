package dev.ahmedmourad.nocopy.compiler

import dev.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import dev.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.coroutines.createCustomCopy
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.name.FqName
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

        if (!isDataCopyMethod(thisDescriptor, name)) {
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
                    .findLeastVisible(classDescriptor)
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

        if (visibility == Visibilities.INTERNAL) {
            messageCollector?.error(
                    "Mirroring internal constructors is not currently supported, try @NoCopy instead",
                    classDescriptor.findPsi()
            )
            return
        }

        result.elementAt(generatedCopyMethodIndex).let { descriptor ->
            val newCopy = descriptor.createCustomCopy { it.newCopyBuilder().setVisibility(visibility) }
            result.remove(descriptor)
            result.add(newCopy)
        }
    }

    private fun Collection<ClassConstructorDescriptor>.findLeastVisible(
            classDescriptor: ClassDescriptor
    ): ClassConstructorDescriptor? {
        return this.minBy {
            it.visibility.asInt() ?: messageCollector?.error(
                    "Unrecognized visibility: ${it.visibility}",
                    it.findPsi()
            ).run { 99 }
        } ?: messageCollector?.error(
                "Couldn't find least visible constructor",
                classDescriptor.findPsi()
        ).run { null }
    }

    private fun isDataCopyMethod(
            classDescriptor: ClassDescriptor,
            name: Name
    ): Boolean {
        return classDescriptor.isData && name.asString() == "copy"
    }


    private fun Collection<SimpleFunctionDescriptor>.findGeneratedCopyMethodIndex(
            classDescriptor: ClassDescriptor
    ): Int? {

        if (size == 1) {
            return 0
        }

        val primaryConstructor = classDescriptor.constructors.firstOrNull { it.isPrimary } ?: return null
        val primaryConstructorParameters = primaryConstructor.valueParameters

        val index = this.indexOfLast {
            it.name.asString() == "copy"
                    && it.returnType == classDescriptor.defaultType
                    && it.valueParameters.size == primaryConstructorParameters.size
                    && it.valueParameters.filterIndexed { index, descriptor ->
                primaryConstructorParameters[index].type != descriptor.type &&
                        primaryConstructorParameters[index].name != descriptor.name
            }.isEmpty()
        }
        return if (index < 0) {
            null
        } else {
            index
        }
    }

    private fun handleNoCopy(
            generatedCopyMethodIndex: Int,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        result.remove(result.elementAt(generatedCopyMethodIndex))
    }

    private fun Annotated.hasAnnotation(annotation: FqName): Boolean {
        return annotations.hasAnnotation(annotation)
    }

    private fun ClassDescriptor.hasNoCopy(): Boolean {
        return this.hasAnnotation(FqName(NO_COPY_ANNOTATION))
    }

    private fun ClassDescriptor.hasLeastVisibleCopy(): Boolean {
        return this.hasAnnotation(FqName(LEAST_VISIBLE_COPY_ANNOTATION))
    }

    private fun Visibility.asInt(): Int? {
        return when (this) {
            Visibilities.PRIVATE -> 1
            Visibilities.PROTECTED -> 2
            Visibilities.INTERNAL -> 3
            Visibilities.PUBLIC -> 4
            else -> null
        }
    }
}
