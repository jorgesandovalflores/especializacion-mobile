# Módulo 04 · Sesión 02 — WorkManager y Servicios en Segundo Plano (Android/Kotlin)

## Objetivos de aprendizaje

1. Comprender la **evolución del manejo de procesos en segundo plano** en Android y cómo ha cambiado con las versiones del sistema.
2. Diferenciar correctamente entre **Service**, **ForegroundService** y **WorkManager**, entendiendo cuándo usar cada uno.
3. Implementar **CoroutineWorker** con **restricciones**, **backoff**, **tareas periódicas** y **encadenamiento** de trabajos.
4. Aplicar estos conceptos en **casos de uso reales** de distintas apps: mensajería, fitness, almacenamiento, clima y multimedia.

---

## 1) Evolución del manejo en segundo plano en Android

### Historia y restricciones por versión

En Android 1.x hasta Android 5.x, los desarrolladores podían ejecutar **servicios en background** de forma indefinida, incluso cuando el usuario cerraba la aplicación.  
Esto permitía construir funciones como reproductores de música o sincronizaciones continuas, pero también provocaba **consumo excesivo de batería** y **sobrecarga de procesos**.

Para corregirlo, Google fue limitando progresivamente el acceso al background:

| Versión                 | Cambio clave                                                   | Impacto                                                                                 |
| ----------------------- | -------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| **Android 6 (API 23)**  | Introducción de **Doze Mode** y **App Standby**.               | Las tareas se suspenden cuando el dispositivo está inactivo.                            |
| **Android 8 (API 26)**  | Restricciones a los **background services**.                   | Los servicios solo pueden ejecutarse con notificación visible o mediante `WorkManager`. |
| **Android 10 (API 29)** | Control de permisos de **ubicación y sensores en background**. | Mayor control del usuario.                                                              |
| **Android 12 (API 31)** | Límite de 10 segundos para servicios iniciados en background.  | Obliga a usar `WorkManager` para trabajos largos.                                       |
| **Android 14 (API 34)** | Optimización de trabajos y energía.                            | WorkManager se integra totalmente con JobScheduler.                                     |

### Caso de uso: app de mensajería

Antes de Android 8, una app de chat podía mantener un `Service` corriendo todo el día para recibir mensajes.  
Hoy, Android detendría ese servicio si la app pasa a segundo plano.  
Por eso, las apps modernas usan **Firebase Cloud Messaging (push)** o **WorkManager** para tareas diferibles (sincronizar mensajes, subir adjuntos, limpiar caché, etc.).

> **Lección:** Android exige usar mecanismos eficientes y cooperativos con el sistema para mantener autonomía sin afectar batería.

---

## 2) Servicios vs WorkManager — ¿cuándo usar qué?

| Tipo de tarea                            | Solución moderna                      | Caso real                                                   |
| ---------------------------------------- | ------------------------------------- | ----------------------------------------------------------- |
| Tarea visible y constante                | **ForegroundService**                 | Reproductor de música, grabadora de audio, tracking de GPS. |
| Tarea corta e inmediata                  | **Service**                           | Descarga rápida o envío instantáneo sin reintentos.         |
| Tarea confiable, diferible y persistente | **WorkManager (OneTimeWorkRequest)**  | Subir fotos o logs al servidor cuando haya conexión.        |
| Tarea periódica                          | **WorkManager (PeriodicWorkRequest)** | Sincronizar pasos o calorías en app fitness.                |
| Ejecución exacta (hora específica)       | **AlarmManager**                      | Recordatorio de medicación o alarma de calendario.          |

### Caso de uso: app de noticias

Una app puede usar:

-   **Service:** para descargar una imagen justo después de que el usuario la comparte.
-   **WorkManager:** para sincronizar artículos o borrar caché cuando el dispositivo está cargando y conectado al Wi-Fi.

---

## 3) Fundamentos de WorkManager

### Tipos de WorkRequest

| Tipo                    | Descripción                                       | Ejemplo                               |
| ----------------------- | ------------------------------------------------- | ------------------------------------- |
| **OneTimeWorkRequest**  | Se ejecuta una vez con condiciones definidas.     | Subir una imagen pendiente.           |
| **PeriodicWorkRequest** | Se repite cada cierto tiempo (mínimo 15 minutos). | Actualizar datos del clima cada hora. |

### Conceptos básicos

-   **Constraints:** definen condiciones (red, batería, carga, etc.).
-   **BackoffPolicy:** define reintentos (exponencial o lineal).
-   **UniqueWork:** evita trabajos duplicados.
-   **Tags:** permiten identificar o cancelar grupos de trabajos.
-   **Data:** comunicación entrada/salida entre Workers.
-   **Foreground dentro del Worker:** usa `setForeground(ForegroundInfo)` para tareas largas que deben ser visibles (notificación persistente).

### Dependencias y permisos mínimos

```kotlin
// build.gradle (Module)
dependencies {
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Ejemplo completo con `CoroutineWorker`

```kotlin
class UploadLogsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString("logPath") ?: return Result.failure()

        return try {
            uploadFile(filePath)
            Result.success(workDataOf("status" to "done"))
        } catch (e: IOException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to e.message))
        }
    }

    private suspend fun uploadFile(path: String) {
        delay(2000) // simulación de red
    }
}
```

### Encolado del trabajo

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresCharging(true)
    .setRequiresBatteryNotLow(true)
    .build()

val uploadRequest = OneTimeWorkRequestBuilder<UploadLogsWorker>()
    .setConstraints(constraints)
    .setInputData(workDataOf("logPath" to "/storage/logs/app.log"))
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        30, TimeUnit.SECONDS
    )
    .addTag("uploadLogs")
    .build()

WorkManager.getInstance(context)
    .enqueueUniqueWork("uploadLogsWork", ExistingWorkPolicy.KEEP, uploadRequest)
```

