package com.ahmedmourad.mirror.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR

/**
 * An annotation to indicate that the visibility of the copy method of a particular data class should match its constructor's
 */
@Retention(BINARY)
@Target(CONSTRUCTOR)
annotation class Mirror
