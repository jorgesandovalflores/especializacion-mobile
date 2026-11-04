package com.example.android.features.signin.domain.usecase

import com.example.android.core.domain.DomainException
import com.example.android.core.domain.SessionStore
import com.example.android.features.signin.domain.repository.AuthRepository
import com.example.android.features.signin.validResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OtpValidateUseCaseMockTest {

    private val repo = mockk<AuthRepository>()
    private val session = mockk<SessionStore>(relaxUnitFun = true)

    @Test
    fun `guarda los tokens del backend y marca si falta el registro`() = runTest {
        coEvery { repo.otpValidate("51987654321", "1234") } returns validResult("51987654321", "ACTIVE")

        OtpValidateUseCase(repo, session)("987654321", "1234").toList()

        coVerify(exactly = 1) { session.saveTokens(access = "access", refresh = "refresh") }
        coVerify(exactly = 1) { session.setRegistrationPending(false) }
        confirmVerified(session)
    }

    @Test
    fun `si el codigo es invalido no toca la sesion`() = runTest {
        coEvery { repo.otpValidate(any(), any()) } throws
            DomainException.ValidationException("El código OTP es inválido o ha expirado.")

        val last = OtpValidateUseCase(repo, session)("987654321", "0000").toList().last()

        assertEquals(OtpValidateState.Error("El código OTP es inválido o ha expirado."), last)
        coVerify(exactly = 0) { session.saveTokens(any(), any()) }
        confirmVerified(session)
    }
}
