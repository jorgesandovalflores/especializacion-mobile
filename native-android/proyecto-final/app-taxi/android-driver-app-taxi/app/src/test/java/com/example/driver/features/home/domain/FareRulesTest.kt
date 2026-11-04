package com.example.driver.features.home.domain

import com.example.driver.core.domain.DomainException
import com.example.driver.features.home.FakeTripRepository
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.rules.FareRules
import com.example.driver.features.home.domain.rules.TripFlow
import com.example.driver.features.home.domain.usecase.SendOfferUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FareRulesTest {

    @Test
    fun `el precio inicial es la tarifa estimada`() {
        assertEquals(19.76, FareRules.initial(19.76), 0.0)
    }

    @Test
    fun `el precio inicial nunca baja del minimo`() {
        assertEquals(1.0, FareRules.initial(0.4), 0.0)
    }

    @Test
    fun `mas y menos cambian de 50 centimos sin errores de redondeo`() {
        var price = 19.76
        repeat(3) { price = FareRules.increase(price) }
        assertEquals(21.26, price, 0.0)

        price = FareRules.decrease(price)
        assertEquals(20.76, price, 0.0)

        assertEquals(0.6, FareRules.increase(0.1), 0.0)
    }

    @Test
    fun `menos se detiene en S 1_00`() {
        assertEquals(1.0, FareRules.decrease(1.3), 0.0)
        assertEquals(1.0, FareRules.decrease(1.0), 0.0)
        assertFalse(FareRules.canDecrease(1.0))
        assertTrue(FareRules.canDecrease(1.5))
    }

    @Test
    fun `una oferta por debajo del minimo no llega al backend`() = runTest {
        val repo = FakeTripRepository()

        val error = runCatching { SendOfferUseCase(repo)("t1", 0.5) }.exceptionOrNull()

        assertTrue(error is DomainException.ValidationException)
        assertTrue(repo.offers.isEmpty())
    }

    @Test
    fun `el flujo del viaje sigue el orden del contrato`() {
        assertEquals(TripStatus.ARRIVED, TripFlow.next(TripStatus.ACCEPTED))
        assertEquals(TripStatus.IN_PROGRESS, TripFlow.next(TripStatus.ARRIVED))
        assertEquals(TripStatus.COMPLETED, TripFlow.next(TripStatus.IN_PROGRESS))
        assertNull(TripFlow.next(TripStatus.COMPLETED))
        assertNull(TripFlow.next(TripStatus.REQUESTED))
    }
}
