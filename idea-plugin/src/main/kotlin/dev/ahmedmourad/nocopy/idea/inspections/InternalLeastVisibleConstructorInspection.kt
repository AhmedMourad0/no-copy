package dev.ahmedmourad.nocopy.idea.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import dev.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import dev.ahmedmourad.nocopy.idea.inspections.fixes.ReplaceClassAnnotationWithFix
import dev.ahmedmourad.nocopy.idea.utils.hasLeastVisibleCopy
import dev.ahmedmourad.nocopy.idea.utils.hasNoCopy
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.allConstructors
import org.jetbrains.kotlin.psi.classVisitor

class InternalLeastVisibleConstructorInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return classVisitor { klass ->

            if (klass.isData() && klass.hasLeastVisibleCopy() && !klass.hasNoCopy()) {

                if (klass.allConstructors.map { it.visibilityAsInt() }.min() != VISIBILITY_INTERNAL) {
                    return@classVisitor
                }

                val annotation = klass.findAnnotation(FqName(LEAST_VISIBLE_COPY_ANNOTATION)) ?: return@classVisitor
                val problemDescriptor = holder.manager.createProblemDescriptor(
                        annotation,
                        annotation,
                        "NoCopy: Mirroring internal constructors is not yet supported.",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOnTheFly,
                        ReplaceClassAnnotationWithFix(FqName(LEAST_VISIBLE_COPY_ANNOTATION), FqName(NO_COPY_ANNOTATION))
                )

                holder.registerProblem(problemDescriptor)
            }
        }
    }
}

private fun KtConstructor<*>.visibilityAsInt(): Int {
    return when {
        this.hasModifier(KtTokens.PRIVATE_KEYWORD) -> VISIBILITY_PRIVATE
        this.hasModifier(KtTokens.PROTECTED_KEYWORD) -> VISIBILITY_PROTECTED
        this.hasModifier(KtTokens.INTERNAL_KEYWORD) -> VISIBILITY_INTERNAL
        else -> VISIBILITY_PUBLIC
    }
}

private const val VISIBILITY_PRIVATE = 1
private const val VISIBILITY_PROTECTED = 2
private const val VISIBILITY_INTERNAL = 3
private const val VISIBILITY_PUBLIC = 4
