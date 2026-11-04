package com.example.android.features.signin.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.R
import com.example.android.core.domain.DomainException
import com.example.android.core.domain.SessionStore
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.features.signin.domain.model.OtpGenerateResult
import com.example.android.features.signin.domain.model.OtpValidateResult
import com.example.android.features.signin.domain.repository.AuthRepository
import com.example.android.features.signin.domain.usecase.OtpGenerateUseCase
import com.example.android.features.signin.domain.usecase.OtpValidateUseCase
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInGenerateOtpRouteTest {

    @get:Rule
    val rule = createComposeRule()

    private val submit = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.signin_submit)

    private fun setRoute(repo: AuthRepository, onGoValidate: (String, String) -> Unit): SignInViewModel {
        val vm = SignInViewModel(OtpGenerateUseCase(repo), OtpValidateUseCase(repo, NoSessionStore))
        rule.setContent {
            AndroidTheme { SignInGenerateOtpRoute(onGoValidate = onGoValidate, vm = vm) }
        }
        rule.onNode(hasSetTextAction()).performTextInput("987654321")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithText(submit).performClick()
        return vm
    }

    @Test
    fun aGeneratedCodeNavigatesToValidate() {
        val repo = FakeGenerateRepository { OtpGenerateResult("2026-11-02T15:00:00Z") }
        var destination: Pair<String, String>? = null

        setRoute(repo) { phone, expiresAt -> destination = phone to expiresAt }
        rule.waitUntil(timeoutMillis = 5_000) { destination != null }

        assertEquals("987654321" to "2026-11-02T15:00:00Z", destination)
        assertEquals(listOf("51987654321"), repo.generatedFor)
    }

    @Test
    fun aRejectedRequestShowsTheMessageForThreeSecondsAndStays() {
        val answer = CompletableDeferred<Unit>()
        val repo = FakeGenerateRepository {
            answer.await()
            throw DomainException.ValidationException("Ya existe un código activo.")
        }
        var destination: Pair<String, String>? = null
        val vm = setRoute(repo) { phone, expiresAt -> destination = phone to expiresAt }

        rule.mainClock.autoAdvance = false
        answer.complete(Unit)
        rule.waitUntil(timeoutMillis = 5_000) { vm.generateOtpUi.value is OtpGenerateState.Error }

        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("Ya existe un código activo.").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(3_000)
        rule.mainClock.autoAdvance = true
        rule.onNodeWithText("Ya existe un código activo.").assertDoesNotExist()
        assertNull(destination)
    }
}

private class FakeGenerateRepository(
    private val onGenerate: suspend (String) -> OtpGenerateResult
) : AuthRepository {
    val generatedFor = mutableListOf<String>()

    override suspend fun otpGenerate(phone: String): OtpGenerateResult {
        generatedFor += phone
        return onGenerate(phone)
    }

    override suspend fun otpValidate(phone: String, code: String): OtpValidateResult = error("No se usa")
}

private object NoSessionStore : SessionStore {
    override suspend fun saveTokens(access: String, refresh: String) = Unit
    override fun accessToken(): Flow<String?> = flowOf(null)
    override fun refreshToken(): Flow<String?> = flowOf(null)
    override suspend fun setRegistrationPending(pending: Boolean) = Unit
    override fun registrationPending(): Flow<Boolean> = flowOf(false)
    override suspend fun clear() = Unit
}
