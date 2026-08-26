package com.example.exampleandroid.login

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private const val PASSWORD_MIN_LENGTH = 6
private const val SIMULATED_NETWORK_DELAY_MS = 900L

class LoginCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    // Comentario (ES): "estado hoisted" dentro del propio composable — ningún código
    // llama a estas variables directamente; Compose observa sus lecturas y decide
    // qué recomponer cuando cambian.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val emailValid = EMAIL_PATTERN.matches(email)
    val passwordValid = password.length >= PASSWORD_MIN_LENGTH
    val formValid = emailValid && passwordValid

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = email.isNotEmpty() && !emailValid,
            supportingText = {
                if (email.isNotEmpty() && !emailValid) Text("Ingresa un email válido")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = password.isNotEmpty() && !passwordValid,
            supportingText = {
                if (password.isNotEmpty() && !passwordValid) {
                    Text("Mínimo $PASSWORD_MIN_LENGTH caracteres")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Comentario (ES): no hay Handler ni findViewById; mutamos el estado
                // y dejamos que la recomposición actualice la UI por nosotros.
                scope.launch {
                    isLoading = true
                    resultMessage = null
                    delay(SIMULATED_NETWORK_DELAY_MS)
                    isLoading = false
                    resultMessage = "Bienvenido, $email"
                }
            },
            enabled = formValid && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Ingresando..." else "Iniciar sesión")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
        }

        resultMessage?.let { message ->
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Light")
@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
