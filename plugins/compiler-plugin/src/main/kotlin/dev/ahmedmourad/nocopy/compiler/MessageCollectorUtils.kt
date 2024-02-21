package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.file

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

internal fun MessageCollector.error(message: String, declaration: IrDeclaration) {
    val location = declaration.file.locationOf(declaration)
    report(CompilerMessageSeverity.ERROR, "$LOG_TAG $message", location)
}

/** Finds the line and column of [irElement] within this file. */
private fun IrFile.locationOf(irElement: IrElement?): CompilerMessageSourceLocation {
    val sourceRangeInfo = fileEntry.getSourceRangeInfo(
        beginOffset = irElement?.startOffset ?: SYNTHETIC_OFFSET,
        endOffset = irElement?.endOffset ?: SYNTHETIC_OFFSET,
    )
    return CompilerMessageLocationWithRange.create(
        path = sourceRangeInfo.filePath,
        lineStart = sourceRangeInfo.startLineNumber + 1,
        columnStart = sourceRangeInfo.startColumnNumber + 1,
        lineEnd = sourceRangeInfo.endLineNumber + 1,
        columnEnd = sourceRangeInfo.endColumnNumber + 1,
        lineContent = null,
    )!!
}
