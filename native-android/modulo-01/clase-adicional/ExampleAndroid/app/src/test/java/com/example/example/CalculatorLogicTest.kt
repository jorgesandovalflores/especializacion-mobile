package com.example.example

import com.example.example.calculator.CalcFormatter
import com.example.example.calculator.CalcResult
import com.example.example.calculator.Operator
import com.example.example.calculator.calculate
import com.example.example.calculator.historySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas locales (host, sin emulador) sobre la lógica de la calculadora
 * en calculator/CalculatorLogic.kt.
 */
class CalculatorLogicTest {

    @Test
    fun `suma dos numeros validos`() {
        val result = calculate("2", "3", Operator.SUMA)
        assertTrue(result is CalcResult.Ok)
        assertEquals(5.0, (result as CalcResult.Ok).operation.result, 0.0001)
    }

    @Test
    fun `dividir entre cero devuelve Error, no una excepcion`() {
        val result = calculate("10", "0", Operator.DIVISION)
        assertTrue(result is CalcResult.Error)
    }

    @Test
    fun `texto invalido devuelve Error gracias al null safety de toDoubleOrNull`() {
        val result = calculate("abc", "3", Operator.SUMA)
        assertTrue(result is CalcResult.Error)
    }

    @Test
    fun `historySummary resume filter, map y sumOf sobre el historial`() {
        val a = (calculate("4", "2", Operator.SUMA) as CalcResult.Ok).operation
        val b = (calculate("8", "4", Operator.DIVISION) as CalcResult.Ok).operation
        val summary = historySummary(listOf(a, b))
        assertTrue(summary.contains("Operaciones: 2"))
        assertTrue(summary.contains("divisiones: 1"))
    }

    @Test
    fun `CalcFormatter percent calcula el porcentaje con JvmOverloads`() {
        assertEquals("10.0%", CalcFormatter.percent(20.0, 200.0))
        assertEquals("10%", CalcFormatter.percent(20.0, 200.0, 0))
    }
}
