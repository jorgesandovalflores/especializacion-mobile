# Módulo 04 · Sesión 01 — GPS, Permisos y Sensores (Android/Kotlin)

> Curso: Especialización en Desarrollo Mobile · Módulo 04 (Android avanzado de plataforma)  
> Sesión 01 — GPS, permisos en tiempo de ejecución y sensores del dispositivo

---

## Objetivos

1. Dominar la **evolución** del sistema de permisos y acceso a **ubicación** y **sensores** en Android (API < 23 → 34+).
2. Implementar **permisos en tiempo de ejecución** con UX clara: **precisa vs. aproximada**, **foreground vs. background**.
3. Usar **FusedLocationProviderClient** con prioridades, _settings resolution_, validación de calidad (accuracy/age/mock).
4. Integrar **lectura de sensores** (acelerómetro y proximidad) de forma **lifecycle-aware** para evitar consumo excesivo.
5. Diseñar **fallbacks** ante fallas (última conocida, degradación de precisión, reintentos con backoff, detección de mocks).
6. Construir un **ejemplo práctico** con **Compose + ViewModel + Hilt** y (opcional) **Foreground Service** para tracking.

---

## Contenido

1. Evolución de ubicación y sensores en Android (con impacto práctico).
2. Permisos en tiempo de ejecución: matriz completa, racionales y patrones UX.
3. FusedLocationProviderClient: prioridades, una sola muestra vs. stream continuo, _settings_ y validación de calidad.
4. Sensores: acelerómetro y proximidad — registro/desregistro, frecuencias y filtrado.
5. Fallbacks y políticas de calidad (accuracy/age/mock) + reintentos con backoff.
6. Ejemplo práctico end‑to‑end (Compose + ViewModel + Hilt).
7. (Opcional) Foreground Service para tracking continuo con notificación persistente.
8. Pruebas, depuración y checklist final.
9. Laboratorio guiado.
10. Quiz de verificación.

---

## 1) Evolución: ubicación y sensores con impacto en diseño

### 1.1 Ubicación (de “todo vale” a “privacidad por diseño”)

#### A) Pre‑Marshmallow (API < 23) — **Permisos al instalar**

**Cómo era**

-   Los permisos (p. ej., `ACCESS_FINE_LOCATION`) se aceptaban **en la instalación**. Si el usuario instalaba, la app tenía el permiso siempre.
-   No existía la distinción UX de **precisa vs. aproximada** ni **grants temporales**.

**Implicancias**

-   Apps asumían acceso constante a GPS y usaban **alta precisión** sin control de batería ni comunicación de valor.

**Riesgos**

-   UX pobre (prometer features que no se usan).
-   **Batería**: consumo elevado por abuso de hardware.

**Estado actual**

-   Modelo obsoleto para apps modernas: hoy todo pasa por **runtime permissions** (API ≥ 23).

---

#### B) Marshmallow → Pie (API 23–28) — **Runtime permissions**

**Cambio clave**

-   Nacen los **dangerous permissions**: se piden **en ejecución** y el usuario **acepta o deniega** en tiempo real.

**Buenas prácticas**

-   Mostrar **racional** si el usuario deniega (explicar valor real).
-   **No** bloquear si deniega: ofrecer **degradación** (mapa sin punto exacto, buscar por dirección).
-   Idempotencia: **no spamear diálogos**.

**Caso real**

-   App de taxi (pasajero): si deniega, permitir **introducir origen/destino manualmente** y luego ofrecer activar ubicación.

---

#### C) Android 10–11 (API 29–30) — **Foreground vs. Background**

**Cambio**

-   Se separa `ACCESS_BACKGROUND_LOCATION`. El sistema **notifica** uso de ubicación en background.
-   Mayor fricción si pides background sin justificar.

**Regla de oro**

-   Primero **foreground** con caso de uso claro; **luego** background en un **segundo paso** con explicación concreta.

**Ejemplos**

-   **Sí background**: conductor en viaje con tracking continuo (notificación persistente + Foreground Service).
-   **No background**: pantalla “cerca de mí” que solo necesita ubicación cuando está visible.

**Errores comunes**

-   Pedir background al iniciar la app → **rechazo** y malas reseñas.

---

#### D) Android 12–14 (API 31–34) — **Precisa vs. aproximada** y **grants temporales**

**Cambios**

-   El usuario puede otorgar **aproximada** aunque pidas **precisa**.
-   **One‑time permission** (“solo esta vez”) y grants temporales.
-   **Foreground Service Types**: para tracking, debes usar **type=location** y **notificación visible**.

**Diseño recomendado**

