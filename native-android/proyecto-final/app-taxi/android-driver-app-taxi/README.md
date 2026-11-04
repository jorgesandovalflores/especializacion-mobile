# Android App Taxi Conductor — labores, solicitudes, ofertas y viaje en tiempo real

App Android nativa (Kotlin + Jetpack Compose) para el **conductor** de App Taxi. Es la contraparte de la app del pasajero ([`../android-app-taxi`](../android-app-taxi/README.md)) y trabaja contra el mismo backend NestJS ([`../backend-app-taxi`](../backend-app-taxi/README.md)).

El conductor inicia sesión con **teléfono y contraseña** y llega a un **mapa a pantalla completa** con su ubicación. Al pulsar **«Iniciar labores»**, un **Foreground Service** de tipo `location` empieza a enviar su posición cada ~3 s por **Socket.IO**. Mientras está en labores ve las **solicitudes cercanas** en tiempo real, abre una, ajusta el precio de a S/ 0.50 y **envía una oferta**. Si el pasajero la acepta, el viaje pasa a ser el **viaje activo**: la ruta se dibuja en el mapa y un solo botón lo lleva por los estados `ACCEPTED → ARRIVED → IN_PROGRESS → COMPLETED`. Al terminar **califica al pasajero** y vuelve a estar disponible. Si cierra la app y la vuelve a abrir, el estado se restaura con `GET /driver/trips/current`.

Reutiliza la arquitectura, la línea gráfica y la capa de red de la app del pasajero: Clean Architecture + MVVM por feature, Hilt, sesión cifrada con Keystore, renovación de token con `Authenticator`, `ErrorMapper`, tema claro/oscuro con `ColorPrimary #7843E6` y textos en español e inglés.

---

## 1. Stack técnico y arquitectura

| Área | Tecnología | Versión | Para qué se usa |
| --- | --- | --- | --- |
| Build | Android Gradle Plugin / Gradle Wrapper | 9.3.3 / 9.7.1 | Igual que la app del pasajero |
| Lenguaje | Kotlin / KSP | 2.4.20 / 2.3.12 | KSP genera el código de Hilt |
| UI | Jetpack Compose (BOM) + Material 3 + `material-icons-core` | 2026.09.00 | Pantallas, paneles y componentes |
| Navegación | Navigation Compose | 2.10.1 | `splash` → `sign_in` → `home` |
| DI | Hilt + `hilt-lifecycle-viewmodel-compose` | 2.60.1 / 1.4.0 | Módulos por feature y `hiltViewModel()` |
| Red | Retrofit + Gson + OkHttp (`logging-interceptor`) | 3.0.0 / 5.5.0 | API REST del conductor |
| Tiempo real | `io.socket:socket.io-client` (sin `org.json:json`, Android ya lo trae) | 2.1.2 | Eventos de viaje y envío de ubicación |
| Asincronía | Kotlin Coroutines + Flow | 1.11.0 | Estados, eventos y casos de uso |
| Ubicación | `play-services-location` + `kotlinx-coroutines-play-services` | 21.4.0 | `FusedLocationProviderClient` cada 3 s |
| Mapa | Maps Compose | 9.0.1 | `GoogleMap`, `Polyline`, `MarkerComposable`, `ComposeMapColorScheme.FOLLOW_SYSTEM` |
| Pruebas | JUnit 4 + `kotlinx-coroutines-test` con fakes | 4.13.2 / 1.11.0 | Pruebas JVM |
| SDK | compileSdk / targetSdk / minSdk | 37 / 37 / 29 | — |

No usa Room, Firebase, Coil ni DataStore. La sesión va en `SharedPreferences` cifradas y el perfil en `SharedPreferences` simples.

La arquitectura es la misma que en la app del pasajero:

- **presentation**: composables `…Route` (Hilt, efectos, navegación) y `…Screen` (solo estado y callbacks), ViewModels con `StateFlow`.
- **domain**: modelos, reglas puras (`FareRules`, `TripFlow`, `PolylineDecoder`), interfaces de repositorio y casos de uso. Sin Android.
- **data**: Retrofit, DTOs y mappers, Socket.IO, GPS y servicio en primer plano.

---

## 2. Estructura de carpetas

