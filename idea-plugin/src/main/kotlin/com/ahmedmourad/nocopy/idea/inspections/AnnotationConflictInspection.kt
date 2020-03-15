package com.ahmedmourad.nocopy.idea.inspections

import com.ahmedmourad.nocopy.core.LEAST_VISIBLE_COPY_ANNOTATION
import com.ahmedmourad.nocopy.core.NO_COPY_ANNOTATION
import com.ahmedmourad.nocopy.idea.inspections.fixes.RemoveClassAnnotationFix
import com.ahmedmourad.nocopy.idea.utils.hasLeastVisibleCopy
import com.ahmedmourad.nocopy.idea.utils.hasNoCopy
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.classVisitor

class AnnotationConflictInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return classVisitor { klass ->

            if (klass.isData() && klass.hasNoCopy() && klass.hasLeastVisibleCopy()) {

                val identifier = klass.nameIdentifier!!

                val problemDescriptor = holder.manager.createProblemDescriptor(
                        identifier,
                        identifier,
                        "NoCopy: Data class is annotated with multiple incompatible no-copy annotations.",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOnTheFly,
                        RemoveClassAnnotationFix(FqName(NO_COPY_ANNOTATION)),
                        RemoveClassAnnotationFix(FqName(LEAST_VISIBLE_COPY_ANNOTATION))
                )

                holder.registerProblem(problemDescriptor)
            }
        }
    }
}
