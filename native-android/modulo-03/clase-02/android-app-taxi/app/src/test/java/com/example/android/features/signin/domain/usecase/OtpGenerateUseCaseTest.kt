package com.example.android.features.signin.domain.usecase

import com.example.android.core.domain.DomainException
import com.example.android.features.signin.FakeAuthRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OtpGenerateUseCaseTest {

    @Test
    fun `emite Loading y Success y envia el telefono con prefijo de pais`() = runTest {
        val repo = FakeAuthRepository()

        val states = OtpGenerateUseCase(repo)("987654321").toList()

        assertEquals(
            listOf(
                OtpGenerateState.Loading,
                OtpGenerateState.Success(phone = "987654321", expiresAt = "2026-09-28T15:02:00Z")
            ),
            states
        )
        assertEquals(listOf("51987654321"), repo.generatedFor)
    }

    @Test
    fun `un error de dominio se emite como Error con su mensaje`() = runTest {
        val repo = FakeAuthRepository(onGenerate = {
            throw DomainException.ValidationException("Ya existe un código activo.")
        })

        val last = OtpGenerateUseCase(repo)("987654321").toList().last()

        assertEquals(OtpGenerateState.Error("Ya existe un código activo."), last)
    }

    @Test
    fun `un error no tipado se emite con un mensaje generico`() = runTest {
        val repo = FakeAuthRepository(onGenerate = { error("boom") })

        val last = OtpGenerateUseCase(repo)("987654321").toList().last()

        assertEquals(OtpGenerateState.Error("Ocurrió un error inesperado"), last)
    }
}
