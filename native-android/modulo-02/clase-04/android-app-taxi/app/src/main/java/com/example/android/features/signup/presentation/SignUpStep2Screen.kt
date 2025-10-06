package com.example.android.features.signup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton

/* =========================
   STEP 2
   ========================= */
@Composable
fun SignUpStep2Route(
    onFinish: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    val isValid = name.isNotBlank()

    SignUpStep2Screen(
        name = name,
        onNameChange = { name = it },
        isValid = isValid,
        onFinish = { onFinish(name) },
        modifier = modifier
    )
}

@Composable
fun SignUpStep2Screen(
    name: String,
    onNameChange: (String) -> Unit,
    isValid: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color.White
    NavigationBarStyle(color = bg, darkIcons = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Centro: título + input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "sign up – step 2",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF0F0F0F)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Botón inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Finalizar registro",
                onClick = onFinish,
                enabled = isValid,
                loading = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep2ScreenPreview_Empty() {
    SignUpStep2Screen(
        name = "",
        onNameChange = {},
        isValid = false,
        onFinish = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep2ScreenPreview_Filled() {
    SignUpStep2Screen(
        name = "Jorge",
        onNameChange = {},
        isValid = true,
        onFinish = {}
    )
}
