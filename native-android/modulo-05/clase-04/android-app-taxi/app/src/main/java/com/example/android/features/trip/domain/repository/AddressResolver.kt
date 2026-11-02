package com.example.android.features.trip.domain.repository

import com.example.android.features.trip.domain.model.GeoPoint

fun interface AddressResolver {
    suspend fun addressOf(point: GeoPoint): String?
}
