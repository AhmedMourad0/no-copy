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

    val hasShatterAnnotation = thisDescriptor.hasShatter(fqShatterAnnotation)

    if (!thisDescriptor.isData) {

        if (thisDescriptor.hasMirror(fqMirrorAnnotation)) {
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
        onShatter()
    } else if (hasMirrorAnnotation) {
        onMirror(mirrorConstructors[0].visibility)
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
        error("Mirroring internal constructors is not currently supported, try Shatter instead ($fqName)")
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
    return this.constructors.any { it.hasAnnotation(fqMirrorAnnotation) }
}

private fun ClassDescriptor.hasShatter(fqShatterAnnotation: FqName): Boolean {
    return this.hasAnnotation(fqShatterAnnotation)
}
