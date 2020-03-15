package com.ahmedmourad.nocopy.idea.inspections

import com.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import com.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import com.ahmedmourad.nocopy.idea.inspections.fixes.AnnotateClassWithFix
import com.ahmedmourad.nocopy.idea.utils.hasLeastVisibleCopy
import com.ahmedmourad.nocopy.idea.utils.hasNoCopy
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.classVisitor
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier

class DataClassPrivateConstructorInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return classVisitor { klass ->

            val primaryConstructor = klass.primaryConstructor

            if (klass.isData() && primaryConstructor?.isPrivate() == true) {

                if (klass.hasNoCopy() || klass.hasLeastVisibleCopy()) {
                    return@classVisitor
                }

                val visibilityModifier = primaryConstructor.visibilityModifier() ?: return@classVisitor
                val problemDescriptor = holder.manager.createProblemDescriptor(
                        visibilityModifier,
                        visibilityModifier,
                        "NoCopy: Private data class constructor is exposed via the generated 'copy' method.",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOnTheFly,
                        AnnotateClassWithFix(FqName(NO_COPY_ANNOTATION)),
                        AnnotateClassWithFix(FqName(LEAST_VISIBLE_COPY_ANNOTATION))
                )

                holder.registerProblem(problemDescriptor)
            }
        }
    }
}
