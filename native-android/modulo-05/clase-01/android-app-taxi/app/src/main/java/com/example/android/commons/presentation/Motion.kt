package com.example.android.commons.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween

const val MOTION_SHORT_MS = 150
const val MOTION_MEDIUM_MS = 300
const val MOTION_LONG_MS = 600

private const val SHAKE_STEP_MS = 50
private val SHAKE_PATTERN = listOf(1f, -1f, 0.6f, -0.6f, 0.3f, 0f)

suspend fun Animatable<Float, AnimationVector1D>.shake(distancePx: Float) {
    SHAKE_PATTERN.forEach { factor ->
        animateTo(distancePx * factor, tween(durationMillis = SHAKE_STEP_MS))
    }
}
