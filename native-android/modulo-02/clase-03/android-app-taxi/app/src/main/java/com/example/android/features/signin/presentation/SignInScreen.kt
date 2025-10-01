package com.example.android.features.signin.presentation

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android.R
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PhoneInputField
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.features.signin.domain.usecase.SignInState

@Composable
fun SignInPhoneScreen(
    onGoHome: (String) -> Unit,
    onGoSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SignInViewModel = hiltViewModel()
) {
    val bg = Color.White
    var phone by remember { mutableStateOf("") }
    val state by vm.ui.collectAsState()

    val normalized = remember(phone) { phone.filter(Char::isDigit) }
    val isValid = normalized.length == 9
    val loading = state is SignInState.Loading

    LaunchedEffect(state) {
        when (val s = state) {
            is SignInState.Success -> onGoHome(s.welcomeName)
            else -> Unit
        }
    }

    NavigationBarStyle(color = bg, darkIcons = true)

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

        // Tarjeta inferior
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
                    onValueChange = { phone = it },
                    placeholder = "Ingresa tu número de teléfono",
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Mensaje de error del ViewModel (si existe)
                if (state is SignInState.Error) {

                }

                PrimaryButton(
                    text = "Ingresar",
                    onClick = { vm.submit(normalized) },
                    enabled = isValid && !loading,
                    loading = loading,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignInPhoneScreenPreviewFilled() {
    SignInPhoneScreen(
        onGoHome = {},
        onGoSignUp = {}
    )
}
