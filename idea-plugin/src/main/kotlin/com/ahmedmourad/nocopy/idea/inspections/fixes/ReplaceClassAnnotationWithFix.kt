package com.ahmedmourad.nocopy.idea.inspections.fixes

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.core.deleteElementAndCleanParent
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ReplaceClassAnnotationWithFix(private val replace: FqName, private val with: FqName) : LocalQuickFix {

    override fun getName() = "Replace @${replace.shortName()} with @${with.shortName()}"

    override fun getFamilyName() = "Replace class annotation "

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        val containingClass = descriptor.startElement.getParentOfType<KtClass>(strict = false) ?: return

        if (!FileModificationService.getInstance().preparePsiElementForWrite(containingClass)) {
            return
        }

        containingClass.findAnnotation(replace)?.deleteElementAndCleanParent()
        containingClass.addAnnotation(with)
    }
}
