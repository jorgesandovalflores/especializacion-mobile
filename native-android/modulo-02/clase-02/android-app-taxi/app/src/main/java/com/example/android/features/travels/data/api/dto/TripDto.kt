package com.example.android.features.travels.data.api.dto

data class TripDto(
    val id: String,
    val passengerName: String,
    val pickup: LocationDto,
    val dropoff: LocationDto,
    val requestedAt: String,
    val status: String
)