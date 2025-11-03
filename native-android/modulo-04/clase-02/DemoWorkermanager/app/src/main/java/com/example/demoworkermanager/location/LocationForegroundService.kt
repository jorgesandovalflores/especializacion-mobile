package com.example.demoworkermanager.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.demoworkermanager.permissions.PermissionUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationForegroundService : Service() {

    companion object {
        const val TAG = "LocationForegroundService"
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate - Servicio creado")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand - Iniciando servicio")

        // Crear la notificación primero
        val notification = createNotification()

        // Iniciar en foreground ANTES de cualquier otra operación
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Servicio en primer plano iniciado")

        // Luego iniciar las actualizaciones de ubicación
        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - Servicio siendo destruido")
        stopLocationUpdates()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Location tracking service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        Log.d(TAG, "Creando notificación")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Seguimiento de Ubicación Activo")
            .setContentText("Tu ubicación se está enviando en tiempo real")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult - Nueva ubicación recibida")
                locationResult.lastLocation?.let { location ->
                    serviceScope.launch {
                        Log.d(TAG, "Ubicación en tiempo real: Lat=${location.latitude}, Lon=${location.longitude}")
                        sendLocationToServer(location.latitude, location.longitude)
                    }
                } ?: run {
                    Log.w(TAG, "Ubicación nula recibida en callback")
                }
            }
        }
        Log.d(TAG, "LocationCallback creado")
    }

    private fun startLocationUpdates() {
        try {
            Log.d(TAG, "Verificando permisos para location updates")
            if (PermissionUtils.hasLocationPermissions(this)) {
                Log.d(TAG, "Permisos OK, configurando LocationRequest")

                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000 // 5 segundos
                ).apply {
                    setMinUpdateIntervalMillis(3000) // Mínimo 3 segundos
                    setMaxUpdateDelayMillis(10000) // Máximo 10 segundos
                }.build()

                Log.d(TAG, "Solicitando actualizaciones de ubicación")
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
                Log.d(TAG, "Solicitud de ubicaciones enviada")

            } else {
                Log.e(TAG, "Sin permisos de ubicación para el servicio")
                stopSelf()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad en startLocationUpdates: ${e.message}", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error general en startLocationUpdates: ${e.message}", e)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        try {
            Log.d(TAG, "Deteniendo actualizaciones de ubicación")
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Actualizaciones de ubicación detenidas")
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo location updates: ${e.message}")
        }
    }

    private fun sendLocationToServer(latitude: Double, longitude: Double) {
        Log.i(TAG, "Enviando ubicación en tiempo real: $latitude, $longitude")
    }
}