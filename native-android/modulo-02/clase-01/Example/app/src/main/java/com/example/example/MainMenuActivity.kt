package com.example.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.example.core.theme.ExampleTheme
import com.example.example.features.mvc.AppMVC
import com.example.example.features.mvp.AppMVP
import com.example.example.features.mvvm.AppMVVM

// Punto de entrada de la app: el mismo menú que la Sección 0 del README,
// para abrir el listado de productos (fakestoreapi.com) implementado con
// cada arquitectura vista en esta clase.
class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ExampleTheme { MainMenuScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Módulo 2 · Sesión 1 — Arquitecturas") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mismo listado de productos, tres arquitecturas. Elige una demo:")

            Button(
                onClick = { context.startActivity(Intent(context, AppMVC::class.java)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("MVC — Model View Controller") }

            Button(
                onClick = { context.startActivity(Intent(context, AppMVP::class.java)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("MVP — Model View Presenter") }

            Button(
                onClick = { context.startActivity(Intent(context, AppMVVM::class.java)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("MVVM — ViewModel + StateFlow + LiveData") }
        }
    }
}
