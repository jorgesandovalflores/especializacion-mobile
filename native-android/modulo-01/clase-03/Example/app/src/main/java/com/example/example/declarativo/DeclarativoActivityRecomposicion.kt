package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.example.declarativo.theme.ExampleTheme
import kotlinx.coroutines.delay

// Cambia manualmente cuál LoginForm* se llama para demostrar cada caso
// de recomposición; comenta/descomenta según lo que quieras mostrar.
class DeclarativoActivityRecomposicion : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginFormState(Modifier.padding(innerPadding))
                    //LoginFormParams(Modifier.padding(innerPadding))
                    //LoginFormCompositionLocal(Modifier.padding(innerPadding))
                    //LoginFormEffectKey(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// IntArray (no State): incrementarlo no dispara recomposición, solo cuenta
// cuántas veces Compose volvió a ejecutar este composable.
@Composable
private fun rememberRecompositionCount(): Int {
    val counter = remember { intArrayOf(0) }
    counter[0]++
    return counter[0]
}

// CASO 1 — State/MutableState: leer username/password dentro del composable
// lo suscribe a sus cambios; cada tecleo dispara una recomposición.
@Composable
fun LoginFormState(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val recompositions = rememberRecompositionCount()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
            Text("Recomposiciones: $recompositions")
        }
    }
}

// CASO 2 — Parámetros: LoginActionButton solo recompone cuando cambia el
// valor de 'enabled' que recibe, no en cada tecleo del formulario.
@Composable
fun LoginFormParams(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val formValid = username.isNotBlank() && password.length >= 6

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            LoginActionButton(enabled = formValid)
        }
    }
}

@Composable
private fun LoginActionButton(enabled: Boolean) {
    val recompositions = rememberRecompositionCount()
    Column {
        Button(onClick = {}, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }
        Text("Recomposiciones: $recompositions")
    }
}

// CASO 3 — CompositionLocal: MaterialTheme.colorScheme viene de un
// CompositionLocal; cambiar de tema recompone lo que lo lee, sin cambiar parámetros.
@Composable
fun LoginFormCompositionLocal(modifier: Modifier = Modifier) {
    var darkTheme by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ExampleTheme(darkTheme = darkTheme) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Modo oscuro")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
                }
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                ThemedLoginButton()
            }
        }
    }
}

@Composable
private fun ThemedLoginButton() {
    val recompositions = rememberRecompositionCount()
    Column {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
        Text("Recomposiciones: $recompositions")
    }
}

// CASO 4 — LaunchedEffect(key): la clave decide cuándo se relanza el
// efecto; con 'username' como clave, cada cambio cancela y relanza la corrutina.
@Composable
fun LoginFormEffectKey(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Escribe un usuario…") }

    LaunchedEffect(username) {
        if (username.isBlank()) {
            status = "Escribe un usuario…"
            return@LaunchedEffect
        }
        status = "Verificando \"$username\"…"
        delay(600)
        status = "Disponible: \"$username\""
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(status, style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormStatePreview() {
    ExampleTheme { LoginFormState() }
}
