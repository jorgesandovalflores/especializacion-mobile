package com.example.demoworkermanager.location

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.demoworkermanager.permissions.PermissionUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "LocationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "LocationWorker iniciado")
        return try {
            if (PermissionUtils.hasLocationPermissions(applicationContext)) {
                Log.d(TAG, "Permisos OK, obteniendo ubicación...")
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

                Log.d(TAG, "Solicitando ubicación actual...")
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                if (location != null) {
                    Log.d(TAG, "Ubicación obtenida: Lat=${location.latitude}, Lon=${location.longitude}")
                    sendLocationToServer(location.latitude, location.longitude)
                    Result.success()
                } else {
                    Log.w(TAG, "Ubicación es nula")
                    Result.retry()
                }
            } else {
                Log.w(TAG, "Sin permisos de ubicación")
                Result.failure()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad: ${e.message}")
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error general: ${e.message}")
            Result.retry()
        }
    }

    private fun sendLocationToServer(latitude: Double, longitude: Double) {
        Log.i(TAG, "Enviando ubicación al servidor: $latitude, $longitude")
    }
}