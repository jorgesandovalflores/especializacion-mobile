package com.example.acelerometroapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Datos que vamos a mostrar en pantalla
data class AccelerometerData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

class AccelerometerViewModel : ViewModel() {

    // Flujo de datos observable para la UI
    private val _sensorData = MutableStateFlow(AccelerometerData())
    val sensorData: StateFlow<AccelerometerData> = _sensorData.asStateFlow()

    private lateinit var acelerometroManager: AcelerometroManager

    fun initializeAccelerometer(context: android.content.Context) {
        acelerometroManager = AcelerometroManager(context) { x, y, z ->
            // Actualizamos los datos cuando el sensor cambia
            _sensorData.value = AccelerometerData(x, y, z)
        }
    }

    fun startAccelerometer() {
        acelerometroManager.startListening()
    }

    fun stopAccelerometer() {
        acelerometroManager.stopListening()
    }
}