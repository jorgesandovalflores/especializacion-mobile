package com.example.acelerometroapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class AcelerometroManager(
    private val context: Context,
    private val onSensorChange: (x: Float, y: Float, z: Float) -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    fun startListening() {
        // 1. Obtener el servicio de sensores del sistema
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // 2. Buscar el sensor de acelerómetro
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 3. Registrar nuestro listener para recibir actualizaciones
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        // 4. Dejar de escuchar el sensor para ahorrar batería
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // 5. Este método se llama CADA VEZ que el sensor detecta movimiento
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]  // Eje horizontal
                val y = it.values[1]  // Eje vertical
                val z = it.values[2]  // Eje profundidad

                // 6. Enviamos los nuevos valores a la pantalla
                onSensorChange(x, y, z)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No nos interesa para este ejemplo básico
    }
}