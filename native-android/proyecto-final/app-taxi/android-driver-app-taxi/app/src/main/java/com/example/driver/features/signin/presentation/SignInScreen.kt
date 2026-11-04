package com.example.driver.features.signin.presentation

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
import androidx.compose.foundation.layout.widthIn
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
import com.example.driver.R
import com.example.driver.commons.presentation.CONTENT_MAX_WIDTH
import com.example.driver.commons.presentation.NavigationBarStyle
import com.example.driver.commons.presentation.PasswordInputField
import com.example.driver.commons.presentation.PhoneInputField
import com.example.driver.commons.presentation.PrimaryButton
import com.example.driver.commons.presentation.ToastHost
import com.example.driver.commons.presentation.ToastMessage
import com.example.driver.commons.presentation.ToastType
import com.example.driver.commons.presentation.WidthClass
import com.example.driver.commons.presentation.widthClassOf
import com.example.driver.core.presentation.theme.AndroidTheme
import com.example.driver.features.signin.domain.usecase.LoginState
import com.example.driver.features.signin.domain.usecase.LoginUseCase
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3_000L

@Composable
fun SignInRoute(
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SignInViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> {
                vm.clearState()
                onGoHome()
            }
            is LoginState.Error -> {
                delay(TOAST_DURATION_MS)
                vm.clearState()
            }
            else -> Unit
        }
    }

    SignInScreen(
        state = state,
        phone = phone,
        password = password,
        onPhoneChange = { phone = it },
        onPasswordChange = { password = it },
        onSubmit = { vm.submit(phone, password) },
        modifier = modifier
    )
}

@Composable
fun SignInScreen(
    state: LoginState,
    phone: String,
    password: String,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loading = state is LoginState.Loading
    val toast = (state as? LoginState.Error)?.let { ToastMessage(ToastType.Error, it.message) }

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
                    .padding(top = 24.dp, bottom = 300.dp)
                    .align(Alignment.TopCenter)
            )
            SignInForm(
                phone = phone,
                password = password,
                onPhoneChange = onPhoneChange,
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit,
                loading = loading,
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
                    password = password,
                    onPhoneChange = onPhoneChange,
                    onPasswordChange = onPasswordChange,
                    onSubmit = onSubmit,
                    loading = loading,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = CONTENT_MAX_WIDTH)
                )
            }
        }

        ToastHost(toast = toast, modifier = Modifier.align(Alignment.TopCenter))
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
    password: String,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    loading: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val canSubmit = LoginUseCase.canSubmit(phone, password)

    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = shape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = AnnotatedString.fromHtml(stringResource(R.string.signin_title)),
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

            PasswordInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.signin_password_placeholder),
                showLabel = stringResource(R.string.signin_password_show),
                hideLabel = stringResource(R.string.signin_password_hide),
                onDone = { if (canSubmit) onSubmit() },
                enabled = !loading
            )

            PrimaryButton(
                text = stringResource(R.string.signin_submit),
                onClick = onSubmit,
                enabled = canSubmit,
                loading = loading,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SignInScreenPreview(state: LoginState, phone: String, password: String) {
    AndroidTheme {
        SignInScreen(
            state = state,
            phone = phone,
            password = password,
            onPhoneChange = {},
            onPasswordChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreviewIdle() {
    SignInScreenPreview(LoginState.Idle, "", "")
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreviewError() {
    SignInScreenPreview(LoginState.Error("Teléfono o contraseña incorrectos"), "900000001", "Driver123*")
}

@Preview(name = "Horizontal", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun SignInScreenPreviewLandscape() {
    SignInScreenPreview(LoginState.Idle, "900000001", "")
}

@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenPreviewDark() {
    SignInScreenPreview(LoginState.Loading, "900000001", "Driver123*")
}
