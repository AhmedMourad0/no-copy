package dev.ahmedmourad.nocopy.idea.inspections.fixes

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class AnnotateClassWithFix(private val annotation: FqName) : LocalQuickFix {

    override fun getName() = "Annotate class with @${annotation.shortName()}"

    override fun getFamilyName() = "Annotate class with "

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        val containingClass = descriptor.startElement.getParentOfType<KtClass>(strict = false) ?: return

        if (!FileModificationService.getInstance().preparePsiElementForWrite(containingClass)) {
            return
        }

        containingClass.addAnnotation(annotation)
    }
}
