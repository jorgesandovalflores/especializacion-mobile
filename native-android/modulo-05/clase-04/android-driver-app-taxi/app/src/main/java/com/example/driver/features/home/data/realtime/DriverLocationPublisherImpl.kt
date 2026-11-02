package com.example.driver.features.home.data.realtime

import com.example.driver.core.domain.realtime.RealtimeGateway
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.repository.DriverLocationPublisher

class DriverLocationPublisherImpl(
    private val gateway: RealtimeGateway
) : DriverLocationPublisher {

    override fun start() = gateway.connect()

    override fun publish(location: UserLocation): Boolean =
        gateway.emit(RealtimeEvents.DRIVER_LOCATION, location.toPayload())
}

fun UserLocation.toPayload(): Map<String, Any?> = mapOf(
    "lat" to latitude,
    "lng" to longitude,
    "bearing" to bearing?.toDouble(),
    "speed" to speedMps?.toDouble(),
    "accuracy" to accuracyMeters.toDouble()
)
