package com.example.example.anidada

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onGoToSignUp: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var pass  by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign In") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                if (email.isNotBlank() && pass.isNotBlank()) onSuccess()
                else error = "Invalid credentials"
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in")
            }
            TextButton(onClick = onGoToSignUp) { Text("Create account") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}