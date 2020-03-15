package com.ahmedmourad.nocopy.idea.extensions

import com.intellij.openapi.project.Project
import com.ahmedmourad.nocopy.compiler.NoCopySyntheticResolveExtension as NoCopyCompilerSyntheticResolveExtension

open class NoCopyIdeSyntheticResolveExtension(
        project: Project
) : NoCopyCompilerSyntheticResolveExtension() {
    override fun onError(message: String) {
        // Don't throw an exception
    }
}
