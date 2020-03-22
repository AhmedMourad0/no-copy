package dev.ahmedmourad.nocopy.idea.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.ahmedmourad.nocopy.idea.utils.hasLeastVisibleCopy
import dev.ahmedmourad.nocopy.idea.utils.hasNoCopy
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.namedFunctionVisitor
import org.jetbrains.kotlin.psi.psiUtil.containingClass

class CopyNamedFunctionInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return namedFunctionVisitor { function ->

            val containingClass = function.containingClass() ?: return@namedFunctionVisitor
            val hasNoCopy = containingClass.hasNoCopy()
            val hasLeastVisibleCopy = containingClass.hasLeastVisibleCopy()

            if (function.name == "copy" && containingClass.isData() && hasNoCopy != hasLeastVisibleCopy) {

                var annotationName = "no-copy"

                if (hasNoCopy) {
                    annotationName = "@NoCopy"
                } else if (hasLeastVisibleCopy) {
                    annotationName = "@LeastVisibleCopy"
                }

                val identifier = function.nameIdentifier ?: return@namedFunctionVisitor
                val problemDescriptor = holder.manager.createProblemDescriptor(
                        identifier,
                        identifier,
                        "NoCopy: Having `copy` named methods in $annotationName annotated classes is not yet supported.",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOnTheFly
                )

                holder.registerProblem(problemDescriptor)
            }
        }
    }
}
