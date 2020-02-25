package com.ahmedmourad.mirror.core

enum class Strategy {
    /** The plugin will only mirror or shatter `copy` of the data classes marked with specified annotations */
    BY_ANNOTATIONS,
    /** The plugin will shatter all `copy` methods of all data classes (no annotations needed) */
    SHATTER_ALL,
    /** The plugin will mirror the least visible constructor for all copy methods of all data classes (no annotations needed) */
    MIRROR_ALL_BY_PRIMARY,
    /** The plugin will mirror the primary constructor for all copy methods of all data classes (no annotations needed) */
    MIRROR_ALL_BY_LEAST_VISIBLE
}
