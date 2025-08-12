package com.example.authsample.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authsample.ui.theme.*

@Composable
fun LoginScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome back", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Email") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Forgot password?",
                color = Primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO: acción */ }
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Text("Sign in")
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Create account",
                color = Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }
    }
}
