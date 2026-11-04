package com.example.driver.features.signin

import com.example.driver.core.domain.DomainException
import com.example.driver.features.home.FakeDriverStore
import com.example.driver.features.home.FakeSessionStore
import com.example.driver.features.home.driver
import com.example.driver.features.signin.domain.model.LoginResult
import com.example.driver.features.signin.domain.repository.DriverAuthRepository
import com.example.driver.features.signin.domain.usecase.LoginState
import com.example.driver.features.signin.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private class FakeDriverAuthRepository(
        private val onLogin: suspend (String, String) -> LoginResult
    ) : DriverAuthRepository {
        val calls = mutableListOf<Pair<String, String>>()

        override suspend fun login(phone: String, password: String): LoginResult {
            calls += phone to password
            return onLogin(phone, password)
        }
    }

    private val session = FakeSessionStore()
    private val store = FakeDriverStore(online = true, driver = null)

    @Test
    fun `login agrega el prefijo 51 y guarda tokens y perfil`() = runTest {
        val repo = FakeDriverAuthRepository { _, _ -> LoginResult("access", "refresh", driver()) }

        val states = LoginUseCase(repo, session, store)("900000001", "Driver123*").toList()

        assertEquals(listOf(LoginState.Loading, LoginState.Success("Carlos")), states)
        assertEquals(listOf("51900000001" to "Driver123*"), repo.calls)
        assertEquals("access", session.currentAccess())
        assertEquals("refresh", session.currentRefresh())
        assertEquals("ABC-123", store.currentDriver()?.vehicle?.plate)
        assertFalse(store.isOnline())
    }

    @Test
    fun `credenciales invalidas muestran el mensaje del backend`() = runTest {
        val repo = FakeDriverAuthRepository { _, _ ->
            throw DomainException.UnauthorizedException("Teléfono o contraseña incorrectos")
        }

        val states = LoginUseCase(repo, session, store)("900000001", "mala").toList()

        assertEquals(LoginState.Error("Teléfono o contraseña incorrectos"), states.last())
        assertNull(session.currentAccess())
    }

    @Test
    fun `solo se puede enviar con 9 digitos y contrasena`() {
        assertTrue(LoginUseCase.canSubmit("900000001", "x"))
        assertFalse(LoginUseCase.canSubmit("90000000", "x"))
        assertFalse(LoginUseCase.canSubmit("900000001", " "))
    }
}