-   **Mínima precisión**: solicita _coarse_ si basta; ofrece “Mejorar precisión” si aporta valor claro (ETA exacta, navegación).
-   Degradación visible: si la ubicación es aproximada, muestra **círculo de incertidumbre** y mensajes claros.

**Checklist**

-   Foreground (precisa/aprox) con racional.
-   Operar con aproximada cuando sea suficiente.
-   Background solo si hay **justificación** y **segunda pantalla** + **FS type=location**.

---

### 1.2 Sensores (acelerómetro, proximidad)

**Evolución**

-   Acceso clásico vía `SensorManager` + `SensorEventListener`.
-   Con Doze/App Standby y límites en background, **registrar/desregistrar** según ciclo de vida es obligatorio.

**Buenas prácticas**

-   Frecuencias moderadas `SENSOR_DELAY_NORMAL/GAME`.
-   Filtrado para ruido (filtro **low‑pass**).
-   Proximidad: puede ser **digital** (2 estados) o **analógico** (cm).

**Casos de uso**

-   Acelerómetro: _shake to refresh_, detección de frenadas bruscas, animaciones dependientes de inclinación.
-   Proximidad: evitar toques accidentales (llamadas, _push‑to‑talk_), apagar/atenuar UI al acercar al rostro.

---

## 2) Permisos en tiempo de ejecución (matriz + UX)

### 2.1 Matriz de permisos (hoy)

-   `ACCESS_COARSE_LOCATION` → **aproximada** (zona/ciudad).
-   `ACCESS_FINE_LOCATION` → **precisa** (GPS).
-   `ACCESS_BACKGROUND_LOCATION` → **seguimiento en segundo plano** (requiere foreground previo y justificación).

### 2.2 Patrón UX recomendado

1. **Pantalla de racional**: “Para mostrar taxis cercanos, necesitamos tu ubicación.”
2. **Solicitud foreground**: `FINE + COARSE`. Si otorga solo **COARSE**, **funciona** con límites.
3. **Pantalla específica** para background si el flujo lo necesita (conductor en ruta).

### 2.3 Lanzador de permisos (Compose)

```kotlin
// Código en inglés; comentarios en español
@Composable
fun RememberLocationPermissionLauncher(
    onGranted: (precise: Boolean) -> Unit,
    onDenied: () -> Unit
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        when {
            fine -> onGranted(true)   // Precisa
            coarse -> onGranted(false) // Aproximada
            else -> onDenied()        // Denegado
        }
    }
}
```

---

## 3) FusedLocationProviderClient (FLP)

### 3.1 Prioridades

-   `PRIORITY_HIGH_ACCURACY` → GPS (consumo mayor).
-   `PRIORITY_BALANCED_POWER_ACCURACY` → redes (buena en ciudad).
-   `PRIORITY_LOW_POWER` → esporádico.
-   `PRIORITY_PASSIVE` → escucha sin activar hardware.

### 3.2 Una muestra vs. flujo continuo

-   `getCurrentLocation()` → **single shot** “mejor posible ahora” (ideal “localízame”).
-   `requestLocationUpdates()` → **stream continuo** (requiere _stop_ explícito y cuidado de batería).

### 3.3 Validación + _settings resolution_

```kotlin
// Comentarios en español
suspend fun fetchBestLocation(
    context: Context,
    priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    maxAgeMs: Long = 30_000L,
    maxAccM: Float = 50f
): Location? {
    val fused = LocationServices.getFusedLocationProviderClient(context)
    val settings = LocationServices.getSettingsClient(context)

    val req = LocationRequest.Builder(priority, 5000).build()
    try {
        settings.checkLocationSettings(
            LocationSettingsRequest.Builder().addLocationRequest(req).build()
        ).await()
    } catch (_: Exception) {
        // En UI, normalmente disparas resolución; aquí devolvemos null
        return null
    }

    val current = try { fused.getCurrentLocation(priority, CancellationTokenSource().token).await() } catch (_: Exception) { null }
    val candidate = current ?: try { fused.lastLocation.await() } catch (_: Exception) { null }

    val okAge = candidate?.time?.let { System.currentTimeMillis() - it <= maxAgeMs } == true
    val okAcc = candidate?.accuracy?.let { it <= maxAccM } == true
    return if (candidate != null && okAge && okAcc) candidate else current ?: candidate
}
```

**Nota**: valida **accuracy** (m), **edad** (ms) e **isFromMockProvider**.

---

## 4) Sensores: acelerómetro y proximidad

### 4.1 Lector lifecycle‑aware

