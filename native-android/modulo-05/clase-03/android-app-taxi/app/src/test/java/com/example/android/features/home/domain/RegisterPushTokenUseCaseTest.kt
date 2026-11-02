package com.example.android.features.home.domain

import com.example.android.core.domain.DomainException
import com.example.android.features.home.FakePushTokenRepository
import com.example.android.features.home.domain.usecase.RegisterPushTokenUseCase
import com.example.android.features.signin.FakeSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterPushTokenUseCaseTest {

    private val repo = FakePushTokenRepository()
    private val session = FakeSessionStore()
    private val useCase = RegisterPushTokenUseCase(repo, session)

    @Test
    fun `sin sesion no registra nada`() = runTest {
        assertFalse(useCase())
        assertEquals(emptyList<String>(), repo.registered)
    }

    @Test
    fun `con sesion registra el token actual de Firebase`() = runTest {
        session.saveTokens("access", "refresh")

        assertTrue(useCase())
        assertEquals(listOf("fcm-token"), repo.registered)
    }

    @Test
    fun `un token nuevo de onNewToken se registra tal cual`() = runTest {
        session.saveTokens("access", "refresh")

        useCase("token-nuevo")

        assertEquals(listOf("token-nuevo"), repo.registered)
    }

    @Test
    fun `sin Firebase configurado no hay token y no falla`() = runTest {
        session.saveTokens("access", "refresh")
        repo.token = null

        assertFalse(useCase())
    }

    @Test
    fun `si el backend falla devuelve false sin lanzar`() = runTest {
        session.saveTokens("access", "refresh")
        repo.failure = DomainException.NetworkException("Sin conexión")

        assertFalse(useCase())
    }
}
