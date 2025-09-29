# Sesión 2 — Hilt para Inyección de Dependencias

## Objetivos
1. Entender DI y su relación con patrones creacionales, con un ejemplo real (login social).
2. Recorrer la evolución de DI en Android y comparar enfoques en una tabla.
3. Configurar Hilt correctamente (qué es KSP/KAPT y cuándo usar cada uno).
4. Comprender componentes y scopes de Hilt con ejemplos descriptivos.
5. Crear un **módulo de red** con explicación funcional y comentarios técnicos en el código.
6. Dominar `@Binds` vs `@Provides` con ejemplos acertados.
7. Implementar un **caso de uso completo** paso a paso (Perfil de Conductor).
8. Inyectar dependencias en **ViewModels** y usarlas desde **Compose**.
9. Mantener un flujo claro entre UI → ViewModel → Repository → API/HTTP con Hilt.

---

## 1) Introducción: DI y Patrones Creacionales (con ejemplo real)

**Qué es DI:** hacer que las clases **reciban** sus dependencias ya creadas (por un contenedor) en vez de **crearlas dentro**.  
Beneficios: desacople, testabilidad con mocks, reemplazo por entorno/flavor y configuración centralizada.

**Relación con patrones creacionales:**
- **Factory/Abstract Factory**: deciden **cómo** construir; DI decide **quién** construye y **cómo llega** al consumidor.
- **Builder**: útil para armar objetos complejos antes de registrarlos; la **entrega** la resuelve DI.
- **Singleton**: con DI se modela por **scope** (p. ej., `@Singleton`), sin `object` globales.
- **Service Locator (anti-patrón)**: oculta dependencias; DI las hace **explícitas** por constructor.

### Ejemplo real — Login social (Facebook/Google) con qualifiers

```kotlin
// contrato común para cualquier proveedor social
interface SocialLogin {
    suspend fun login(): AuthResult
}

data class AuthResult(val success: Boolean, val token: String?)

// implementación Facebook (ejemplo de uso real con SDK)
class FacebookLogin @Inject constructor(
    private val context: Context
) : SocialLogin {
    override suspend fun login(): AuthResult {
        // TODO: integrar SDK Facebook y devolver resultado real
        return AuthResult(true, "fb-token")
    }
}

// implementación Google (ejemplo de uso real con Google Sign-In)
class GoogleLogin @Inject constructor(
    private val context: Context
) : SocialLogin {
    override suspend fun login(): AuthResult {
        // TODO: integrar Google Sign-In y devolver resultado real
        return AuthResult(true, "g-token")
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FacebookProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleProvider

@Module
@InstallIn(SingletonComponent::class)
object SocialModule {

    // registramos ambas implementaciones como singletons con qualifiers
    @Provides @Singleton @FacebookProvider
    fun provideFacebookLogin(@ApplicationContext ctx: Context): SocialLogin = FacebookLogin(ctx)

    @Provides @Singleton @GoogleProvider
    fun provideGoogleLogin(@ApplicationContext ctx: Context): SocialLogin = GoogleLogin(ctx)
}

// caso de uso elige un proveedor sin acoplar la UI
class AuthService @Inject constructor(
    @FacebookProvider private val social: SocialLogin
) {
    suspend fun signIn(): AuthResult = social.login()
}
```

**Claves:** la UI no crea el proveedor ni lo conoce; cambiar Facebook→Google es cambiar el **binding**, no la pantalla. Testear = inyectar un fake.

---

## 2) Evolución de DI en Android (con tabla comparativa)

1) **Manual/Singletons estáticos:** rápido, pero acoplado y difícil de testear/ciclo de vida.  
2) **Service Locator:** centraliza pero **oculta** dependencias → difícil de razonar.  
3) **Dagger 1/2:** compilación, muy performante, pero verboso en Android.  
4) **Dagger-Android:** ayudó, aunque seguía siendo complejo.  
5) **Koin/Kodein/Toothpick:** ergonómicos (runtime), sin chequeos de compilación.  
6) **Hilt (sobre Dagger):** convenciones Android listas, scopes/componentes integrados, testing sencillo y performance de Dagger.

### Comparativa

| Enfoque | Resolución | Perf. | Boilerplate | Testing | Ciclo de vida | Curva | Cuándo usar |
|---|---|---:|---:|---:|---:|---:|---|
| Manual/Singleton | N/A | Alta | Bajo | Difícil | Manual | Baja | POCs muy simples |
| Service Locator | Runtime | Media | Media | Media/Difícil | Manual | Media | Legado |
| Dagger 2 | Compilación | Muy alta | Alto | Excelente | Requiere diseño | Alta | Apps grandes |
| Dagger-Android | Compilación | Muy alta | Medio/Alto | Excelente | Mejor que Dagger puro | Media/Alta | Legacy con Dagger |
| Koin/Kodein | Runtime | Media | Bajo | Bueno | A mano | Baja/Media | Prototipos |
| **Hilt** | **Compilación** | **Muy alta** | **Bajo/Medio** | **Excelente** | **Integrado** | **Media** | **Android moderno** |

