package com.example.android.features.signin.domain.usecase

import com.example.android.core.domain.DomainException
import com.example.android.features.signin.FakeAuthRepository
import com.example.android.features.signin.FakeSessionStore
import com.example.android.features.signin.validResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpValidateUseCaseTest {

    @Test
    fun `pasajero activo guarda la sesion y no pide registro`() = runTest {
        val repo = FakeAuthRepository()
        val session = FakeSessionStore()

        val states = OtpValidateUseCase(repo, session)("987654321", "1234").toList()

        assertEquals(listOf(OtpValidateState.Loading, OtpValidateState.Success(showRegister = false)), states)
        assertEquals(listOf("51987654321" to "1234"), repo.validatedWith)
        assertEquals("access", session.currentAccess())
        assertEquals("refresh", session.currentRefresh())
        assertFalse(session.isRegistrationPending())
    }

    @Test
    fun `pasajero nuevo pide registro`() = runTest {
        val repo = FakeAuthRepository(onValidate = { phone, _ -> validResult(phone, "INACTIVE_REGISTER") })
        val session = FakeSessionStore()

        val last = OtpValidateUseCase(repo, session)("987654321", "1234").toList().last()

        assertEquals(OtpValidateState.Success(showRegister = true), last)
        assertTrue(session.isRegistrationPending())
    }

    @Test
    fun `codigo invalido emite Error y no guarda la sesion`() = runTest {
        val repo = FakeAuthRepository(onValidate = { _, _ ->
            throw DomainException.ValidationException("El código OTP es inválido o ha expirado.")
        })
        val session = FakeSessionStore()

        val last = OtpValidateUseCase(repo, session)("987654321", "0000").toList().last()

        assertEquals(OtpValidateState.Error("El código OTP es inválido o ha expirado."), last)
        assertNull(session.currentAccess())
    }
}
