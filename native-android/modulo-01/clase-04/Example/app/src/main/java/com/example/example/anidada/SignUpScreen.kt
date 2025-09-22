package com.example.example.anidada

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackToSignIn: () -> Unit,
    onRegistered: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var pass  by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
                navigationIcon = { TextButton(onClick = onBackToSignIn) { Text("Back") } }
            )
        }
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
                label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it },
                label = { Text("Confirm password") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                error = when {
                    email.isBlank() || pass.isBlank() -> "Missing data"
                    pass != confirm -> "Passwords do not match"
                    else -> null
                }
                if (error == null) onRegistered()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Create account")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}