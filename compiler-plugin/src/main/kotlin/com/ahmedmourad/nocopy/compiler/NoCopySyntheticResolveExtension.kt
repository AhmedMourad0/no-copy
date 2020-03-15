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

    protected open fun onError(message: String) {
        error(message)
    }

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
            handleLeastVisibleCopy(thisDescriptor.fqNameOrNull(), it, result, ::onError)
        }, onNoCopy = {
            handleNoCopy(result)
        }, onError = ::onError)
    }
}

private fun handleByAnnotation(
        thisDescriptor: ClassDescriptor,
        name: Name,
        onLeastVisibleCopy: (Visibility) -> Unit,
        onNoCopy: () -> Unit,
        onError: (String) -> Unit
) {

    val hasNoCopy = thisDescriptor.hasNoCopy()
    val hasLeastVisibleCopy = thisDescriptor.hasLeastVisibleCopy()

    if (!thisDescriptor.isData) {

        if (hasNoCopy) {
            onError("Only data classes could be annotated with @NoCopy (${thisDescriptor.fqNameOrNull()})")
            return
        }

        if (hasLeastVisibleCopy) {
            onError("Only data classes could be annotated with @LeastVisibleCopy (${thisDescriptor.fqNameOrNull()})")
            return
        }

        return
    }

    if (name.asString() != "copy") {
        return
    }

    if (hasLeastVisibleCopy && hasNoCopy) {
        onError("You cannot have @NoCopy and @LeastVisibleCopy on the same data class (${thisDescriptor.fqNameOrNull()})")
        return
    }

    when {
        hasNoCopy -> onNoCopy()
        hasLeastVisibleCopy -> thisDescriptor.constructors.findLeastVisible(onError)?.visibility?.let(onLeastVisibleCopy)
    }
}

private fun handleLeastVisibleCopy(
        fqName: FqName?,
        visibility: Visibility,
        result: MutableCollection<SimpleFunctionDescriptor>,
        onError: (String) -> Unit
) {

    if (visibility == Visibilities.INTERNAL) {
        onError("Mirroring internal constructors is not currently supported, try @NoCopy instead ($fqName)")
        return
    }

    val newCopy = result.firstOrNull()
            ?.createCustomCopy { it.newCopyBuilder().setVisibility(visibility) }

    result.clear()
    newCopy?.let(result::add) ?: onError("Couldn't mirror constructor ($fqName)")
}

private fun Collection<ClassConstructorDescriptor>.findLeastVisible(
        onError: (String) -> Unit
): ClassConstructorDescriptor? {
    return this.minBy {
        it.visibility.asInt() ?: onError("Unrecognized visibility: ${it.visibility}").run { 99 }
    } ?: onError("Couldn't find least visible constructor").run { null }
}

private fun isDataCopyMethod(
        thisDescriptor: ClassDescriptor,
        name: Name
): Boolean {
    return thisDescriptor.isData && name.asString() == "copy"
}

private fun handleNoCopy(result: MutableCollection<SimpleFunctionDescriptor>) {
    result.clear()
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
