package com.example.android.commons.presentation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class WidthClassTest {

    @Test
    fun `phone in portrait is compact`() {
        assertEquals(WidthClass.Compact, widthClassOf(360.dp))
        assertEquals(WidthClass.Compact, widthClassOf(599.dp))
    }

    @Test
    fun `phone in landscape and small tablet are medium`() {
        assertEquals(WidthClass.Medium, widthClassOf(600.dp))
        assertEquals(WidthClass.Medium, widthClassOf(800.dp))
        assertEquals(WidthClass.Medium, widthClassOf(839.dp))
    }

    @Test
    fun `tablet in landscape is expanded`() {
        assertEquals(WidthClass.Expanded, widthClassOf(840.dp))
        assertEquals(WidthClass.Expanded, widthClassOf(1280.dp))
    }
}
