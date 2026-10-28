package com.example.android.features.splash

import com.example.android.features.signin.FakeSessionStore
import com.example.android.features.splash.domain.usecase.GetStartDestinationUseCase
import com.example.android.features.splash.domain.usecase.StartDestination
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetStartDestinationUseCaseTest {

    @Test
    fun `sin token guardado va a iniciar sesion`() = runTest {
        assertEquals(StartDestination.SignIn, GetStartDestinationUseCase(FakeSessionStore())())
    }

    @Test
    fun `con token y registro pendiente retoma el registro`() = runTest {
        val session = FakeSessionStore().apply {
            saveTokens("access", "refresh")
            setRegistrationPending(true)
        }

        assertEquals(StartDestination.SignUp, GetStartDestinationUseCase(session)())
    }

    @Test
    fun `con token y registro completo va a Home`() = runTest {
        val session = FakeSessionStore().apply { saveTokens("access", "refresh") }

        assertEquals(StartDestination.Home, GetStartDestinationUseCase(session)())
    }
}