```
com/example/driver/
├─ AppMain.kt                          // @HiltAndroidApp
├─ core/
│  ├─ data/                            // NetworkModule, HeadersInterceptor, AuthInterceptor, TokenAuthenticator,
│  │                                   // TokenRefresher, RefreshApi (driver/auth/refresh), ErrorMapper, SafeCall,
│  │                                   // SecurityModule, SessionStoreEncryptedPrefs, SessionExpirationImpl, CoroutineModule
│  │  └─ realtime/                     // SocketIoRealtimeClient + RealtimeModule (singleton)
│  ├─ domain/                          // SessionStore, SessionExpiration, LocalCache, DomainException
│  │  └─ realtime/                     // RealtimeGateway, ConnectionState
│  └─ presentation/                    // activity/MainActivity (NavHost), theme/ (Color, Theme, Type)
├─ commons/
│  ├─ data/                            // CommonsModule, local/DriverStorePrefs, remote/dto/DriverDto
│  ├─ domain/                          // model/Driver + Vehicle, store/DriverStore
│  └─ presentation/                    // PrimaryButton, TextInputField, PhoneInputField, PasswordInputField,
│                                      // AppToast/ToastHost, SystemBars, Motion, Adaptive, PhoneFormat
└─ features/
   ├─ splash/{domain,presentation}/ + SplashModule.kt
   ├─ signin/{data,domain,presentation}/ + SignInModule.kt     // login teléfono + contraseña
   └─ home/{data,domain,presentation}/ + HomeModule.kt         // mapa, labores, solicitudes, viaje, calificación
      ├─ data/remote/                  // DriverTripsApi + DTOs
      ├─ data/realtime/                // TripEventsRepositoryImpl, DriverLocationPublisherImpl, RealtimeEvents
      ├─ data/location/                // LocationRepositoryImpl (FusedLocationProvider)
      ├─ data/tracking/                // LocationTrackingService, TrackingNotification, LocationTrackerImpl
      ├─ domain/rules/                 // FareRules, TripFlow, PolylineDecoder
      └─ presentation/                 // HomeScreen, HomePanels, HomeViewModel, HomeUiState, permisos
```

Cada feature con pantallas tiene sus textos en `features/<feature>/presentation/res/values/` (español) y `values-en/` (inglés); `app/build.gradle.kts` registra esas carpetas en `sourceSets["main"].res.directories`. El nombre visible es «App Taxi Conductor» en español y «App Taxi Driver» en inglés.

---

## 3. Sesión, red y tiempo real

**REST.** Igual que en la app del pasajero, hay dos clientes OkHttp: el público (para `POST /driver/auth/login` y `POST /driver/auth/refresh`) y el autenticado, que agrega `Authorization: Bearer` con `AuthInterceptor` y, ante un 401, renueva el token una sola vez con `TokenAuthenticator` + `TokenRefresher` (con `Mutex`). Si el refresh token ya no sirve, `SessionExpirationImpl` borra la sesión y el perfil, y `MainActivity` vuelve al login con un toast rojo. Los errores del backend (`{ status_code, message, errors[] }`) se muestran con el `message` o con el primer `errors[].message`.

**Sesión y perfil.** `SessionStoreEncryptedPrefs` guarda los tokens cifrados con AES-256-GCM (clave en Android Keystore). `DriverStorePrefs` guarda el `DriverDto` del login (nombre, placa, rating) y la marca local «en labores», para reanudar el turno al reabrir la app.

**Socket.IO.** `SocketIoRealtimeClient` es un singleton de Hilt que se conecta a `${API_BASE_URL}realtime` (namespace `/realtime`) así:

```kotlin
val options = IO.Options().apply {
    forceNew = true
    reconnection = true
    transports = arrayOf(WebSocket.NAME)
    auth = mapOf("token" to accessToken)
    callFactory = httpClient
    webSocketFactory = httpClient
}
```

- Expone `connection: StateFlow<ConnectionState>` (el punto de color junto al avatar) y `on(evento): Flow<String>` con el JSON de cada evento. `TripEventsRepositoryImpl` lo convierte en `Flow<TripEvent>` con Gson.
- Si recibe `connect_error` con `message: "unauthorized"`, renueva el token con `TokenRefresher` y vuelve a conectar con un socket nuevo (máximo dos intentos seguidos). Si el refresh es rechazado, expira la sesión.
- Ante una desconexión de red, Socket.IO reconecta solo (1 s a 5 s). Si el servidor cierra el socket (`io server disconnect`), lo reabre a los 3 s.
- Se desconecta al cerrar sesión y cuando el access token desaparece (sesión expirada).

