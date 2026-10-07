package com.example.android.features.signup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.TextInputField
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.usecase.NAME_MAX_LENGTH
import com.example.android.features.signup.domain.usecase.hasValidNames

@Composable
fun SignUpStep1Route(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SignUpViewModel = hiltViewModel()
) {

    SignUpStep1Screen(
        draft = vm.draft,
        onNamesChange = vm::onNamesChange,
        onNext = onNext,
        modifier = modifier
    )
}

@Composable
fun SignUpStep1Screen(
    draft: SignUpDraft?,
    onNamesChange: (givenName: String, familyName: String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loaded = draft != null
    val current = draft ?: SignUpDraft()

    NavigationBarStyle(darkIcons = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
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
                step = 1,
                title = "Información personal",
                subtitle = "Completa tus datos para continuar",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextInputField(
                value = current.givenName,
                onValueChange = { onNamesChange(it, current.familyName) },
                placeholder = "Ingresa tus nombres",
                enabled = loaded,
                maxLength = NAME_MAX_LENGTH,
                capitalization = KeyboardCapitalization.Words
            )
            TextInputField(
                value = current.familyName,
                onValueChange = { onNamesChange(current.givenName, it) },
                placeholder = "Ingresa tus apellidos",
                enabled = loaded,
                maxLength = NAME_MAX_LENGTH,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
        }

        PrimaryButton(
            text = "Continuar",
            onClick = onNext,
            enabled = loaded && current.hasValidNames(),
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep1ScreenPreviewEmpty() {
    SignUpStep1Screen(draft = SignUpDraft(), onNamesChange = { _, _ -> }, onNext = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun SignUpStep1ScreenPreviewRestored() {
    SignUpStep1Screen(
        draft = SignUpDraft(givenName = "Jorge", familyName = "Sandoval"),
        onNamesChange = { _, _ -> },
        onNext = {}
    )
}
