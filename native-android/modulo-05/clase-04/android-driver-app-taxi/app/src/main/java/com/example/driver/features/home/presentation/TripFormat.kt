package com.example.driver.features.home.presentation

import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private const val METERS_PER_KM = 1_000
private const val SECONDS_PER_MINUTE = 60.0

fun formatMoney(amount: Double): String = String.format(Locale.ROOT, "S/ %.2f", amount)

fun formatDistance(meters: Int): String =
    if (meters < METERS_PER_KM) "$meters m" else String.format(Locale.ROOT, "%.1f km", meters / METERS_PER_KM.toDouble())

fun formatDuration(seconds: Int): String = "${max(1, (seconds / SECONDS_PER_MINUTE).roundToInt())} min"

fun formatRating(rating: Double): String = String.format(Locale.ROOT, "%.1f", rating)
