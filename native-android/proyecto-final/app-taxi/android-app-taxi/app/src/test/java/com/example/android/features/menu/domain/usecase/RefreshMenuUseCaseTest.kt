package com.example.android.features.menu.domain.usecase

import com.example.android.core.domain.DomainException
import com.example.android.features.menu.FakeMenuRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshMenuUseCaseTest {

    @Test
    fun `emite Loading y Success cuando la sincronizacion termina`() = runTest {
        val repo = FakeMenuRepository()

        val states = RefreshMenuUseCase(repo)().toList()

        assertEquals(listOf(RefreshMenuState.Loading, RefreshMenuState.Success), states)
        assertEquals(1, repo.refreshCalls)
    }

    @Test
    fun `un error de dominio se emite como Error con su mensaje`() = runTest {
        val repo = FakeMenuRepository(onRefresh = {
            throw DomainException.NetworkException("No se pudo conectar con el servidor. Revisa tu conexión")
        })

        val last = RefreshMenuUseCase(repo)().toList().last()

        assertEquals(RefreshMenuState.Error("No se pudo conectar con el servidor. Revisa tu conexión"), last)
    }

    @Test
    fun `un error no tipado se emite con un mensaje generico`() = runTest {
        val repo = FakeMenuRepository(onRefresh = { error("boom") })

        val last = RefreshMenuUseCase(repo)().toList().last()

        assertEquals(RefreshMenuState.Error("Ocurrió un error inesperado"), last)
    }
}
