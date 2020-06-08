package dev.ahmedmourad.nocopy.annotations

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that the copy method of a particular data class won't be generated
 */
@Retention(SOURCE)
@Target(CLASS)
annotation class NoCopy
