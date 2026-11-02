package com.example.driver.commons.presentation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WidthClass { Compact, Medium, Expanded }

val MEDIUM_MIN_WIDTH = 600.dp
val EXPANDED_MIN_WIDTH = 840.dp
val COMPACT_MAX_HEIGHT = 480.dp
val CONTENT_MAX_WIDTH = 560.dp

fun widthClassOf(width: Dp): WidthClass = when {
    width < MEDIUM_MIN_WIDTH -> WidthClass.Compact
    width < EXPANDED_MIN_WIDTH -> WidthClass.Medium
    else -> WidthClass.Expanded
}
