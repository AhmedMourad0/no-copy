package dev.ahmedmourad.nocopy.compiler

import dev.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private fun Annotated.hasAnnotation(annotation: FqName): Boolean {
    return annotations.hasAnnotation(annotation)
}

internal fun ClassDescriptor.hasNoCopy(): Boolean {
    return this.hasAnnotation(FqName(NO_COPY_ANNOTATION))
}

internal fun isGeneratedCopyMethod(
        classDescriptor: ClassDescriptor,
        name: Name
): Boolean {
    return classDescriptor.isData && name.asString() == "copy"
}

internal fun Collection<SimpleFunctionDescriptor>.findGeneratedCopyMethodIndex(
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
