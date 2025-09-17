package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class DeclarativoActivityRememberCasoUso : ComponentActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                DrawerWithRememberSave(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerWithRememberSave(modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                TopAppBar(
                    title = { Text("Menú") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.close() }
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Cerrar")
                        }
                    }
                )
                ListItem(headlineContent = { Text("Opción 1") })
                ListItem(headlineContent = { Text("Opción 2") })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pantalla principal") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { inner ->
            Text(
                text = if (drawerState.isOpen) "Drawer abierto" else "Drawer cerrado",
                modifier = modifier.padding(inner).padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawerWithRememberPreview() {
    DrawerWithRememberSave()
}
