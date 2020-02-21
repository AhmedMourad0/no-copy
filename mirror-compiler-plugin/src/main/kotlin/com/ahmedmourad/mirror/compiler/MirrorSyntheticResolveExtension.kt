package com.ahmedmourad.mirror.compiler

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.classValueType
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

/**
 * A [SyntheticResolveExtension] that replaces the open toString descriptor
 * with a final descriptor for data classes.
 */
class MirrorSyntheticResolveExtension(
    private val fqMirrorAnnotation: FqName
) : SyntheticResolveExtension {

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)

        val constructor = thisDescriptor.constructors.firstOrNull { it.isPrimary } ?: return
        if (name.asString() == "copy" && thisDescriptor.isMirrored(fqMirrorAnnotation)) {
            // Remove the open toString descriptor
            result.clear()
            // Add a final toString descriptor
            result += SimpleFunctionDescriptorImpl.create(
                thisDescriptor,
                Annotations.EMPTY,
                name,
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                thisDescriptor.source
            ).initialize(
                null,
                thisDescriptor.thisAsReceiverParameter,
                emptyList(),
                emptyList(),
                thisDescriptor.classValueType,
                Modality.FINAL,
                constructor.visibility
            )
        }
    }
}

internal fun Annotated.isMirrored(redactedAnnotation: FqName): Boolean {
    return annotations.hasAnnotation(redactedAnnotation)
}

