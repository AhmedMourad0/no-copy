package com.ahmedmourad.nocopy.idea.inspections

import com.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import com.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import com.ahmedmourad.nocopy.idea.inspections.fixes.ConvertToDataClassFix
import com.ahmedmourad.nocopy.idea.inspections.fixes.RemoveAllClassAnnotationFix
import com.ahmedmourad.nocopy.idea.inspections.fixes.RemoveClassAnnotationFix
import com.ahmedmourad.nocopy.idea.utils.hasLeastVisibleCopy
import com.ahmedmourad.nocopy.idea.utils.hasNoCopy
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.classVisitor
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.isObjectLiteral

class AnnotatedNonDataClassInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return classVisitor { klass ->

            if (!klass.isData() && (klass.hasNoCopy() || klass.hasLeastVisibleCopy())) {

                val quickFixes = mutableListOf<LocalQuickFix>()

                if (klass.canBeData()) {
                    quickFixes += ConvertToDataClassFix()
                }

                val annotations = mutableListOf<FqName>()

                if (klass.hasNoCopy()) {
                    annotations += FqName(NO_COPY_ANNOTATION)
                }

                if (klass.hasLeastVisibleCopy()) {
                    annotations += FqName(LEAST_VISIBLE_COPY_ANNOTATION)
                }

                if (annotations.size == 1) {
                    quickFixes += RemoveClassAnnotationFix(annotations[0])
                } else if (annotations.size > 1) {
                    quickFixes += RemoveAllClassAnnotationFix(annotations)
                }

                val identifier = klass.nameIdentifier!!
                val problemDescriptor = holder.manager.createProblemDescriptor(
                        identifier,
                        identifier,
                        "NoCopy: no-copy annotations can only be applied to data classes.",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOnTheFly,
                        *quickFixes.toTypedArray()
                )

                holder.registerProblem(problemDescriptor)
            }
        }
    }
}

private fun KtClass.canBeData(): Boolean {
    val cannotBeData = this.isInterface() ||
            this.isEnum() ||
            this.isInner() ||
            this.isSealed() ||
            this.isAbstract() ||
            this.isAnnotation() ||
            this.hasModifier(KtTokens.INLINE_KEYWORD) ||
            this.isObjectLiteral()
    return !cannotBeData
}
