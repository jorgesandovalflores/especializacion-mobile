package com.example.datastore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current.applicationContext
    val vm: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )

    val state by vm.uiState.collectAsStateWithLifecycle()

    // Estado local para el input inmediato
    var usernameInput by remember { mutableStateOf(state.username) }

    // Sincronizar con el estado del ViewModel
    LaunchedEffect(state.username) {
        usernameInput = state.username
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Column {
            Text("Dark mode")
            Switch(
                checked = state.darkMode,
                onCheckedChange = vm::onDarkModeChange,
                thumbContent = null,
                colors = SwitchDefaults.colors()
            )
        }

        OutlinedTextField(
            value = usernameInput,
            onValueChange = { newValue ->
                usernameInput = newValue
                vm.onUsernameChange(newValue)
            },
            label = { Text("Username") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* Acción guardar */ }) {
            Text("Guardar")
        }

        // Opcional: mostrar el estado actual del ViewModel
        Text("Estado actual: ${state.username}")
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}