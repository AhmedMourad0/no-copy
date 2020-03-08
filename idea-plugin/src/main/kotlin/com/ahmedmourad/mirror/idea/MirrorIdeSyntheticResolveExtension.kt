package com.ahmedmourad.mirror.idea

import com.ahmedmourad.mirror.core.Strategy
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.name.FqName
import com.ahmedmourad.mirror.compiler.MirrorSyntheticResolveExtension as MirrorCompilerSyntheticResolveExtension

open class MirrorIdeSyntheticResolveExtension(project: Project) : MirrorCompilerSyntheticResolveExtension(
        FqName("com.ahmedmourad.mirror.annotations.Mirror"),
        FqName("com.ahmedmourad.mirror.annotations.Shatter"),
        Strategy.BY_ANNOTATIONS
)
