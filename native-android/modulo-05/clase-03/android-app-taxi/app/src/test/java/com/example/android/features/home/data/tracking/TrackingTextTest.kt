package com.example.android.features.home.data.tracking

import com.example.android.features.home.userLocation
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class TrackingTextTest {

    private val lima = ZoneId.of("America/Lima")

    @Test
    fun `sin ubicacion todavia muestra que esta buscando`() {
        assertEquals("Buscando tu ubicación…", null.toTrackingText(lima))
    }

    @Test
    fun `con ubicacion muestra la precision y la hora local`() {
        val location = userLocation(accuracyMeters = 12.7f, timeMillis = 1_792_420_860_000L)

        assertEquals("Precisión ±12 m · 9:41", location.toTrackingText(lima))
    }
}
