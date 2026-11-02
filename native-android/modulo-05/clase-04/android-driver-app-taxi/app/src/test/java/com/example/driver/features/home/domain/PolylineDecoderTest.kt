package com.example.driver.features.home.domain

import com.example.driver.features.home.domain.model.GeoPoint
import com.example.driver.features.home.domain.rules.PolylineDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineDecoderTest {

    private fun assertPoint(expected: GeoPoint, actual: GeoPoint) {
        assertEquals(expected.latitude, actual.latitude, 1e-6)
        assertEquals(expected.longitude, actual.longitude, 1e-6)
    }

    @Test
    fun `decodifica el ejemplo oficial de Google con precision 5`() {
        val points = PolylineDecoder.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@")

        assertEquals(3, points.size)
        assertPoint(GeoPoint(38.5, -120.2), points[0])
        assertPoint(GeoPoint(40.7, -120.95), points[1])
        assertPoint(GeoPoint(43.252, -126.453), points[2])
    }

    @Test
    fun `respeta la precision indicada`() {
        val points = PolylineDecoder.decode("_p~iF~ps|U", precision = 6)

        assertPoint(GeoPoint(3.85, -12.02), points.single())
    }

    @Test
    fun `una cadena vacia no tiene puntos`() {
        assertTrue(PolylineDecoder.decode("").isEmpty())
    }

    @Test
    fun `una cadena truncada descarta el punto incompleto`() {
        val points = PolylineDecoder.decode("_p~iF~ps|U_ulL")

        assertEquals(1, points.size)
        assertPoint(GeoPoint(38.5, -120.2), points[0])
    }
}
