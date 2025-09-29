package com.example.android.features.travels.data.api

import com.example.android.features.travels.data.api.dto.TripDto
import retrofit2.http.GET

// el endpoint "/" devuelve lista de viajes pendientes
interface TravelApi {
    @GET("/")
    suspend fun getPendingTrips(): List<TripDto>
}