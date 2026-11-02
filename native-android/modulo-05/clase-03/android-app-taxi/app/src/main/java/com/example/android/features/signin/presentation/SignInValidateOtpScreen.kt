package com.example.android.features.signin.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.R
import com.example.android.commons.presentation.COMPACT_MAX_HEIGHT
import com.example.android.commons.presentation.CONTENT_MAX_WIDTH
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.OtpCodeInput
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.commons.presentation.formatPeruPhone
import com.example.android.commons.presentation.shake
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import com.example.android.features.signin.domain.usecase.OtpValidateState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

private const val OTP_LENGTH = 4

private enum class ResendUi { Sending, Countdown, Available }

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
    NavigationBarStyle()

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

    val shakeOffset = remember { Animatable(0f) }
    val shakeDistance = with(LocalDensity.current) { 12.dp.toPx() }
    LaunchedEffect(validateState) {
        if (validateState is OtpValidateState.Error) shakeOffset.shake(shakeDistance)
    }

    val isExpired = remaining <= 0
    val isValidating = validateState is OtpValidateState.Loading
    val isResending = generateState is OtpGenerateState.Loading
    val canValidate = code.length == OTP_LENGTH && !isExpired && !isResending
    val formattedPhone = formatPeruPhone(phone)

    val toast = when {
        validateState is OtpValidateState.Error -> ToastMessage(ToastType.Error, validateState.message)
        generateState is OtpGenerateState.Error -> ToastMessage(ToastType.Error, generateState.message)
        generateState is OtpGenerateState.Success ->
            ToastMessage(ToastType.Success, stringResource(R.string.signin_toast_resent))
        showSentToast -> ToastMessage(ToastType.Success, stringResource(R.string.signin_toast_sent, formattedPhone))
        else -> null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val topPadding = if (maxHeight < COMPACT_MAX_HEIGHT) 24.dp else 88.dp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.signin_code_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = pluralStringResource(R.plurals.signin_code_sent, OTP_LENGTH, OTP_LENGTH, formattedPhone),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OtpCodeInput(
                    d0 = d0, onD0 = { d0 = it },
                    d1 = d1, onD1 = { d1 = it },
                    d2 = d2, onD2 = { d2 = it },
                    d3 = d3, onD3 = { d3 = it },
                    modifier = Modifier.graphicsLayer { translationX = shakeOffset.value }
                )

                val resendUi = when {
                    isResending -> ResendUi.Sending
                    !isExpired -> ResendUi.Countdown
                    else -> ResendUi.Available
                }
                AnimatedContent(
                    targetState = resendUi,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                    },
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier.fillMaxWidth(),
                    label = "resend"
                ) { current ->
                    when (current) {
                        ResendUi.Sending -> Text(
                            text = stringResource(R.string.signin_resending),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ResendUi.Countdown -> Text(
                            text = stringResource(R.string.signin_resend_countdown, formatAsMmSs(remaining)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ResendUi.Available -> Text(
                            text = stringResource(R.string.signin_resend),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(
                                    enabled = !isValidating,
                                    onClickLabel = stringResource(R.string.signin_resend_action),
                                    role = Role.Button,
                                    onClick = onResend
                                )
                        )
                    }
                }
            }

            PrimaryButton(
                text = stringResource(R.string.signin_validate),
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
    AndroidTheme {
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
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewLoading() {
    AndroidTheme {
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
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewError() {
    AndroidTheme {
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
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInValidateOtpScreenPreviewExpired() {
    AndroidTheme {
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
}

@Preview(name = "Fuente 200 %", showBackground = true, fontScale = 2f, heightDp = 900)
@Composable
private fun SignInValidateOtpScreenPreviewLargeFont() {
    AndroidTheme {
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
}
