package com.example.android.features.travels.domain.repository

import com.example.android.features.travels.domain.model.Trip

interface TravelRepository {
    suspend fun getPendingTrips(): List<Trip>
}