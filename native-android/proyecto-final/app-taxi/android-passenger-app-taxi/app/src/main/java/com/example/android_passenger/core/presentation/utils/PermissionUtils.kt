package com.example.android_passenger.core.presentation.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionUtils {

    // -------- Ubicación --------
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    // -------- Notificaciones (Android 13+) --------
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // En Android <= 12 no existe el permiso en tiempo de ejecución
            true
        }
    }


    fun registerNotificationChannels(context: Context) {
        val all = NotificationChannel(
            /* id   = */ "all",
            /* name = */ "Notificaciones generales",
            /* importance = */ NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Canal principal para notificaciones del backend"
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(all)
    }
}

// ---------------- Composables de estado de permisos ----------------

@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasLocationPermission(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasPermission = fine || coarse
    }

    return remember(hasPermission) {
        LocationPermissionState(
            hasPermission = hasPermission,
            requestPermission = {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }
}

data class LocationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberNotificationPermissionState(): NotificationPermissionState {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    return remember(hasPermission) {
        NotificationPermissionState(
            hasPermission = hasPermission,
            requestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
    }
}

data class NotificationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)
