package com.example.android.features.signup.domain

import com.example.android.core.domain.DomainException
import com.example.android.features.signin.FakeSessionStore
import com.example.android.features.signup.FakeSignUpDraftStore
import com.example.android.features.signup.FakeSignUpRepository
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.usecase.SignUpState
import com.example.android.features.signup.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpUseCaseTest {

    private val draft = SignUpDraft(givenName = " Jorge ", familyName = "Sandoval", email = "jorge@example.com ")

    @Test
    fun `envia el borrador guardado, marca el registro como completo y borra el borrador`() = runTest {
        val repo = FakeSignUpRepository()
        val store = FakeSignUpDraftStore(draft)
        val session = FakeSessionStore().apply { setRegistrationPending(true) }

        val states = SignUpUseCase(repo, store, session)().toList()

        assertEquals(listOf(SignUpState.Loading, SignUpState.Success), states)
        assertEquals(listOf(Triple("Jorge", "Sandoval", "jorge@example.com")), repo.requests)
        assertFalse(session.isRegistrationPending())
        assertEquals(SignUpDraft(), store.current)
    }

    @Test
    fun `si el backend rechaza el correo emite Error y conserva el borrador`() = runTest {
        val repo = FakeSignUpRepository(onSignUp = { _, _, _ ->
            throw DomainException.ValidationException("El correo ya está registrado por otro pasajero.")
        })
        val store = FakeSignUpDraftStore(draft)
        val session = FakeSessionStore().apply { setRegistrationPending(true) }

        val last = SignUpUseCase(repo, store, session)().toList().last()

        assertEquals(SignUpState.Error("El correo ya está registrado por otro pasajero."), last)
        assertTrue(session.isRegistrationPending())
        assertEquals(draft, store.current)
    }
}
