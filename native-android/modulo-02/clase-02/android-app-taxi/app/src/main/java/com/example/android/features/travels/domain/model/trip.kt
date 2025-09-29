package com.example.android.features.travels.domain.model

data class Trip(
    val id: String,
    val passengerName: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val requestedAtIso: String
)