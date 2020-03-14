package com.ahmedmourad.nocopy.compiler

import com.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import com.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import org.jetbrains.kotlin.codegen.coroutines.createCustomCopy
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

open class NoCopySyntheticResolveExtension : SyntheticResolveExtension {

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

        handleByAnnotation(thisDescriptor, name, onLeastVisibleCopy = {
            super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
            handleLeastVisibleCopy(thisDescriptor.fqNameOrNull(), it, result)
        }, onNoCopy = {
            handleNoCopy(result)
        })
    }
}

private fun isDataCopyMethod(
        thisDescriptor: ClassDescriptor,
        name: Name
): Boolean {
    return thisDescriptor.isData && name.asString() == "copy"
}

private fun handleByAnnotation(
        thisDescriptor: ClassDescriptor,
        name: Name,
        onLeastVisibleCopy: (Visibility) -> Unit,
        onNoCopy: () -> Unit
) {

    val hasNoCopy = thisDescriptor.hasNoCopy()
    val hasLeastVisibleCopy = thisDescriptor.hasLeastVisibleCopy()

    if (!thisDescriptor.isData) {

        if (hasLeastVisibleCopy) {
            error("Only data classes could be annotated with @LeastVisibleCopy (${thisDescriptor.fqNameOrNull()})")
        }

        if (hasNoCopy) {
            error("Only data classes could be annotated with @NoCopy (${thisDescriptor.fqNameOrNull()})")
        }

        return
    }

    if (name.asString() != "copy") {
        return
    }

    if (hasLeastVisibleCopy && hasNoCopy) {
        error("You cannot have @LeastVisibleCopy and @NoCopy on the same data class (${thisDescriptor.fqNameOrNull()})")
    }

    when {
        hasNoCopy -> onNoCopy()
        hasLeastVisibleCopy -> onLeastVisibleCopy(thisDescriptor.constructors.findLeastVisible().visibility)
    }
}

private fun handleNoCopy(result: MutableCollection<SimpleFunctionDescriptor>) {
    result.clear()
}

private fun handleLeastVisibleCopy(
        fqName: FqName?,
        visibility: Visibility,
        result: MutableCollection<SimpleFunctionDescriptor>
) {

    if (visibility == Visibilities.INTERNAL) {
        error("Mirroring internal constructors is not currently supported, try @NoCopy instead ($fqName)")
    }

    val newCopy = result.firstOrNull()
            ?.createCustomCopy { it.newCopyBuilder().setVisibility(visibility) }

    result.clear()
    newCopy?.let(result::add) ?: error("Couldn't mirror constructor ($fqName)")
}

private fun Annotated.hasAnnotation(annotation: FqName): Boolean {
    return annotations.hasAnnotation(annotation)
}

private fun Collection<ClassConstructorDescriptor>.findLeastVisible(): ClassConstructorDescriptor {
    return this.minBy {
        when (val v = it.visibility) {
            Visibilities.PRIVATE -> 1
            Visibilities.PROTECTED -> 2
            Visibilities.INTERNAL -> 3
            Visibilities.PUBLIC -> 4
            else -> error("Unrecognized visibility: $v")
        }
    } ?: error("Couldn't find least visible constructor")
}

private fun ClassDescriptor.hasLeastVisibleCopy(): Boolean {
    return this.hasAnnotation(FqName(LEAST_VISIBLE_COPY_ANNOTATION))
}

private fun ClassDescriptor.hasNoCopy(): Boolean {
    return this.hasAnnotation(FqName(NO_COPY_ANNOTATION))
}
