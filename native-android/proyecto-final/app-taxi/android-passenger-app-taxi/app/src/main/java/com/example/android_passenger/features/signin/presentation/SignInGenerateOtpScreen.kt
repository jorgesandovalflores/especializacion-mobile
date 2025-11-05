package com.example.android_passenger.features.signin.presentation

import android.os.Build
import com.example.android_passenger.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android_passenger.commons.presentation.NavigationBarStyle
import com.example.android_passenger.commons.presentation.PhoneInputField
import com.example.android_passenger.commons.presentation.PrimaryButton
import com.example.android_passenger.commons.presentation.BaseToast
import com.example.android_passenger.commons.presentation.ToastType
import com.example.android_passenger.core.presentation.utils.PermissionUtils
import com.example.android_passenger.core.presentation.utils.rememberLocationPermissionState
import com.example.android_passenger.core.presentation.utils.rememberNotificationPermissionState
import com.example.android_passenger.features.signin.domain.usecase.OtpGenerateState

@Composable
fun SignInGenerateOtpRoute(
    onGoValidate: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    vm: SignInViewModel = hiltViewModel()
) {
    val appContext = LocalContext.current.applicationContext
    val state by vm.generateOtpUi.collectAsState()
    var phone by remember { mutableStateOf("") }
    val normalized = remember(phone) { phone.filter(Char::isDigit) }
    val isValid = normalized.length == 9
    val loading = state is OtpGenerateState.Loading

    // Estados de permisos
    val notifPerm = rememberNotificationPermissionState()
    val locPerm = rememberLocationPermissionState()

    // Registrar canales de notificación (incluye "all")
    LaunchedEffect(Unit) {
        PermissionUtils.registerNotificationChannels(context = appContext)
        if (!notifPerm.hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPerm.requestPermission()
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is OtpGenerateState.Success -> {
                vm.clearGenerateState()
                onGoValidate(s.phone, s.expiresAt)
            }
            else -> Unit
        }
    }

    SignInGenerateOtpScreen(
        phone = phone,
        onPhoneChange = { phone = it },
        isValid = isValid,
        loading = loading,
        state = state,
        onSubmit = {
            // Si quieres forzar pedir permisos justo antes de continuar:
            if (!locPerm.hasPermission) locPerm.requestPermission()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifPerm.hasPermission) {
                notifPerm.requestPermission()
            }
            vm.callGenerateOtp(normalized)
        },
        modifier = modifier
    )
}

@Composable
fun SignInGenerateOtpScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    isValid: Boolean,
    loading: Boolean,
    state: OtpGenerateState,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color.White
    NavigationBarStyle(color = bg, darkIcons = true)

    val toastMessage = remember(state) {
        (state as? OtpGenerateState.Error)?.message
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.feature_signin_picture),
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
            shadowElevation = 0.dp,
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
                horizontalAlignment = Alignment.Start,
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
                    modifier = Modifier.padding(top = 4.dp)
                )

                PrimaryButton(
                    text = "Ingresar",
                    onClick = onSubmit,
                    enabled = isValid && !loading,
                    loading = loading,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        toastMessage?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                BaseToast(message = it, type = ToastType.Error)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignInGenerateOtpScreenPreview_Idle() {
    SignInGenerateOtpScreen(
        phone = "",
        onPhoneChange = {},
        isValid = false,
        loading = false,
        state = OtpGenerateState.Idle,
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignInGenerateOtpScreenPreview_Error() {
    SignInGenerateOtpScreen(
        phone = "987654321",
        onPhoneChange = {},
        isValid = true,
        loading = false,
        state = OtpGenerateState.Error(message = "No pudimos generar el código. Intenta de nuevo."),
        onSubmit = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignInGenerateOtpScreenPreview_Loading() {
    SignInGenerateOtpScreen(
        phone = "987654321",
        onPhoneChange = {},
        isValid = true,
        loading = true,
        state = OtpGenerateState.Loading,
        onSubmit = {}
    )
}
