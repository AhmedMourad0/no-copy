package com.ahmedmourad.mirror.intellij

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.allConstructors
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier

class DataPrivateConstructorInspectionSuppressor : InspectionSuppressor {

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {

        if (toolId == TOOL_ID && element is KtClass && element.isData()) {
            return element.hasShatter() ||
                    element.allConstructors
                            .lowestMirroredVisibility()
                            .asInt() < (element.primaryConstructor!!.visibilityModifier() as Visibility).asInt()
        }

        return false
    }

    override fun getSuppressActions(p0: PsiElement?, p1: String): Array<SuppressQuickFix> = SuppressQuickFix.EMPTY_ARRAY

    companion object {
        private const val TOOL_ID = "DataClassPrivateConstructor"
    }
}

private fun KtClass.hasShatter(): Boolean {
    return this.hasAnnotation(FqName("com.ahmedmourad.mirror.annotations.Shatter"))
}

private fun List<KtConstructor<*>>.lowestMirroredVisibility(): Visibility {
    return this.filter(KtConstructor<*>::hasMirror)
            .map { it.visibilityModifier() as Visibility }
            .minBy(Visibility::asInt) ?: error("Couldn't find least visible constructor")
}

private fun Visibility.asInt(): Int {
    return when (this) {
        Visibilities.PRIVATE -> 1
        Visibilities.PROTECTED -> 2
        Visibilities.INTERNAL -> 3
        Visibilities.PUBLIC -> 4
        else -> error("Unrecognized visibility: $this")
    }
}

private fun KtConstructor<*>.hasMirror(): Boolean {
    return this.hasAnnotation(FqName("com.ahmedmourad.mirror.annotations.Mirror"))
}

private fun KtAnnotated.hasAnnotation(annotation: FqName): Boolean {
    return findAnnotation(annotation) != null
}
