package com.ahmedmourad.mirror.compiler

import com.ahmedmourad.mirror.core.Strategy
import org.jetbrains.kotlin.codegen.coroutines.createCustomCopy
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class MirrorSyntheticResolveExtension(
        private val fqMirrorAnnotation: FqName,
        private val fqShatterAnnotation: FqName,
        private val strategy: Strategy
) : SyntheticResolveExtension {

    override fun generateSyntheticMethods(
            thisDescriptor: ClassDescriptor,
            name: Name,
            bindingContext: BindingContext,
            fromSupertypes: List<SimpleFunctionDescriptor>,
            result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        when (strategy) {

            Strategy.SHATTER_ALL -> {
                if (isDataCopyMethod(thisDescriptor, name)) {
                    handleShatter(result)
                } else {
                    super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
                }
            }

            Strategy.MIRROR_ALL_BY_LEAST_VISIBLE -> {
                super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
                if (isDataCopyMethod(thisDescriptor, name)) {
                    handleMirror(
                            thisDescriptor.fqNameOrNull(),
                            thisDescriptor.constructors.findLeastVisible().visibility,
                            result
                    )
                }
            }

            Strategy.MIRROR_ALL_BY_PRIMARY -> {
                super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
                if (isDataCopyMethod(thisDescriptor, name)) {
                    handleMirror(
                            thisDescriptor.fqNameOrNull(),
                            thisDescriptor.constructors.findPrimary().visibility,
                            result
                    )
                }
            }

            Strategy.BY_ANNOTATIONS -> {
                handleByAnnotation(thisDescriptor, name, fqMirrorAnnotation, fqShatterAnnotation, onMirror = {
                    super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
                    handleMirror(thisDescriptor.fqNameOrNull(), it, result)
                }, onShatter = {
                    handleShatter(result)
                })
            }
        }
    }
}

private fun isDataCopyMethod(thisDescriptor: ClassDescriptor, name: Name): Boolean {
    return thisDescriptor.isData && name.asString() == "copy"
}

private fun handleByAnnotation(
        thisDescriptor: ClassDescriptor,
        name: Name,
        fqMirrorAnnotation: FqName,
        fqShatterAnnotation: FqName,
        onMirror: (Visibility) -> Unit,
        onShatter: () -> Unit
) {

    val isShattered = thisDescriptor.hasShatter(fqShatterAnnotation)

    if (!thisDescriptor.isData) {

        if (thisDescriptor.hasMirror(fqMirrorAnnotation)) {
            error("Only data classes could be annotated with @Mirror (${thisDescriptor.fqNameOrNull()})")
        }

        if (isShattered) {
            error("Only data classes could be annotated with @Shatter (${thisDescriptor.fqNameOrNull()})")
        }

        return
    }

    if (name.asString() != "copy") {
        return
    }

    val mirroredConstructors = thisDescriptor.constructors.filter { it.hasAnnotation(fqMirrorAnnotation) }

    if (mirroredConstructors.size > 1) {
        error("You cannot have more than one @Mirror annotated constructors (${thisDescriptor.fqNameOrNull()})")
    }

    val isConstructorMirrored = mirroredConstructors.isNotEmpty()
    val isClassMirrored = thisDescriptor.hasAnnotation(fqMirrorAnnotation)

    if (isConstructorMirrored && isClassMirrored) {
        error("You cannot have @Mirror on a class and its constructor at the same time (${thisDescriptor.fqNameOrNull()})")
    }

    if ((isClassMirrored || isConstructorMirrored) && isShattered) {
        error("You cannot have @Mirror and Shatter for the same data class (${thisDescriptor.fqNameOrNull()})")
    }

    when {
        isShattered -> onShatter()
        isConstructorMirrored -> onMirror(mirroredConstructors[0].visibility)
        isClassMirrored -> onMirror(thisDescriptor.constructors.findLeastVisible().visibility)
    }
}

private fun handleShatter(result: MutableCollection<SimpleFunctionDescriptor>) {
    result.clear()
}

private fun handleMirror(
        fqName: FqName?,
        visibility: Visibility,
        result: MutableCollection<SimpleFunctionDescriptor>
) {

    if (visibility == Visibilities.INTERNAL) {
        error("Mirroring internal constructors is not currently supported, try @Shatter instead ($fqName)")
    }

    val newCopy = result.firstOrNull()
            ?.createCustomCopy { it.newCopyBuilder().setVisibility(visibility) }

    result.clear()
    newCopy?.let(result::add) ?: error("Couldn't mirror constructor ($fqName)")
}


private fun Annotated.hasAnnotation(mirrorAnnotation: FqName): Boolean {
    return annotations.hasAnnotation(mirrorAnnotation)
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

private fun Collection<ClassConstructorDescriptor>.findPrimary(): ClassConstructorDescriptor {
    return this.firstOrNull { it.isPrimary } ?: error("Couldn't find primary constructor")
}

private fun ClassDescriptor.hasMirror(fqMirrorAnnotation: FqName): Boolean {
    return this.hasAnnotation(fqMirrorAnnotation) || this.constructors.any { it.hasAnnotation(fqMirrorAnnotation) }
}

private fun ClassDescriptor.hasShatter(fqShatterAnnotation: FqName): Boolean {
    return this.hasAnnotation(fqShatterAnnotation)
}
