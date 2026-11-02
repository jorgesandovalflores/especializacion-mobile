package com.example.android.features.trip.data

import com.example.android.features.trip.data.repository.TripEventParser
import com.example.android.features.trip.data.repository.TripEvents
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import com.example.android.features.trip.presentation.formatted
import org.junit.Test

class TripEventParserTest {

    private val parser = TripEventParser()

    private val driverJson = """
        {"id":"d2","givenName":"María","familyName":"Rojas","phone":"51900000002","photoUrl":null,
         "rating":5,"vehicle":{"plate":"XYZ-789","brand":"Kia","model":"Rio","color":"Gris"}}
    """.trimIndent()

    @Test
    fun `trip offer se convierte en OfferReceived`() {
        val json = """{"id":"o2","tripId":"t1","price":17,"currency":"PEN","etaSeconds":465,
            "distanceToPickupMeters":2100,"status":"PENDING","createdAt":"2026-11-02T21:00:00Z","driver":$driverJson}"""

        val event = parser.parse(TripEvents.OFFER, json) as TripEvent.OfferReceived

        assertEquals("S/ 17.00", event.offer.price.formatted())
        assertEquals("XYZ-789", event.offer.driver.vehicle.plate)
        assertEquals(465, event.offer.etaSeconds)
    }

    @Test
    fun `trip status trae el viaje con su ruta decodificada`() {
        val json = """{"id":"t1","status":"ARRIVED","origin":{"lat":-12.0464,"lng":-77.0428,"address":null},
            "destination":{"lat":-12.1211,"lng":-77.0297,"address":"Parque Kennedy"},"distanceMeters":9919,
            "durationSeconds":790,"polyline":"_p~iF~ps|U_ulLnnqC","estimatedFare":19.88,"finalFare":17,
            "currency":"PEN","driver":$driverJson,"ratingByPassenger":null}"""

        val event = parser.parse(TripEvents.STATUS, json) as TripEvent.StatusChanged

        assertEquals(TripStatus.ARRIVED, event.trip.status)
        assertEquals(2, event.trip.route.size)
        assertEquals(17.0, event.trip.finalFare!!.amount, 0.0)
    }

    @Test
    fun `driver location trae la posicion y el rumbo`() {
        val json = """{"driverId":"d2","tripId":"t1","lat":-12.05,"lng":-77.04,"bearing":90,"speed":8,"at":"2026-11-02T21:00:00Z"}"""

        val event = parser.parse(TripEvents.DRIVER_LOCATION, json) as TripEvent.DriverMoved

        assertEquals(GeoPoint(-12.05, -77.04), event.position.point)
        assertEquals(90f, event.position.bearing)
    }

    @Test
    fun `un json invalido o un evento desconocido se ignoran`() {
        assertNull(parser.parse(TripEvents.OFFER, "{no es json"))
        assertNull(parser.parse("trip:other", "{}"))
    }
}
