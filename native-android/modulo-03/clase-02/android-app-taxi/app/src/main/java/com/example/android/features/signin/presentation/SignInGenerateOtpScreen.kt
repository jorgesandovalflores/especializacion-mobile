package com.example.android.features.signin.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.R
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PhoneInputField
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import kotlinx.coroutines.delay

internal const val TOAST_DURATION_MS = 3_000L
private const val PHONE_LENGTH = 9

@Composable
fun SignInGenerateOtpRoute(
    onGoValidate: (phone: String, expiresAt: String) -> Unit,
    modifier: Modifier = Modifier,
    vm: SignInViewModel = hiltViewModel()
) {
    val state by vm.generateOtpUi.collectAsStateWithLifecycle()
    var phone by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is OtpGenerateState.Success -> {
                vm.clearGenerateState()
                onGoValidate(s.phone, s.expiresAt)
            }
            is OtpGenerateState.Error -> {
                delay(TOAST_DURATION_MS)
                vm.clearGenerateState()
            }
            else -> Unit
        }
    }

    SignInGenerateOtpScreen(
        state = state,
        phone = phone,
        onPhoneChange = { phone = it },
        onSubmit = vm::callGenerateOtp,
        modifier = modifier
    )
}

@Composable
fun SignInGenerateOtpScreen(
    state: OtpGenerateState,
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val loading = state is OtpGenerateState.Loading
    val isValid = phone.length == PHONE_LENGTH
    val toast = (state as? OtpGenerateState.Error)?.let { ToastMessage(ToastType.Error, it.message) }

    NavigationBarStyle(darkIcons = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.feature_signin_picture),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(top = 24.dp)
                .align(Alignment.TopCenter)
        )

        Surface(
            color = Color(0xFFF0F0F0),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                        .union(WindowInsets.ime)
                        .only(WindowInsetsSides.Bottom)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Validaremos tu ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("identidad") }
                        append(", con tu número de teléfono")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F0F0F)
                )

                PhoneInputField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    placeholder = "Ingresa tu número de teléfono",
                    enabled = !loading,
                    modifier = Modifier.padding(top = 4.dp)
                )

                PrimaryButton(
                    text = "Ingresar",
                    onClick = { onSubmit(phone) },
                    enabled = isValid,
                    loading = loading,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        ToastHost(
            toast = toast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewIdle() {
    SignInGenerateOtpScreen(
        state = OtpGenerateState.Idle,
        phone = "",
        onPhoneChange = {},
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewFilled() {
    SignInGenerateOtpScreen(
        state = OtpGenerateState.Idle,
        phone = "987654321",
        onPhoneChange = {},
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewLoading() {
    SignInGenerateOtpScreen(
        state = OtpGenerateState.Loading,
        phone = "987654321",
        onPhoneChange = {},
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewError() {
    SignInGenerateOtpScreen(
        state = OtpGenerateState.Error("Ya existe un código activo. Espera 1 minuto antes de solicitar uno nuevo."),
        phone = "987654321",
        onPhoneChange = {},
        onSubmit = {}
    )
}
