package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.example.declarativo.theme.ExampleTheme

class DeclarativoActivityHoisting : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreenHoisted(
                        modifier = Modifier.padding(innerPadding),
                        onSubmit = { email, password ->

                        }
                    )
                }
            }
        }
    }

}

// validador simple de email
private fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

@Composable
fun LoginScreenHoisted(
    modifier: Modifier = Modifier,
    // callback de submit inyectable (para test/navegación)
    onSubmit: (email: String, password: String) -> Unit = { _, _ -> }
) {
    // el PADRE posee el estado (state hoisting)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // derivación memorizada; solo recalcula cuando cambian dependencias
    val formValid by remember(email, password) {
        derivedStateOf { isValidEmail(email) && password.length >= 6 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmailField(
            value = email,
            onValueChange = { email = it },
            isError = email.isNotEmpty() && !isValidEmail(email)
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            isError = password.isNotEmpty() && password.length < 6
        )

        Button(
            onClick = { onSubmit(email, password) },
            enabled = formValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange, // ← hijo sin estado muta al padre
        label = { Text("Email") },
        isError = isError,
        singleLine = true,
        supportingText = {
            if (isError) Text("Invalid email format")
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password (min 6)") },
        isError = isError,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        supportingText = {
            if (isError) Text("Password too short")
        },
        modifier = Modifier.fillMaxWidth()
    )
}