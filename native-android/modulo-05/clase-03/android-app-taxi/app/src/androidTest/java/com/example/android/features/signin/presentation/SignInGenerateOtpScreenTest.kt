package com.example.android.features.signin.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.R
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInGenerateOtpScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val submit = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.signin_submit)

    private fun setScreen(
        state: OtpGenerateState = OtpGenerateState.Idle,
        initialPhone: String = "",
        onSubmit: (String) -> Unit = {}
    ) {
        rule.setContent {
            AndroidTheme {
                var phone by remember { mutableStateOf(initialPhone) }
                SignInGenerateOtpScreen(
                    state = state,
                    phone = phone,
                    onPhoneChange = { phone = it },
                    onSubmit = onSubmit
                )
            }
        }
    }

    @Test
    fun theButtonIsEnabledOnlyWithNineDigits() {
        setScreen()

        rule.onNodeWithText(submit).assertIsNotEnabled()
        rule.onNode(hasSetTextAction()).performTextInput("98765432")
        rule.onNodeWithText(submit).assertIsNotEnabled()
        rule.onNode(hasSetTextAction()).performTextInput("1")
        rule.onNodeWithText(submit).assertIsEnabled()
    }

    @Test
    fun submittingSendsTheTypedPhone() {
        var sent: String? = null
        setScreen(initialPhone = "987654321", onSubmit = { sent = it })

        rule.onNodeWithText(submit).performClick()

        assertEquals("987654321", sent)
    }

    @Test
    fun anErrorStateShowsItsMessage() {
        setScreen(state = OtpGenerateState.Error("Ya existe un código activo."), initialPhone = "987654321")

        rule.onNodeWithText("Ya existe un código activo.").assertIsDisplayed()
    }

    @Test
    fun whileLoadingThePhoneCannotBeEdited() {
        setScreen(state = OtpGenerateState.Loading, initialPhone = "987654321")

        rule.onNodeWithText("987654321").assertIsNotEnabled()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
        rule.onNodeWithText(submit).assertDoesNotExist()
    }

    @Test
    fun theTitleIsAHeadingForTalkBack() {
        setScreen()

        rule.onNode(isHeading()).assertIsDisplayed()
    }
}