### Observación en Compose

```kotlin
@Composable
fun UploadStatusScreen() {
    val wm = WorkManager.getInstance(LocalContext.current)
    val workInfos by wm.getWorkInfosByTagLiveData("uploadLogs").observeAsState(emptyList())

    val text = when (val info = workInfos.firstOrNull()) {
        null -> "Esperando..."
        else -> when (info.state) {
            WorkInfo.State.ENQUEUED -> "En cola"
            WorkInfo.State.RUNNING -> "Subiendo..."
            WorkInfo.State.SUCCEEDED -> "Completado"
            WorkInfo.State.FAILED -> "Falló"
            WorkInfo.State.CANCELLED -> "Cancelado"
            else -> "Bloqueado"
        }
    }

    Text(text)
}
```

---

## 4) Caso de uso: sincronización de datos offline → online (app de notas)

**Escenario:** el usuario edita notas sin conexión.  
**Solución:** cada inserción/edición en local dispara un `OneTimeWorkRequest` con restricción de red conectada.  
**Pasos funcionales:**

1. Guardar cambios en Room.
2. Encolar Worker con `NetworkType.CONNECTED`.
3. Si falla la API → `Result.retry()` con backoff exponencial.
4. Al éxito, marcar nota como sincronizada.

---

## 5) Caso de uso: sincronización periódica (app del clima)

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

val weatherSync = PeriodicWorkRequestBuilder<WeatherWorker>(
    1, TimeUnit.HOURS,
    15, TimeUnit.MINUTES // flex interval
)
    .setConstraints(constraints)
    .addTag("weatherSync")
    .build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        "weatherUpdates",
        ExistingPeriodicWorkPolicy.UPDATE,
        weatherSync
    )
```

**Notas:** Android agrupa trabajos para ahorrar batería; intervalo mínimo 15 min.

---

## 6) Caso de uso: subida de archivos grandes (app de almacenamiento)

```kotlin
class UploadFileWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val file = inputData.getString("filePath") ?: return Result.failure()

        setForeground(createForegroundInfo(0))

        try {
            for (i in 0..100 step 10) {
                setForeground(createForegroundInfo(i))
                setProgress(workDataOf("progress" to i))
                delay(500)
            }
            // Lógica real de subida aquí
            return Result.success()
        } catch (e: IOException) {
            return Result.retry()
        }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val channelId = "upload_channel"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Subiendo archivo...")
            .setContentText("Progreso: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        return ForegroundInfo(101, notification)
    }
}
```

---

## 7) Caso de uso: encadenamiento de tareas (app fitness)

**Meta:** `Leer pasos → Calcular calorías → Subir estadísticas`

```kotlin
val readSteps = OneTimeWorkRequestBuilder<ReadStepsWorker>().build()
val calculateCalories = OneTimeWorkRequestBuilder<CalculateWorker>().build()
val uploadStats = OneTimeWorkRequestBuilder<UploadStatsWorker>().build()

WorkManager.getInstance(context)
    .beginUniqueWork("dailySync", ExistingWorkPolicy.REPLACE, readSteps)
    .then(calculateCalories)
    .then(uploadStats)
    .enqueue()
```

**Beneficios:** orden garantizado, aislamiento por pasos, fácil observación y cancelación.

---

## 8) Caso de uso: cancelación de tareas (app de galería)

Cancelar por **tag** (ej. “uploadAlbum”) o por **unique work**:

```kotlin
WorkManager.getInstance(context).cancelAllWorkByTag("uploadAlbum")
// o
WorkManager.getInstance(context).cancelUniqueWork("dailySync")
```

**¿Cuándo cancelar?** cuando el usuario detiene una subida, cambia de cuenta o la tarea ya no aplica.

---

## 9) Caso de uso: pruebas automatizadas (app de sincronización empresarial)

```kotlin
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    @Test
    fun syncCompletesSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<UploadLogsWorker>(context)
            .setInputData(workDataOf("logPath" to "/storage/logs/app.log"))
            .build()

        val result = runBlocking { worker.startWork().get() }
        assertThat(result, `is`(Result.success()))
    }
}
```

**Ventajas:** reproducibilidad, CI/CD, sin dependencias de red real.

---

## 10) Errores comunes y checklist

| Error                                        | Descripción                        | Solución                    |
| -------------------------------------------- | ---------------------------------- | --------------------------- |
| Usar `Service` para tareas largas invisibles | Android mata el proceso            | Usa `WorkManager`           |
| Configurar `PeriodicWorkRequest` < 15 min    | El sistema lo ignora               | Intervalo mínimo: 15 min    |
| No usar `unique work`                        | Se duplican trabajos               | Usa `enqueueUniqueWork`     |
| Olvidar `Constraints`                        | Ejecuta en condiciones no deseadas | Define red/carga/batería    |
| No manejar reintentos                        | Fallos silenciosos                 | `BackoffPolicy.EXPONENTIAL` |
| No observar progreso                         | Mala UX                            | `LiveData`/`Flow` + Compose |
| No cancelar correctamente                    | Procesos colgados                  | `cancelAllWorkByTag`        |
| No probar Workers                            | Errores no detectados              | `WorkManagerTestInitHelper` |

**Checklist final:**

-   [x] Seleccionaste correctamente entre Service y WorkManager.
-   [x] Definiste restricciones y backoff.
-   [x] Evitaste duplicados con unique work.
-   [x] Mostraste progreso/estado al usuario.
-   [x] Probaste comportamiento offline y con Doze.