| Evento | Dirección | Qué hace la app |
| --- | --- | --- |
| `session:ready` | servidor → app | No se usa; el estado `Connected` basta |
| `driver:location` | app → servidor | Cada ~3 s mientras está en labores o con un viaje activo: `{ lat, lng, bearing, speed, accuracy }` |
| `trip:requested` | servidor → app | Agrega la solicitud a la lista si está en labores y sin viaje |
| `trip:unavailable` | servidor → app | Quita la solicitud; si tenía una oferta o estaba abierta, avisa «eligió a otro conductor» o «canceló la solicitud» |
| `trip:offer-accepted` | servidor → app | Abre el viaje activo y vacía la lista de solicitudes |
| `trip:status` | servidor → app | Actualiza el viaje activo. `CANCELLED` muestra un toast y vuelve a disponible |

Al conectar (o reconectar) estando en labores, y al iniciar labores, la app llama a `GET /driver/trips/requests` para no perder solicitudes emitidas mientras estaba desconectada.

---

## 4. Flujo de pantallas

| Ruta | Qué muestra | Navega a |
| --- | --- | --- |
| `splash` | Logo con rebote 1,5 s | `home` si hay access token, si no `sign_in` (limpia el back stack) |
| `sign_in` | Teléfono (9 dígitos, prefijo `+51` visible) y contraseña. El botón se habilita con 9 dígitos y contraseña | `home` tras `POST /driver/auth/login`; los errores se muestran en un toast |
| `home` | Mapa a pantalla completa, encabezado del conductor y panel inferior | `sign_in` al cerrar sesión o si la sesión expira |

En **Home**, el encabezado muestra el avatar con la inicial, el estado del socket, el nombre, «placa · marca modelo» y la calificación con estrella. El botón circular de la derecha cierra sesión: si está en labores primero envía `PUT /driver/availability { online: false }`. No deja cerrar sesión durante un viaje.

El panel inferior cambia según `HomeUiState.panel`:

| Panel | Cuándo | Contenido |
| --- | --- | --- |
| `Restoring` | Al abrir, mientras responde `GET /driver/trips/current` | Indicador de carga |
| `Offline` | Fuera de servicio | Interruptor «Iniciar labores» (`PUT /driver/availability { online: true, lat, lng }`) |
| `Requests` | En labores, sin viaje | Interruptor «Terminar labores» y tarjetas con origen, destino, distancia, duración y tarifa sugerida |
| `OfferDraft` | Al tocar una tarjeta | Ruta en el mapa (polyline de precisión 5), precio propuesto = `estimatedFare`, botones − / + de S/ 0.50 (mínimo S/ 1.00) y «Enviar oferta» (`POST /driver/trips/:id/offers`). Después: «Oferta enviada, esperando al pasajero» |
| `ActiveTrip` | `ACCEPTED`, `ARRIVED` o `IN_PROGRESS` | Pasajero (con botón para llamar), origen y destino con marcadores, ruta, precio final y un botón: «Llegué al punto de recojo» → «Iniciar viaje» → «Finalizar viaje» (`PUT /driver/trips/:id/status`) |
| `Rating` | `COMPLETED` sin calificar | Cinco estrellas accesibles, comentario opcional y «Enviar calificación» (`POST /driver/trips/:id/rating`) |

Si falta el permiso de ubicación o el GPS está apagado, en lugar de `Offline` o `Requests` aparece el panel de permisos de la app del pasajero (permitir, abrir ajustes, activar ubicación o reintentar). No se puede iniciar labores sin permiso ni terminarlas con un viaje activo.

**Servicio en primer plano.** `HomeViewModel` decide con `TrackingCommand` cuándo arrancar o detener `LocationTrackingService`: corre si hay permiso y el conductor está en labores **o** tiene un viaje en curso. El servicio muestra la notificación persistente «Estás en labores», pide ubicaciones cada 3 s a `FusedLocationProviderClient` y las publica con `DriverLocationPublisher` (`driver:location`). Usa `stopWithTask="false"` y su propia conexión al socket singleton, así sigue enviando la posición durante todo el viaje aunque la app pase a segundo plano o se quite de recientes. Se detiene al terminar labores, al cerrar sesión o si la sesión expira.

