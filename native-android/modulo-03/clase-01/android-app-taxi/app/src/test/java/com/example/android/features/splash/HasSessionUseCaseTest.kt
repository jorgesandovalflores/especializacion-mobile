package com.example.android.features.splash

import com.example.android.features.signin.FakeSessionStore
import com.example.android.features.splash.domain.usecase.HasSessionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasSessionUseCaseTest {

    @Test
    fun `sin token guardado no hay sesion`() = runTest {
        assertFalse(HasSessionUseCase(FakeSessionStore())())
    }

    @Test
    fun `con token guardado hay sesion`() = runTest {
        val session = FakeSessionStore().apply { saveTokens("access", "refresh") }

        assertTrue(HasSessionUseCase(session)())
    }
}
