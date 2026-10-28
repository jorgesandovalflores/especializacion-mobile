package com.example.android.features.signup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.TextInputField
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.usecase.EMAIL_MAX_LENGTH
import com.example.android.features.signup.domain.usecase.SignUpState
import com.example.android.features.signup.domain.usecase.hasValidEmail
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3_000L

@Composable
fun SignUpStep2Route(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SignUpViewModel = hiltViewModel()
) {
    val state by vm.signUpUi.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            is SignUpState.Success -> {
                vm.clearSignUpState()
                onFinish()
            }
            is SignUpState.Error -> {
                delay(TOAST_DURATION_MS)
                vm.clearSignUpState()
            }
            else -> Unit
        }
    }

    SignUpStep2Screen(
        draft = vm.draft,
        state = state,
        onEmailChange = vm::onEmailChange,
        onSubmit = vm::callSignUp,
        modifier = modifier
    )
}

@Composable
fun SignUpStep2Screen(
    draft: SignUpDraft?,
    state: SignUpState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loaded = draft != null
    val current = draft ?: SignUpDraft()
    val loading = state is SignUpState.Loading
    val toast = (state as? SignUpState.Error)?.let { ToastMessage(ToastType.Error, it.message) }

    NavigationBarStyle(darkIcons = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
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
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SignUpStepHeader(
                    step = 2,
                    title = "Información de contacto",
                    subtitle = if (current.givenName.isBlank()) "¿A qué correo te escribimos?"
                    else "${current.givenName.trim()}, ¿a qué correo te escribimos?",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextInputField(
                    value = current.email,
                    onValueChange = onEmailChange,
                    placeholder = "Ingresa tu correo electrónico",
                    enabled = loaded && !loading,
                    maxLength = EMAIL_MAX_LENGTH,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            }

            PrimaryButton(
                text = "Finalizar registro",
                onClick = onSubmit,
                enabled = loaded && current.hasValidEmail(),
                loading = loading,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        ToastHost(
            toast = toast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private val previewDraft = SignUpDraft(givenName = "Jorge", familyName = "Sandoval", email = "jorge@example.com")

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep2ScreenPreviewEmpty() {
    SignUpStep2Screen(
        draft = previewDraft.copy(email = ""),
        state = SignUpState.Idle,
        onEmailChange = {},
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep2ScreenPreviewFilled() {
    SignUpStep2Screen(draft = previewDraft, state = SignUpState.Idle, onEmailChange = {}, onSubmit = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep2ScreenPreviewLoading() {
    SignUpStep2Screen(draft = previewDraft, state = SignUpState.Loading, onEmailChange = {}, onSubmit = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep2ScreenPreviewError() {
    SignUpStep2Screen(
        draft = previewDraft,
        state = SignUpState.Error("El correo ya está registrado por otro pasajero."),
        onEmailChange = {},
        onSubmit = {}
    )
}
