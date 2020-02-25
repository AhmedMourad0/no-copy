package com.ahmedmourad.mirror.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.coroutines.createCustomCopy
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class MirrorSyntheticResolveExtension(
        private val messageCollector: MessageCollector,
        private val fqMirrorAnnotation: FqName,
        private val fqShatterAnnotation: FqName
) : SyntheticResolveExtension {

    override fun generateSyntheticMethods(
            thisDescriptor: ClassDescriptor,
            name: Name,
            bindingContext: BindingContext,
            fromSupertypes: List<SimpleFunctionDescriptor>,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)

        val hasShatterAnnotation = thisDescriptor.hasAnnotation(fqShatterAnnotation)

        if (!thisDescriptor.isData) {

            if (thisDescriptor.constructors.any { it.hasAnnotation(fqMirrorAnnotation) }) {
                error("Only data classes could be annotated with Mirror (${thisDescriptor.fqNameOrNull()})")
            }

            if (hasShatterAnnotation) {
                error("Only data classes could be annotated with Shatter (${thisDescriptor.fqNameOrNull()})")
            }

            return
        }

        if (name.asString() != "copy") {
            return
        }

        val mirrorConstructors = thisDescriptor.constructors.filter { it.hasAnnotation(fqMirrorAnnotation) }

        if (mirrorConstructors.size > 1) {
            error("You cannot have more than one Mirror annotated constructors (${thisDescriptor.fqNameOrNull()})")
        }

        val hasMirrorAnnotation = mirrorConstructors.isNotEmpty()

        if (hasMirrorAnnotation && hasShatterAnnotation) {
            error("You cannot have Mirror and Shatter for the same data class (${thisDescriptor.fqNameOrNull()})")
        }

        if (hasShatterAnnotation) {
            handleShatter(result)
        } else if (hasMirrorAnnotation) {
            handleMirror(thisDescriptor, mirrorConstructors[0], result)
        }
    }

    private fun handleShatter(result: MutableCollection<SimpleFunctionDescriptor>) {
        result.clear()
    }

    private fun handleMirror(
            thisDescriptor: ClassDescriptor,
            constructor: ClassConstructorDescriptor,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {

        if (constructor.visibility == Visibilities.INTERNAL) {
            error("Mirroring internal constructors is not currently supported, try Shatter instead (${thisDescriptor.fqNameOrNull()})")
        }

        val newCopy = result.firstOrNull()
                ?.createCustomCopy { it.newCopyBuilder().setVisibility(constructor.visibility) }

        result.clear()
        newCopy?.let(result::add) ?: error("Couldn't mirror constructor (${thisDescriptor.fqNameOrNull()})")
    }
}

internal fun Annotated.hasAnnotation(mirrorAnnotation: FqName): Boolean {
    return annotations.hasAnnotation(mirrorAnnotation)
}

