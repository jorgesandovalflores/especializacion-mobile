package com.example.driver.features.home.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class TripFormatTest {

    @Test
    fun `formatea montos en soles con dos decimales`() {
        assertEquals("S/ 19.76", formatMoney(19.76))
        assertEquals("S/ 1.00", formatMoney(1.0))
    }

    @Test
    fun `formatea distancia en metros o kilometros`() {
        assertEquals("850 m", formatDistance(850))
        assertEquals("9.8 km", formatDistance(9_838))
    }

    @Test
    fun `formatea la duracion en minutos con minimo de uno`() {
        assertEquals("13 min", formatDuration(787))
        assertEquals("1 min", formatDuration(10))
    }
}
