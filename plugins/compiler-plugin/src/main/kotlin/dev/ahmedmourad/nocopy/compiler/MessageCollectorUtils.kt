package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.com.intellij.psi.PsiElement

internal const val LOG_TAG = "NoCopy: "

internal fun MessageCollector.error(message: String, element: PsiElement?) {
    this.report(
        CompilerMessageSeverity.ERROR,
        "$LOG_TAG$message",
        MessageUtil.psiElementToMessageLocation(element)
    )
}

internal fun MessageCollector.log(message: String) {
    this.report(
        CompilerMessageSeverity.LOGGING,
        "$LOG_TAG$message",
        CompilerMessageLocation.create(null)
    )
}
