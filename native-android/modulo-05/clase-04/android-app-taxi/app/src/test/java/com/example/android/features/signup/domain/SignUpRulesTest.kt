package com.example.android.features.signup.domain

import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.usecase.hasValidEmail
import com.example.android.features.signup.domain.usecase.hasValidNames
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpRulesTest {

    @Test
    fun `nombres y apellidos necesitan al menos 2 caracteres sin contar espacios`() {
        assertTrue(SignUpDraft(givenName = "Jo", familyName = "Li").hasValidNames())
        assertFalse(SignUpDraft(givenName = "J ", familyName = "Sandoval").hasValidNames())
        assertFalse(SignUpDraft(givenName = "Jorge", familyName = "").hasValidNames())
    }

    @Test
    fun `el correo debe tener usuario, arroba y dominio`() {
        assertTrue(SignUpDraft(email = "jorge@example.com").hasValidEmail())
        assertTrue(SignUpDraft(email = " jorge@example.com ").hasValidEmail())
        assertFalse(SignUpDraft(email = "jorge@example").hasValidEmail())
        assertFalse(SignUpDraft(email = "jorge example@mail.com").hasValidEmail())
        assertFalse(SignUpDraft(email = "").hasValidEmail())
    }
}
