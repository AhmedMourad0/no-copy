package com.ahmedmourad.nocopy.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that the visibility of the copy method of a particular
 * data class should match that of the least visible constructor
 */
@Retention(BINARY)
@Target(CLASS)
annotation class LeastVisibleCopy
