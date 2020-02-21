package com.ahmedmourad.mirror.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that the visibility of the copy method of a particular data class should match its constructor's
 */
@Retention(BINARY)
@Target(CLASS)
annotation class Mirror
