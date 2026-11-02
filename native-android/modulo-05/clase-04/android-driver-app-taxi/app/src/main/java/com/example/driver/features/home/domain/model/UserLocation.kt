package com.example.driver.features.home.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timeMillis: Long,
    val isMock: Boolean,
    val bearing: Float? = null,
    val speedMps: Float? = null
)
