package com.example.android.features.signin.presentation

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
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
import com.example.android.commons.presentation.WidthClass
import com.example.android.commons.presentation.widthClassOf
import com.example.android.core.presentation.theme.AndroidTheme
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

    NavigationBarStyle()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (widthClassOf(maxWidth) == WidthClass.Compact) {
            SignInPicture(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .padding(top = 24.dp)
                    .align(Alignment.TopCenter)
            )
            SignInForm(
                phone = phone,
                onPhoneChange = onPhoneChange,
                onSubmit = onSubmit,
                loading = loading,
                isValid = isValid,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                            .union(WindowInsets.ime)
                            .only(WindowInsetsSides.Bottom)
                    )
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignInPicture(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SignInForm(
                    phone = phone,
                    onPhoneChange = onPhoneChange,
                    onSubmit = onSubmit,
                    loading = loading,
                    isValid = isValid,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        ToastHost(
            toast = toast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun SignInPicture(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.feature_signin_picture),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun SignInForm(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    loading: Boolean,
    isValid: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = shape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = AnnotatedString.fromHtml(stringResource(R.string.signin_identity_title)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            PhoneInputField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = stringResource(R.string.signin_phone_placeholder),
                enabled = !loading,
                modifier = Modifier.padding(top = 4.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.signin_submit),
                onClick = { onSubmit(phone) },
                enabled = isValid,
                loading = loading,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SignInGenerateOtpScreenPreview(state: OtpGenerateState, phone: String) {
    AndroidTheme {
        SignInGenerateOtpScreen(
            state = state,
            phone = phone,
            onPhoneChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewIdle() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Idle, "")
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewFilled() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Idle, "987654321")
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewLoading() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Loading, "987654321")
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInGenerateOtpScreenPreviewError() {
    SignInGenerateOtpScreenPreview(
        OtpGenerateState.Error("Ya existe un código activo. Espera 1 minuto antes de solicitar uno nuevo."),
        "987654321"
    )
}

@Preview(name = "Horizontal", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun SignInGenerateOtpScreenPreviewLandscape() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Idle, "987654321")
}

@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignInGenerateOtpScreenPreviewDark() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Idle, "987654321")
}

@Preview(name = "Inglés", showBackground = true, locale = "en")
@Composable
private fun SignInGenerateOtpScreenPreviewEnglish() {
    SignInGenerateOtpScreenPreview(OtpGenerateState.Idle, "")
}
