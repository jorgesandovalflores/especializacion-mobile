package com.example.android.features.trip.data

import com.example.android.features.trip.data.mapper.PolylineDecoder
import com.example.android.features.trip.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineDecoderTest {

    @Test
    fun `decodifica el ejemplo de la documentacion de Google`() {
        val points = PolylineDecoder.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@")

        assertEquals(
            listOf(GeoPoint(38.5, -120.2), GeoPoint(40.7, -120.95), GeoPoint(43.252, -126.453)),
            points
        )
    }

    @Test
    fun `una cadena vacia no tiene puntos`() {
        assertTrue(PolylineDecoder.decode("").isEmpty())
    }

    @Test
    fun `ignora un punto incompleto al final`() {
        assertEquals(listOf(GeoPoint(38.5, -120.2)), PolylineDecoder.decode("_p~iF~ps|U_ulL"))
    }
}
