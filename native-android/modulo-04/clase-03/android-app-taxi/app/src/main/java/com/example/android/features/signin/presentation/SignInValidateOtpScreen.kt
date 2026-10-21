package com.example.android.features.signin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.OtpCodeInput
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.commons.presentation.formatPeruPhone
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import com.example.android.features.signin.domain.usecase.OtpValidateState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

private const val OTP_LENGTH = 4

@Composable
fun SignInValidateOtpRoute(
    phone: String,
    expiresAtUtcMillis: Long,
    onGoHome: () -> Unit,
    onGoSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SignInViewModel = hiltViewModel()
) {
    val validateState by vm.validateOtpUi.collectAsStateWithLifecycle()
    val generateState by vm.generateOtpUi.collectAsStateWithLifecycle()

    var expiresAt by rememberSaveable { mutableStateOf(Instant.ofEpochMilli(expiresAtUtcMillis).toString()) }
    var showSentToast by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(showSentToast) {
        if (showSentToast) {
            delay(TOAST_DURATION_MS)
            showSentToast = false
        }
    }

    LaunchedEffect(validateState) {
        when (val s = validateState) {
            is OtpValidateState.Success -> {
                vm.clearValidateState()
                if (s.showRegister) onGoSignUp() else onGoHome()
            }
            is OtpValidateState.Error -> {
                delay(TOAST_DURATION_MS)
                vm.clearValidateState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(generateState) {
        when (val g = generateState) {
            is OtpGenerateState.Success -> {
                expiresAt = g.expiresAt
                showSentToast = false
                delay(TOAST_DURATION_MS)
                vm.clearGenerateState()
            }
            is OtpGenerateState.Error -> {
                delay(TOAST_DURATION_MS)
                vm.clearGenerateState()
            }
            else -> Unit
        }
    }

    SignInValidateOtpScreen(
        phone = phone,
        expiresAt = expiresAt,
        validateState = validateState,
        generateState = generateState,
        showSentToast = showSentToast,
        onValidate = { code -> vm.callValidateOtp(phone, code) },
        onResend = { vm.callGenerateOtp(phone) },
        modifier = modifier
    )
}

@Composable
fun SignInValidateOtpScreen(
    phone: String,
    expiresAt: String,
    validateState: OtpValidateState,
    generateState: OtpGenerateState,
    showSentToast: Boolean,
    onValidate: (String) -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarStyle(darkIcons = true)

    var d0 by remember(expiresAt) { mutableStateOf("") }
    var d1 by remember(expiresAt) { mutableStateOf("") }
    var d2 by remember(expiresAt) { mutableStateOf("") }
    var d3 by remember(expiresAt) { mutableStateOf("") }
    val code = "$d0$d1$d2$d3"

    val target = remember(expiresAt) { runCatching { Instant.parse(expiresAt) }.getOrNull() }
    var remaining by remember(target) { mutableIntStateOf(secondsRemaining(target)) }
    LaunchedEffect(target) {
        while (remaining > 0) {
            delay(1_000L)
            remaining = secondsRemaining(target)
        }
    }

    val isExpired = remaining <= 0
    val isValidating = validateState is OtpValidateState.Loading
    val isResending = generateState is OtpGenerateState.Loading
    val canValidate = code.length == OTP_LENGTH && !isExpired && !isResending
    val formattedPhone = formatPeruPhone(phone)

    val toast = when {
        validateState is OtpValidateState.Error -> ToastMessage(ToastType.Error, validateState.message)
        generateState is OtpGenerateState.Error -> ToastMessage(ToastType.Error, generateState.message)
        generateState is OtpGenerateState.Success -> ToastMessage(ToastType.Success, "Te enviamos un nuevo código")
        showSentToast -> ToastMessage(ToastType.Success, "Enviamos un código al $formattedPhone")
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 88.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Ingresa el código",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0F0F)
                )
                Text(
                    text = "Hemos enviado un código de 4 dígitos al número $formattedPhone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4A4A4A)
                )

                OtpCodeInput(
                    d0 = d0, onD0 = { d0 = it },
                    d1 = d1, onD1 = { d1 = it },
                    d2 = d2, onD2 = { d2 = it },
                    d3 = d3, onD3 = { d3 = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    when {
                        isResending -> Text(
                            text = "Enviando un nuevo código…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B6B6B)
                        )
                        !isExpired -> Text(
                            text = "Puedes volver a enviar un código en ${formatAsMmSs(remaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B6B6B)
                        )
                        else -> Text(
                            text = "Reenviar código",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F0F0F),
                            modifier = Modifier.clickable(enabled = !isValidating, onClick = onResend)
                        )
                    }
                }
            }

            PrimaryButton(
                text = "Validar",
                onClick = { onValidate(code) },
                enabled = canValidate,
                loading = isValidating,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        ToastHost(
            toast = toast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private fun secondsRemaining(target: Instant?): Int {
    if (target == null) return 0
    return Duration.between(Instant.now(), target).seconds.coerceAtLeast(0).toInt()
}

private fun formatAsMmSs(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewCodeSent() {
    SignInValidateOtpScreen(
        phone = "987654321",
        expiresAt = Instant.now().plusSeconds(120).toString(),
        validateState = OtpValidateState.Idle,
        generateState = OtpGenerateState.Idle,
        showSentToast = true,
        onValidate = {},
        onResend = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewLoading() {
    SignInValidateOtpScreen(
        phone = "987654321",
        expiresAt = Instant.now().plusSeconds(60).toString(),
        validateState = OtpValidateState.Loading,
        generateState = OtpGenerateState.Idle,
        showSentToast = false,
        onValidate = {},
        onResend = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewError() {
    SignInValidateOtpScreen(
        phone = "987654321",
        expiresAt = Instant.now().plusSeconds(30).toString(),
        validateState = OtpValidateState.Error("El código OTP es inválido o ha expirado."),
        generateState = OtpGenerateState.Idle,
        showSentToast = false,
        onValidate = {},
        onResend = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewExpired() {
    SignInValidateOtpScreen(
        phone = "987654321",
        expiresAt = Instant.now().minusSeconds(1).toString(),
        validateState = OtpValidateState.Idle,
        generateState = OtpGenerateState.Idle,
        showSentToast = false,
        onValidate = {},
        onResend = {}
    )
}