```kotlin
// Comentarios en español
class SensorReader(
    private val context: Context
) : SensorEventListener {

    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val prox = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val _accel = MutableStateFlow(Triple(0f, 0f, 0f))
    val accelFlow: StateFlow<Triple<Float, Float, Float>> = _accel

    private val _proximity = MutableStateFlow<Float?>(null)
    val proximityFlow: StateFlow<Float?> = _proximity

    fun start() {
        manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        manager.registerListener(this, prox, SensorManager.SENSOR_DELAY_NORMAL)
    }
    fun stop() = manager.unregisterListener(this)

    override fun onSensorChanged(e: SensorEvent) {
        when (e.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> _accel.value = Triple(e.values[0], e.values[1], e.values[2])
            Sensor.TYPE_PROXIMITY -> _proximity.value = e.values.firstOrNull()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

### 4.2 Filtro low‑pass (opcional)

```kotlin
// Comentarios en español
fun lowPass(input: FloatArray, output: FloatArray?, alpha: Float = 0.1f): FloatArray {
    if (output == null) return input
    for (i in input.indices) output[i] = output[i] + alpha * (input[i] - output[i])
    return output
}
```

---

## 5) Fallbacks y políticas de calidad

### 5.1 Política de calidad

```kotlin
data class QualityPolicy(
    val maxAgeMs: Long = 30_000L,
    val maxAccMeters: Float = 50f
)

fun Location?.passes(policy: QualityPolicy): Boolean {
    this ?: return false
    val fresh = System.currentTimeMillis() - time <= policy.maxAgeMs
    val accurate = accuracy <= policy.maxAccMeters
    return fresh && accurate
}
```

### 5.2 Reintentos con backoff (escalado)

```kotlin
// Comentarios en español
suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    baseDelayMs: Long = 400,
    block: suspend () -> T?
): T? {
    var attempt = 0
    var delayMs = baseDelayMs
    while (attempt < maxAttempts) {
        val res = block()
        if (res != null) return res
        attempt++
        kotlinx.coroutines.delay(delayMs)
        delayMs *= 2
    }
    return null
}
```

---

## 6) Ejemplo práctico end‑to‑end

### 6.1 Manifest (extracto mínimo)

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

<!-- Solo si necesitas tracking en segundo plano -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>

<!-- Si harás tracking continuo -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/>
```

### 6.2 Repositorios

```kotlin
// Comentarios en español
class LocationRepository(private val context: Context) {
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val settings by lazy { LocationServices.getSettingsClient(context) }

    suspend fun ensureSettings(priority: Int): Boolean {
        val req = LocationRequest.Builder(priority, 4000).build()
        return try {
            settings.checkLocationSettings(
                LocationSettingsRequest.Builder().addLocationRequest(req).build()
            ).await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun current(priority: Int) = try {
        fused.getCurrentLocation(priority, CancellationTokenSource().token).await()
    } catch (_: Exception) { null }

    suspend fun lastKnown() = try { fused.lastLocation.await() } catch (_: Exception) { null }
}

class SensorsRepository(private val reader: SensorReader) {
    val accel = reader.accelFlow
    val proximity = reader.proximityFlow
    fun start() = reader.start()
    fun stop() = reader.stop()
}
```

### 6.3 ViewModel

```kotlin
// Comentarios en español
data class UiLocation(
    val status: String = "idle",
    val lat: Double? = null,
    val lng: Double? = null,
    val acc: Float? = null,
    val speed: Float? = null,
    val mock: Boolean = false,
    val msg: String? = null
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val locRepo: LocationRepository,
    private val sensRepo: SensorsRepository
) : ViewModel() {

    private val policy = QualityPolicy()
    private val _ui = MutableStateFlow(UiLocation())
    val ui: StateFlow<UiLocation> = _ui

    val accel = sensRepo.accel
    val proximity = sensRepo.proximity

    fun startSensors() = sensRepo.start()
    fun stopSensors() = sensRepo.stop()

    fun loadOnce(precise: Boolean) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(status = "loading", msg = null)

            val priority = if (precise) Priority.PRIORITY_HIGH_ACCURACY
                           else Priority.PRIORITY_BALANCED_POWER_ACCURACY
            val settingsOk = locRepo.ensureSettings(priority)

            val first = if (settingsOk) locRepo.current(priority) else null
            val candidate = when {
                first.passes(policy) -> first
                else -> locRepo.lastKnown()
            }

            val result = candidate ?: first
            if (result == null) {
                _ui.value = _ui.value.copy(status = "error", msg = "No se obtuvo ubicación. Revisa permisos/GPS.")
                return@launch
            }

            _ui.value = UiLocation(
                status = "ok",
                lat = result.latitude,
                lng = result.longitude,
                acc = result.accuracy,
                speed = result.speed,
                mock = result.isFromMockProvider,
                msg = if (result.isFromMockProvider) "Ubicación simulada detectada" else null
            )
        }
    }
}
```

