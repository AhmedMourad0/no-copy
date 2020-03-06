package com.ahmedmourad.mirror.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that the copy method of a particular data class won't be generated
 */
@Retention(BINARY)
@Target(CLASS)
annotation class Shatter
