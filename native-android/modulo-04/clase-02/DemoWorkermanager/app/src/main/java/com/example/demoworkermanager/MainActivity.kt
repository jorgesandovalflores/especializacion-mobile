package com.example.demoworkermanager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.demoworkermanager.location.LocationForegroundService
import com.example.demoworkermanager.location.LocationWorker
import com.example.demoworkermanager.permissions.PermissionUtils
import com.example.demoworkermanager.preferences.AppStateManager
import com.example.demoworkermanager.ui.theme.LocationDemoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationTrackingScreen()
                }
            }
        }
    }
}

@Composable
fun LocationTrackingScreen() {
    val context = LocalContext.current
    val appStateManager = remember { AppStateManager(context) }

    // Estados observables
    var isTrackingActive by remember { mutableStateOf(false) }
    var isTrackingWithService by remember { mutableStateOf(false) }

    // WorkManager instance
    val workManager = remember { WorkManager.getInstance(context) }

    // Observar cambios en el estado
    LaunchedEffect(key1 = appStateManager) {
        appStateManager.isLocationTrackingActive.collectLatest { active ->
            isTrackingActive = active
        }
    }

    LaunchedEffect(key1 = appStateManager) {
        appStateManager.isTrackingWithService.collectLatest { withService ->
            isTrackingWithService = withService
        }
    }

    // Launcher para permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(context, "Se necesitan permisos de ubicación", Toast.LENGTH_LONG).show()
        }
    }

    // Solicitar permisos
    fun requestLocationPermissions() {
        val permissions = PermissionUtils.getRequiredLocationPermissions()
        locationPermissionLauncher.launch(permissions)
    }

    // Iniciar tracking con WorkManager
    fun startWorkManagerTracking() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES, // Intervalo mínimo recomendado
            1, TimeUnit.MINUTES   // Flex interval
        ).setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "location_work",
            ExistingPeriodicWorkPolicy.KEEP,
            locationWorkRequest
        )
    }

    // Detener WorkManager tracking
    fun stopWorkManagerTracking() {
        workManager.cancelUniqueWork("location_work")
    }

    // Iniciar servicio en primer plano
    fun startForegroundServiceTracking() {
        Log.d("MainActivity", "Iniciando servicio foreground")

        if (!PermissionUtils.hasLocationPermissions(context)) {
            Log.e("MainActivity", "Sin permisos de ubicación")
            return
        }

        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("MainActivity", "startForegroundService llamado exitosamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}", e)
        }
    }

    // Detener servicio en primer plano
    fun stopForegroundServiceTracking() {
        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        context.stopService(serviceIntent)
    }

    // Iniciar tracking
    fun startTracking(withService: Boolean) {
        val hasPermissions = PermissionUtils.hasLocationPermissions(context)
        Log.d("PermissionDebug", "Permisos concedidos: $hasPermissions")

        if (hasPermissions) {
            // Persistir estado
            kotlinx.coroutines.MainScope().launch {
                appStateManager.startLocationTracking(withService)
            }

            if (withService) {
                startForegroundServiceTracking()
            } else {
                startWorkManagerTracking()
            }

            Toast.makeText(context, "Tracking iniciado", Toast.LENGTH_SHORT).show()
        } else {
            requestLocationPermissions()
        }
    }

    // Detener tracking
    fun stopTracking() {
        kotlinx.coroutines.MainScope().launch {
            appStateManager.stopLocationTracking()
        }

        if (isTrackingWithService) {
            stopForegroundServiceTracking()
        } else {
            stopWorkManagerTracking()
        }

        Toast.makeText(context, "Tracking detenido", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isTrackingActive) {
            // Mostrar opciones cuando no hay tracking activo
            Text(
                text = "Selecciona el método de tracking",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { startTracking(withService = false) }
            ) {
                Text("WorkManager (Background)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { startTracking(withService = true) }
            ) {
                Text("Foreground Service")
            }
        } else {
            // Mostrar estado activo y botón para detener
            Text(
                text = if (isTrackingWithService) {
                    "Tracking activo con Foreground Service"
                } else {
                    "Tracking activo con WorkManager"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { stopTracking() }
            ) {
                Text("Detener Tracking")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationTrackingScreenPreview() {
    LocationDemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Para el preview, simulamos un contexto sin dependencias reales
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Selecciona el método de tracking",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { }
                ) {
                    Text("WorkManager (Background)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { }
                ) {
                    Text("Foreground Service")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationTrackingScreenActivePreview() {
    LocationDemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tracking activo con WorkManager",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { }
                ) {
                    Text("Detener Tracking")
                }
            }
        }
    }
}