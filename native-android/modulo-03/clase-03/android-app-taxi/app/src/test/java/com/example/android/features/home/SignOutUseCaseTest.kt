package com.example.android.features.home

import com.example.android.core.domain.LocalCache
import com.example.android.features.home.domain.usecase.SignOutUseCase
import com.example.android.features.signin.FakeSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SignOutUseCaseTest {

    @Test
    fun `cerrar sesion borra los tokens y la cache local`() = runTest {
        val session = FakeSessionStore().apply { saveTokens("access", "refresh") }
        var cacheClears = 0

        SignOutUseCase(session, LocalCache { cacheClears++ })()

        assertNull(session.currentAccess())
        assertEquals(1, cacheClears)
    }
}
