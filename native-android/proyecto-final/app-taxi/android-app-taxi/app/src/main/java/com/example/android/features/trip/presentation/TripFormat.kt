package com.example.android.features.trip.presentation

import com.example.android.features.trip.domain.model.Fare
import java.util.Locale
import kotlin.math.roundToInt

fun Fare.formatted(): String {
    val symbol = if (currency == "PEN") "S/" else currency
    return "$symbol ${String.format(Locale.US, "%.2f", amount)}"
}

fun formatDistance(meters: Int): String =
    if (meters < 1_000) "$meters m" else String.format(Locale.US, "%.1f km", meters / 1_000.0)

fun formatDuration(seconds: Int): String = "${(seconds / 60.0).roundToInt().coerceAtLeast(1)} min"

fun etaMinutes(seconds: Int): Int = (seconds / 60.0).roundToInt().coerceAtLeast(1)
