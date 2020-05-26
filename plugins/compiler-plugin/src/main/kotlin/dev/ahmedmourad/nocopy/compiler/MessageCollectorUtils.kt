package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.com.intellij.psi.PsiElement

internal fun MessageCollector.error(message: String, element: PsiElement?) {
    this.report(
            CompilerMessageSeverity.ERROR,
            message,
            MessageUtil.psiElementToMessageLocation(element)
    )
}

internal fun MessageCollector.log(message: String) {
    this.report(
            CompilerMessageSeverity.LOGGING,
            "NO_COPY: $message",
            CompilerMessageLocation.create(null)
    )
}
