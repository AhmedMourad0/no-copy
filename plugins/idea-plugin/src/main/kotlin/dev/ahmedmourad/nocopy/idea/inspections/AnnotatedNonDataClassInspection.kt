package dev.ahmedmourad.nocopy.idea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import dev.ahmedmourad.nocopy.idea.inspections.fixes.ConvertToDataClassFix
import dev.ahmedmourad.nocopy.idea.inspections.fixes.RemoveAllClassAnnotationsFix
import dev.ahmedmourad.nocopy.idea.inspections.fixes.RemoveClassAnnotationFix
import dev.ahmedmourad.nocopy.idea.utils.hasNoCopy
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.classOrObjectVisitor

class AnnotatedNonDataClassInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return classOrObjectVisitor { klass ->

            if (!klass.hasModifier(KtTokens.DATA_KEYWORD) && klass.hasNoCopy()) {

                val quickFixes = mutableListOf<LocalQuickFix>()

                if (klass.canBeData()) {
                    quickFixes += ConvertToDataClassFix()
                }

                val annotations = mutableListOf<FqName>()

                if (klass.hasNoCopy()) {
                    annotations += FqName(NO_COPY_ANNOTATION)
                }

                if (annotations.size == 1) {
                    quickFixes += RemoveClassAnnotationFix(annotations[0])
                } else if (annotations.size > 1) {
                    quickFixes += RemoveAllClassAnnotationsFix(annotations)
                }

                val identifier = klass.nameIdentifier ?: return@classOrObjectVisitor
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

private fun KtClassOrObject.canBeData(): Boolean {
    return arrayOf(
            KtTokens.ENUM_KEYWORD,
            KtTokens.INNER_KEYWORD,
            KtTokens.SEALED_KEYWORD,
            KtTokens.ABSTRACT_KEYWORD,
            KtTokens.INLINE_KEYWORD,
            KtTokens.ANNOTATION_KEYWORD
    ).none(this::hasModifier)// && this.isOrdinaryClass
}
