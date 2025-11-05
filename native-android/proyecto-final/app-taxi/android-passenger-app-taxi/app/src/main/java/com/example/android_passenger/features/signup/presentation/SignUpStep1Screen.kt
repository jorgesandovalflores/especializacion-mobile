package com.example.android_passenger.features.signup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android_passenger.commons.presentation.GenericInputField
import com.example.android_passenger.commons.presentation.NavigationBarStyle
import com.example.android_passenger.commons.presentation.PrimaryButton
import com.example.android_passenger.features.signup.domain.model.SignUpModelStep1
import com.example.android_passenger.features.signup.domain.usecase.GetSignUpStep1UseCaseState

@Composable
fun SignUpStep1Route(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val step1State by viewModel.getStep1.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.callGetStep1()
    }

    LaunchedEffect(step1State) {
        when (step1State) {
            is GetSignUpStep1UseCaseState.Success -> {
            }
            else -> Unit
        }
    }

    SignUpStep1Screen(
        step1State = step1State,
        onGivenNameChange = { givenName ->
        },
        onFamilyNameChange = { familyName ->
        },
        onNext = { givenName, familyName ->
            viewModel.callSaveStep1(
                SignUpModelStep1(
                    givenName = givenName,
                    familyName = familyName,
                    photoUrl = ""
                )
            )
            onNext()
        },
        modifier = modifier
    )
}

@Composable
fun SignUpStep1Screen(
    step1State: GetSignUpStep1UseCaseState,
    onGivenNameChange: (String) -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onNext: (givenName: String, familyName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(0xFFF8F9FA)
    NavigationBarStyle(color = Color.White, darkIcons = true)

    var givenName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }

    LaunchedEffect(step1State) {
        when (step1State) {
            is GetSignUpStep1UseCaseState.Success -> {
                val data = step1State.data
                givenName = data.givenName ?: ""
                familyName = data.familyName ?: ""
            }
            else -> Unit
        }
    }

    val isLoading = step1State is GetSignUpStep1UseCaseState.Loading
    val isValid = givenName.isNotBlank() && familyName.isNotBlank() && !isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Información personal",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "Completa tus datos para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                GenericInputField(
                    value = givenName,
                    onValueChange = {
                        givenName = it
                        onGivenNameChange(it)
                    },
                    placeholder = "Ingresa tus nombres",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    maxLength = 50,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    enabled = !isLoading
                )

                GenericInputField(
                    value = familyName,
                    onValueChange = {
                        familyName = it
                        onFamilyNameChange(it)
                    },
                    placeholder = "Ingresa tus apellidos",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    maxLength = 50,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    enabled = !isLoading
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Continuar",
                onClick = { onNext(givenName, familyName) },
                enabled = isValid,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ===== Previews ===== */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview_Empty() {
    SignUpStep1Screen(
        step1State = GetSignUpStep1UseCaseState.Success(
            SignUpModelStep1("", "", "")
        ),
        onGivenNameChange = {},
        onFamilyNameChange = {},
        onNext = { _, _ -> }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview_Filled() {
    SignUpStep1Screen(
        step1State = GetSignUpStep1UseCaseState.Success(
            SignUpModelStep1("Juan", "Pérez", "")
        ),
        onGivenNameChange = {},
        onFamilyNameChange = {},
        onNext = { _, _ -> }
    )
}
