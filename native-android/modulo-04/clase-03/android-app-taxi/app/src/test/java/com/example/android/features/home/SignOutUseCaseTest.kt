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
    fun `cerrar sesion borra los tokens, la cache local y el token de push`() = runTest {
        val session = FakeSessionStore().apply { saveTokens("access", "refresh") }
        val push = FakePushTokenRepository()
        var cacheClears = 0

        SignOutUseCase(session, LocalCache { cacheClears++ }, push)()

        assertNull(session.currentAccess())
        assertEquals(1, cacheClears)
        assertEquals(1, push.deletes)
    }
}
