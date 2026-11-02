package com.example.android.features.trip.data.repository

import com.example.android.features.trip.data.mapper.toDomain
import com.example.android.features.trip.data.remote.dto.DriverLocationDto
import com.example.android.features.trip.data.remote.dto.OfferDto
import com.example.android.features.trip.data.remote.dto.TripDto
import com.example.android.features.trip.domain.model.TripEvent
import com.google.gson.Gson

object TripEvents {
    const val OFFER = "trip:offer"
    const val STATUS = "trip:status"
    const val DRIVER_LOCATION = "driver:location"
}

class TripEventParser(private val gson: Gson = Gson()) {

    fun parse(event: String, json: String): TripEvent? = runCatching {
        when (event) {
            TripEvents.OFFER -> TripEvent.OfferReceived(gson.fromJson(json, OfferDto::class.java).toDomain())
            TripEvents.STATUS -> TripEvent.StatusChanged(gson.fromJson(json, TripDto::class.java).toDomain())
            TripEvents.DRIVER_LOCATION -> TripEvent.DriverMoved(
                gson.fromJson(json, DriverLocationDto::class.java).toDomain()
            )
            else -> null
        }
    }.getOrNull()
}
