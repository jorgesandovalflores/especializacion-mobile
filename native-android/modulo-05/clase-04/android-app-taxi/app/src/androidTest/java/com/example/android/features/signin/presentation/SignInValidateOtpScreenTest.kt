package com.example.android.features.signin.presentation

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.R
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import com.example.android.features.signin.domain.usecase.OtpValidateState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SignInValidateOtpScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun anExpiredCodeOffersResendAsAButton() {
        var resent = false
        rule.setContent {
            AndroidTheme {
                SignInValidateOtpScreen(
                    phone = "987654321",
                    expiresAt = Instant.now().minusSeconds(1).toString(),
                    validateState = OtpValidateState.Idle,
                    generateState = OtpGenerateState.Idle,
                    showSentToast = false,
                    onValidate = {},
                    onResend = { resent = true }
                )
            }
        }

        rule.onNodeWithText(context.getString(R.string.signin_validate)).assertIsNotEnabled()
        rule.onNodeWithText(context.getString(R.string.signin_resend))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()

        assertTrue(resent)
    }
}
