package com.example.android.features.home.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timeMillis: Long,
    val isMock: Boolean
)
