package com.ahmedmourad.nocopy.idea.utils

import com.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import com.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated

internal fun KtAnnotated.hasNoCopy(): Boolean {
    return this.findAnnotation(FqName(NO_COPY_ANNOTATION)) != null
}

internal fun KtAnnotated.hasLeastVisibleCopy(): Boolean {
    return this.findAnnotation(FqName(LEAST_VISIBLE_COPY_ANNOTATION)) != null
}