**Accesibilidad y modo oscuro.** Títulos con `heading()`, botones circulares y estrellas de 48 dp con `contentDescription`, interruptor con `Role.Switch` y `stateDescription`, panel con `liveRegion` para que TalkBack anuncie los cambios, toasts asertivos y el mapa con `ComposeMapColorScheme.FOLLOW_SYSTEM`. Con el teléfono horizontal o en tablet, el panel queda flotando a la izquierda con un ancho máximo de 440 dp.

---

## 5. Ejecutar y probar

### Arranque rápido

1. Levanta infra y backend ([`../infra-app-taxi`](../infra-app-taxi/README.md) y [`../backend-app-taxi`](../backend-app-taxi/README.md)). El seed local crea los conductores `51900000001` y `51900000002`.
2. Abre la carpeta `android-driver-app-taxi` en Android Studio (Gradle JDK 17).
3. `API_BASE_URL` es la misma de la app del pasajero (`app/build.gradle.kts`, debe terminar en `/`). Para el emulador con backend local, redirige el puerto por adb y usa el loopback (desde Android 17, una app con `targetSdk 37` no llega a `10.0.2.2` sin el permiso `ACCESS_LOCAL_NETWORK`):
   ```bash
   adb -s emulator-5554 reverse tcp:3001 tcp:3001
   ```
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:3001/\"")
   ```
   El socket usa la misma URL más `realtime`.
4. Agrega `MAPS_API_KEY=...` en `local.properties` (no se versiona). Sin key, la app funciona pero el mapa se ve gris.
5. Ejecuta `app` en un dispositivo o emulador con Google Play services y fija una posición en *Extended controls → Location*.

**Credenciales de prueba del seed:** teléfono `51900000001` (en la pantalla se escribe `900000001`) y contraseña `Driver123*`.

Para probar el flujo completo, abre la app del pasajero en otro dispositivo, pide un viaje cerca de la posición del conductor, oferta desde esta app y acepta la oferta desde la del pasajero.

### Comandos y pruebas

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

| Prueba (JVM) | Qué verifica |
| --- | --- |
| `HomeViewModelTest` (15) | Conexión del socket y perfil al abrir; iniciar labores con la ubicación, carga de solicitudes y arranque del servicio; sin permiso no inicia labores; terminar labores detiene el servicio; `trip:requested` solo en labores y sin duplicados; recarga al reconectar; oferta con precio ajustado y espera; `trip:unavailable`; oferta aceptada y avance `ACCEPTED → ARRIVED → IN_PROGRESS → COMPLETED`; bloqueo de «Terminar labores» con viaje; calificación con comentario recortado; cancelación por el pasajero; servicio activo con un viaje restaurado aunque esté fuera de servicio; `trip:status` del ganador; cerrar sesión |
| `PolylineDecoderTest` (4) | Ejemplo oficial de Google con precisión 5, otra precisión, cadena vacía y cadena truncada |
| `FareRulesTest` (6) | Precio inicial, − / + de S/ 0.50 sin errores de redondeo (con `BigDecimal`), tope en S/ 1.00, oferta inválida que no llega al backend, orden de estados |
| `TripFormatTest` (3) | `S/ 19.76`, `9.8 km`, `13 min` |
| `LoginUseCaseTest` (3) | Prefijo `51`, tokens y perfil guardados, mensaje del backend ante error, validación del formulario |

Las pruebas usan fakes (`FakeTripRepository`, `FakeTripEventsRepository`, `FakeLocationRepository`, `FakeLocationTracker`, `FakeDriverStore`, `FakeSessionStore`) y `UnconfinedTestDispatcher`, como en la app del pasajero. No hay pruebas instrumentadas.

Lint queda sin errores. Solo avisa de versiones más nuevas de dependencias (se mantienen iguales a la app del pasajero) y de `InsecureBaseConfiguration`: HTTP en claro está permitido a propósito para el backend local en desarrollo.
