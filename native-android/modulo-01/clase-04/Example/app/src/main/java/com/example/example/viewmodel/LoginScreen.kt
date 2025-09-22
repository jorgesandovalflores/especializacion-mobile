package com.example.example.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateHome: () -> Unit,
    onNavigateRegister: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var pass  by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    // recoger eventos del VM y traducirlos a navegación
    LaunchedEffect(Unit) {
        vm.navEvents.collectLatest { event ->
            when (event) {
                is NavEvent.GoHome     -> onNavigateHome()
                is NavEvent.GoRegister -> onNavigateRegister()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) }
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
                label = { Text("Email") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    error = null
                    vm.onLogin(email, pass)
                    if (!(email == "a@b.com" && pass == "1234")) {
                        error = "Invalid credentials"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in")
            }
            TextButton(onClick = { vm.onClickRegister() }) {
                Text("Create account")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