### 6.4 UI Compose

```kotlin
// Comentarios en español
@Composable
fun LocationScreen(viewModel: LocationViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsState()
    val accel by viewModel.accel.collectAsState()
    val prox by viewModel.proximity.collectAsState()

    val launcher = RememberLocationPermissionLauncher(
        onGranted = { precise -> viewModel.loadOnce(precise) },
        onDenied = { /* Mostrar degradación o ir a Settings */ }
    )

    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose { viewModel.stopSensors() }
    }

    Column(Modifier.padding(16.dp)) {
        Text("GPS, permisos y sensores", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }) { Text("Usar ubicación (precisa/aprox)") }

        Spacer(Modifier.height(16.dp))
        when (ui.status) {
            "idle" -> Text("Listo para solicitar ubicación.")
            "loading" -> Text("Obteniendo ubicación…")
            "ok" -> {
                Text("Lat: ${ui.lat}")
                Text("Lng: ${ui.lng}")
                Text("Acc: ${ui.acc} m  —  Vel: ${ui.speed} m/s")
                if (ui.mock) Text("Advertencia: ubicación simulada.")
            }
            "error" -> Text(ui.msg ?: "Error desconocido")
        }

        Spacer(Modifier.height(24.dp))
        Text("Acelerómetro: x=${"%.2f".format(accel.first)}, y=${"%.2f".format(accel.second)}, z=${"%.2f".format(accel.third)}")
        Text("Proximidad: ${prox?.let { "${it} cm" } ?: "N/A"}")
    }
}
```

---

## 7) (Opcional) Foreground Service de tracking

### 7.1 Diagrama de flujo

```mermaid
flowchart LR
    A[UI solicita permisos] --> B{Foreground otorgado?}
    B -- No --> C[Degradar UX]
    B -- Sí --> D[Iniciar FS type=location]
    D --> E[Solicitar updates periódicos]
    E --> F[Emitir a ViewModel/Repo]
    F --> G[Enviar a backend (si aplica)]
    G --> H{Fin de viaje?}
    H -- No --> E
    H -- Sí --> I[stopForeground(true) + stopSelf()]
```

### 7.2 Código base del Service

```kotlin
// Comentarios en español
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var locationRepo: LocationRepository

    private val channelId = "tracking_channel"
    private val notifId = 1001

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notif = buildNotification("Iniciando tracking…")
        startForeground(notifId, notif)
        startCollecting()
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Tracking", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Tracking activo")
            .setContentText(text)
            .setOngoing(true)
        return builder.build()
    }

    private fun startCollecting() {
        lifecycleScope.launch {
            while (true) {
                val loc = fetchBestLocation(
                    context = this@TrackingService,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
                // TODO: enviar loc al backend si aplica
                if (loc != null) {
                    val text = "Lat:${"%.5f".format(loc.latitude)} Lon:${"%.5f".format(loc.longitude)}"
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(notifId, buildNotification(text))
                }
                kotlinx.coroutines.delay(30_000)
            }
        }
    }
}
```

> Recuerda declarar `FOREGROUND_SERVICE_LOCATION` en el manifest y **detener** el service cuando acabe el viaje.

---

## 8) Pruebas, depuración y checklist

### 8.1 Pruebas

-   **ADB grants** para emulador:  
    `adb shell pm grant <package> android.permission.ACCESS_FINE_LOCATION`
-   **Mock locations** desde opciones de desarrollador (probar `isFromMockProvider`).
-   **Sensores**: inyectar `SensorReader` falso con `MutableStateFlow` para valores predecibles.

### 8.2 Depuración

-   Loggear **priority**, **accuracy**, **age**, **provider**, **isMock**.
-   Ver consumo con _Battery Historian_ o `adb bugreport` en sesiones largas.

### 8.3 Checklist

-   [ ] Pantalla de **racional** clara.
-   [ ] Lanzadores para **precisa/aprox** y (si aplica) **background** en segundo paso.
-   [ ] FLP con validación de **edad/accuracy/mock** y **settings resolution**.
-   [ ] Sensores lifecycle‑aware + frecuencias moderadas.
-   [ ] ViewModel coordinando permisos → settings → captura → fallbacks.
-   [ ] UI Compose con estados y datos en vivo.
-   [ ] (Opcional) Foreground Service con canal y notificación.
-   [ ] Pruebas de mock y registros diagnósticos.

---

## Apéndice A — Referencias útiles

-   Android Developers — Location permissions, Foreground services, Sensors API.
-   Google Play Services Location — FusedLocationProviderClient.
-   Recomendaciones de UX para permisos y privacidad en Android 12–14.