---

## 3) Configuración de Hilt (Gradle, KSP vs KAPT)

**KAPT vs KSP:**
- **KAPT** (Kotlin Annotation Processing Tool): puente con procesadores Java. Estable, pero más lento (genera stubs).
- **KSP** (Kotlin Symbol Processing): procesa símbolos Kotlin nativamente. **Más rápido** e incremental.  
Hilt soporta ambos; **recomendado KSP** si tu stack ya lo usa.

**`app/build.gradle` (KSP recomendado):**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    // ... tu configuración usual (namespace, compileSdk, defaultConfig, etc.)
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
    }
}

dependencies {
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
```

> Si prefieres **KAPT**: cambia `id("com.google.devtools.ksp")` por `kotlin("kapt")` y `ksp(...)` por `kapt(...)`.

---

## 4) `@HiltAndroidApp` (qué hace y por qué importa)

Anotar tu clase `Application` con `@HiltAndroidApp`:
- **Genera** el contenedor raíz en **compilación**.
- Registra componentes/scopes alineados con el **ciclo de vida Android**.
- Habilita `@AndroidEntryPoint` en Activities/Fragments/Services.

```kotlin
// inicializa el grafo raíz de Hilt al arrancar la app
@HiltAndroidApp
class AppMain : Application()
```

`AndroidManifest.xml`:
```xml
<application
    android:name=".AppMain"
    ...>
</application>
```

---

## 5) Componentes y Scopes de Hilt (explicación con ejemplo)

Piensa en **“cada cuánto necesito la misma instancia”**:

- **SingletonComponent / @Singleton**: 1 por app.  
  Ej.: `OkHttpClient`, `Retrofit`, `RoomDatabase`, repos globales.
- **ActivityRetainedComponent / @ActivityRetainedScoped**: persiste durante la **vida lógica** de una Activity (sobrevive a rotaciones).  
  Ej.: `SessionManager` o caches breves atados al flujo de la Activity.
- **ViewModelComponent / @ViewModelScoped**: 1 por ViewModel.  
  Ej.: `CalculateFareUseCase` o un `FilterState`.
- **ActivityComponent / @ActivityScoped** y **FragmentComponent / @FragmentScoped**: 1 por pantalla concreta.  
  Ej.: adaptadores/formatters que viven lo que vive la UI puntual.

---

## 6) Módulo de red (OkHttp + Retrofit): teoría funcional + código comentado

**Funcional (qué resuelve):**
1) Cliente HTTP (**OkHttp**) con **interceptores** (auth/logging).  
2) Conversor JSON (Gson/Moshi).  
3) Cliente de API type-safe (**Retrofit**).  
4) Todo **scopeado** a `@Singleton` para **reutilizar conexiones**.

```kotlin
// contrato para obtener token sin acoplar a almacenamiento concreto
interface TokenProvider {
    fun tokenOrNull(): String?
}

// implementación de ejemplo (sustituye con DataStore/Keychain)
class DataStoreTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenProvider {
    override fun tokenOrNull(): String? {
        // TODO: leer token real de DataStore/Keychain
        return null
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // agrega Authorization si hay token
    @Provides @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenProvider.tokenOrNull()
        val req = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else original
        chain.proceed(req)
    }

    // logging de red (desactiva o reduce nivel en producción)
    @Provides @Singleton
    fun provideLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // OkHttp con nuestros interceptores
    @Provides @Singleton
    fun provideOkHttpClient(
        auth: Interceptor,
        log: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(log)
        .build()

    // Retrofit con Gson y baseUrl desde BuildConfig
    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
```

---

## 7) Repository: `@Binds` vs `@Provides` (explicación ampliada)

**Usa `@Binds` cuando:**
- Solo necesitas **enlazar** una **interfaz** con **su implementación**.
- No hay **lógica** de construcción (la implementación ya es `@Inject`-able).
- Ventaja: **menos boilerplate**, más claro y rápido de compilar.

**Usa `@Provides` cuando:**
- Necesitas **construir** el objeto con **lógica** (ifs, builders, flags).
- No controlas el constructor (librería externa, builder complejo sin `@Inject`).
- Debes **elegir** una implementación en compile-time (feature flags) o mezclar dependencias/qualifiers.

**Ejemplo combinado (enlazar repo + lógica de precios):**
```kotlin
// contrato de repositorio
interface DriverRepository {
    suspend fun getMyProfile(): Driver
}

class DriverRepositoryImpl @Inject constructor(
    private val api: DriverApi
) : DriverRepository {
    override suspend fun getMyProfile(): Driver {
        val dto = api.getMyProfile()
        return Driver(id = dto.id, name = dto.name)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // mapeo interfaz -> implementación sin lógica
    @Binds @Singleton
    abstract fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository
}

// ejemplo de @Provides con lógica para decidir implementación
interface FareCalculator { fun calc(distanceKm: Double): Double }

class PromoFareCalculator @Inject constructor() : FareCalculator {
    override fun calc(distanceKm: Double) = distanceKm * 0.8
}

class DefaultFareCalculator @Inject constructor() : FareCalculator {
    override fun calc(distanceKm: Double) = distanceKm * 1.0
}

@Module
@InstallIn(SingletonComponent::class)
object PricingModule {

    // si hay flag de promo, inyecta la versión promocional
    @Provides @Singleton
    fun provideFareCalculator(): FareCalculator {
        return if (BuildConfig.ENABLE_PROMO_PRICING) PromoFareCalculator() else DefaultFareCalculator()
    }
}
```

**Regla práctica:**  
- **Binds** = “solo mapeo” (interfaz→impl).  
- **Provides** = “tengo que construir/decidir” (lógica, builders, librerías).

---

## 8) Inyección en ViewModels y Compose

- Los **hosts** (Activity/Fragment/Service) que reciben inyección llevan `@AndroidEntryPoint`.
- Los **ViewModels** llevan `@HiltViewModel` y reciben dependencias por constructor.
- En **Compose**, se obtiene el VM con `hiltViewModel()` (no se inyecta directamente en Composables).

```kotlin
// Activity host con Hilt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DriverScreen() }
    }
}

// API de ejemplo
interface DriverApi {
    @GET("drivers/me")
    suspend fun getMyProfile(): DriverDto
}

data class DriverDto(val id: String, val name: String)
data class Driver(val id: String, val name: String)

data class DriverUiState(
    val loading: Boolean = false,
    val data: Driver? = null,
    val error: String? = null
)

// ViewModel inyectado
@HiltViewModel
class DriverViewModel @Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(DriverUiState())
    val ui: StateFlow<DriverUiState> = _ui

    fun loadProfile() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val driver = repository.getMyProfile()
                _ui.update { it.copy(loading = false, data = driver) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}

// pantalla Compose que consume el VM
@Composable
fun DriverScreen(vm: DriverViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.loadProfile() }

    when {
        ui.loading -> CircularProgressIndicator()
        ui.error != null -> Text("Error: ${ui.error}")
        ui.data != null -> Text("Welcome, ${ui.data!!.name}")
        else -> Text("No data")
    }
}
```

---

## 9) Caso de uso completo — “Perfil de Conductor” (paso a paso claro)

**Objetivo funcional:** al abrir la pantalla, obtener el perfil del conductor autenticado vía API y mostrar su nombre.  
**Arquitectura:** Compose (UI) → ViewModel (estado) → Repository (negocio/mapeo) → Retrofit API (red) → OkHttp (interceptores).

### Paso 0 — Base del proyecto
- `@HiltAndroidApp` en `Application` y `@AndroidEntryPoint` en la Activity host.
- `BuildConfig.API_BASE_URL` definido por flavor.
- NetworkModule configurado (OkHttp + interceptores + Retrofit).

### Paso 1 — Contratos de datos
```kotlin
// DTO recibido por la red
data class DriverDto(val id: String, val name: String)

// modelo de dominio para UI/negocio
data class Driver(val id: String, val name: String)
```

### Paso 2 — API Retrofit
```kotlin
// endpoint del perfil
interface DriverApi {
    @GET("drivers/me")
    suspend fun getMyProfile(): DriverDto
}
```

### Paso 3 — Repositorio (mapea DTO→dominio)
```kotlin
interface DriverRepository {
    suspend fun getMyProfile(): Driver
}

class DriverRepositoryImpl @Inject constructor(
    private val api: DriverApi
) : DriverRepository {
    override suspend fun getMyProfile(): Driver {
        val dto = api.getMyProfile()
        return Driver(id = dto.id, name = dto.name)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository
}
```

### Paso 4 — ViewModel (gestiona estado y errores)
```kotlin
@HiltViewModel
class DriverViewModel @Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(DriverUiState())
    val ui: StateFlow<DriverUiState> = _ui

    fun loadProfile() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val driver = repository.getMyProfile()
                _ui.update { it.copy(loading = false, data = driver) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "Network error") }
            }
        }
    }
}
```

### Paso 5 — UI Compose (observa `ui` y reacciona)
```kotlin
@Composable
fun DriverScreen(vm: DriverViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.loadProfile() }

    when {
        ui.loading -> CircularProgressIndicator()
        ui.error != null -> Text("Error: ${ui.error}")
        ui.data != null -> Text("Welcome, ${ui.data!!.name}")
        else -> Text("No data")
    }
}
```

**Qué aprendemos (y por qué así):**
- La UI **no** conoce red ni tokens. Solo lee `ui`.
- El VM **orquesta** y maneja errores/estados.
- El Repo **encapsula** Retrofit y mapea DTO→dominio (si mañana cambias el API, no tocas la UI).
- OkHttp/Retrofit viven en **`@Singleton`** para rendimiento.
- Cambios de proveedor (p. ej., base URLs, auth) se hacen en **módulos**/bindings, no en la UI.

**Variaciones realistas:**
- Agregar **qualifiers** para `PublicApi` y `InternalApi`.
- Inyectar un `TokenProvider` real con DataStore.
- Pagination/Retry: extender el Repo (la UI no cambia).
