package com.example.android.commons.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.R
import com.example.android.core.presentation.theme.AndroidTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtpCodeInputSemanticsTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun digit(position: Int) = context.getString(R.string.common_otp_digit, position, 4)

    @Test
    fun eachBoxIsDescribedAndFocusMovesForward() {
        rule.setContent { AndroidTheme { OtpInputHost() } }

        (1..4).forEach { rule.onNodeWithContentDescription(digit(it)).assertExists() }
        rule.onNodeWithContentDescription(digit(1)).performTextInput("7")
        rule.onNodeWithContentDescription(digit(2)).assertIsFocused()
    }
}

@Composable
private fun OtpInputHost() {
    var d0 by remember { mutableStateOf("") }
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var d3 by remember { mutableStateOf("") }
    OtpCodeInput(
        d0 = d0, onD0 = { d0 = it },
        d1 = d1, onD1 = { d1 = it },
        d2 = d2, onD2 = { d2 = it },
        d3 = d3, onD3 = { d3 = it }
    )
}
