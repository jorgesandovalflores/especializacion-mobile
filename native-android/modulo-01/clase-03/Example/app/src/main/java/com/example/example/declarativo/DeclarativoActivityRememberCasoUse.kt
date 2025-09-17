package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeclarativoActivityRememberCasoUse : ComponentActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                RegisterForm(modifier = Modifier.padding(innerPadding))
            }
        }
    }

}

@Composable
fun RegisterForm(modifier: Modifier = Modifier) {
    // Persistente: el texto del usuario debe sobrevivir rotaciones
    var username by rememberSaveable { mutableStateOf("") }

    // Efímero: el mensaje temporal no importa perderlo al rotar
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Registro de usuario")
        Spacer(Modifier.height(8.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    isSaving = true
                    delay(2000) // Simula guardado en backend
                    isSaving = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
        Spacer(Modifier.height(16.dp))
        if (isSaving) {
            Text("Guardando…") // Se pierde si rotas mientras aparece
        }
        Text("Valor actual: $username") // Se mantiene tras rotación
    }
}

@Composable
@Preview
fun RegisterFormPreview() {
    RegisterForm()
}