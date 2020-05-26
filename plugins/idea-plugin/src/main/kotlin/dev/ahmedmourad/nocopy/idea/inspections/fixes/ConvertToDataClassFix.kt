package dev.ahmedmourad.nocopy.idea.inspections.fixes

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ConvertToDataClassFix : LocalQuickFix {

    override fun getName() = "Convert to data class"

    override fun getFamilyName() = "Convert to data class"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        val containingClass = descriptor.startElement.getParentOfType<KtClass>(strict = false) ?: return

        if (!FileModificationService.getInstance().preparePsiElementForWrite(containingClass)) {
            return
        }

        containingClass.addModifier(KtTokens.DATA_KEYWORD)
    }
}
