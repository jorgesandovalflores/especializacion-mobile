# Módulo 03 · Sesión 04 — UI Reactiva con Datos Remotos (Compose)

## Objetivos de aprendizaje

1. Observar datos **reactivos** desde un `ViewModel` usando `Flow`/`StateFlow`.
2. Modelar **estados de carga** (`loading`, `success`, `error`) de forma tipada y consistente.
3. Implementar **renderizado condicional** en Jetpack Compose evitando estados incoherentes.
4. Diseñar una **capa de datos** con repositorio remoto y cache opcional.
5. Realizar **pruebas** con _Fake APIs_ y **MockWebServer** (instrumentación mínima y unit tests).

---

## Contenido de la clase

1. Patrón de estados UI: `UiState` y `Resource` (o `Result`).
2. ViewModel reactivo con `StateFlow` y `viewModelScope`.
3. Renderizado condicional en Compose (`when`, `Crossfade`, _empty/error states_).
4. Diseño de repositorio remoto (Retrofit/OkHttp) + mapeo a dominio.
5. Mecanismos de reintento y _retry_ por intención del usuario.
6. Pruebas: _Fake API_ (para previews) y **MockWebServer** (para unit tests).
7. Buenas prácticas: unidireccionalidad de datos, cancelaciones, _idempotency_ de intents.

---

## Desarrollo de la clase

### 1) Modelo de estados UI

Definiremos dos niveles de modelado:

-   **Resource<T>**: estado de _datos_ a nivel de repositorio (loading/success/error).
-   **UiState**: estado de _pantalla_ (incluye flags de interacción y datos ya preparados para UI).

```kotlin
sealed class Resource<out T> {
    // Representa carga en progreso
    data object Loading : Resource<Nothing>()

    // Éxito con datos
    data class Success<T>(val data: T) : Resource<T>()

    // Error con causa y mensaje legible
    data class Error(
        val throwable: Throwable,
        val readableMessage: String
    ) : Resource<Nothing>()
}
```

```kotlin
// Estado de pantalla listo para Compose
sealed interface UsersUiState {
    // Primera carga
    data object Loading : UsersUiState

    // Lista vacía (éxito sin datos)
    data object Empty : UsersUiState

    // Lista con datos
    data class Success(val items: List<UserUiModel>) : UsersUiState

    // Error con opción de reintentar
    data class Error(val message: String) : UsersUiState
}
```

```kotlin
data class UserUiModel(
    val id: String,
    val displayName: String,
    val email: String
)
```

---

### 2) Capa de datos (remota)

Ejemplo mínimo con Retrofit. Puedes intercambiar por cualquier cliente HTTP.

```kotlin
// DTO — datos crudos de red
data class UserDto(
    val id: String,
    val name: String,
    val email: String
)

interface UsersApi {
    // Endpoint hipotético
    @GET("/users")
    suspend fun listUsers(): List<UserDto>
}

// Mapper a modelo UI (o a dominio si lo tienes separado)
fun UserDto.toUi(): UserUiModel = UserUiModel(
    id = id,
    displayName = name,
    email = email
)
```

Repositorio que emite `Flow<Resource<List<UserUiModel>>>`:

```kotlin
interface UsersRepository {
    // Exposición reactiva para la UI
    fun fetchUsers(): Flow<Resource<List<UserUiModel>>>
}

class UsersRepositoryImpl(
    private val api: UsersApi
) : UsersRepository {
    override fun fetchUsers(): Flow<Resource<List<UserUiModel>>> = flow {
        // Emitimos Loading al comenzar
        emit(Resource.Loading)
        try {
            val remote = api.listUsers().map { it.toUi() }
            emit(Resource.Success(remote))
        } catch (t: Throwable) {
            // Mapear error a mensaje legible (simple)
            val readable = t.message ?: "Unexpected error"
            emit(Resource.Error(t, readable))
        }
    }
}
```

---

### 3) ViewModel reactivo con `StateFlow`

El VM convierte `Resource` → `UsersUiState` y maneja _intents_ como `refresh()` o `retry()`.

```kotlin
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repo: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val uiState: StateFlow<UsersUiState> = _uiState

    init {
        // Primera carga
        refresh()
    }

    fun refresh() {
        // Cancelamos cargas previas si fuese necesario
        viewModelScope.launch {
            repo.fetchUsers().collect { res ->
                when (res) {
                    is Resource.Loading -> _uiState.value = UsersUiState.Loading
                    is Resource.Success -> {
                        val items = res.data
                        _uiState.value = if (items.isEmpty()) UsersUiState.Empty
                        else UsersUiState.Success(items)
                    }
                    is Resource.Error -> {
                        _uiState.value = UsersUiState.Error(res.readableMessage)
                    }
                }
            }
        }
    }

    fun retry() = refresh() // Reutilizamos intención
}
```

---

### 4) Renderizado condicional en Compose

Pantalla que observa `uiState` y reacciona.

```kotlin
@Composable
fun UsersScreen(
    vm: UsersViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    UsersScaffold(
        state = state,
        onRetry = vm::retry,
        onRefresh = vm::refresh
    )
}
```

```kotlin
@Composable
fun UsersScaffold(
    state: UsersUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit
) {
    // Contenedor básico; aquí podrías usar SwipeRefresh si lo deseas
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is UsersUiState.Loading -> LoadingState()
            is UsersUiState.Empty -> EmptyState(onRefresh)
            is UsersUiState.Success -> UsersList(state.items)
            is UsersUiState.Error -> ErrorState(state.message, onRetry)
        }
    }
}
```

```kotlin
@Composable
fun LoadingState() {
    // Indicador de progreso centrado
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    // Mensaje y acción para recargar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No hay usuarios disponibles")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRefresh) { Text("Actualizar") }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ocurrió un error")
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
fun UsersList(items: List<UserUiModel>) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(items) { user ->
            ListItem(
                headlineContent = { Text(user.displayName) },
                supportingContent = { Text(user.email) }
            )
            Divider()
        }
    }
}
```

> Nota: Para _pull-to-refresh_ puedes integrar `accompanist-swiperefresh` o `pullRefresh` de Material 3, mapeando `Loading` al indicador.

---

### 5) Fake API para previews y pruebas de UI

Un _Fake_ permite desacoplar la UI del backend.

```kotlin
class FakeUsersApi : UsersApi {
    override suspend fun listUsers(): List<UserDto> {
        // Simula latencia
        delay(400)
        return listOf(
            UserDto("1", "Ada Lovelace", "ada@computing.io"),
            UserDto("2", "Alan Turing", "alan@computing.io")
        )
    }
}

class FakeUsersRepository : UsersRepository {
    override fun fetchUsers(): Flow<Resource<List<UserUiModel>>> = flow {
        emit(Resource.Loading)
        delay(300)
        emit(
            Resource.Success(
                listOf(
                    UserUiModel("1", "Ada Lovelace", "ada@computing.io"),
                    UserUiModel("2", "Alan Turing", "alan@computing.io")
                )
            )
        )
    }
}
```

---

### 6) Pruebas con MockWebServer

Ejemplo unit test del repositorio.

```kotlin
// build.gradle (module): agrega dependencia
// testImplementation("com.squareup.okhttp3:mockwebserver:<version>")
```

```kotlin
class UsersRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: UsersApi
    private lateinit var repo: UsersRepository

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        api = retrofit.create(UsersApi::class.java)
        repo = UsersRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `emit Loading then Success`() = runTest {
        val body = """
            [
              {"id":"1","name":"Ada","email":"ada@computing.io"},
              {"id":"2","name":"Alan","email":"alan@computing.io"}
            ]
        """.trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val emissions = mutableListOf<Resource<List<UserUiModel>>>()
        repo.fetchUsers().toList(emissions)

        assertTrue(emissions.first() is Resource.Loading)
        val last = emissions.last()
        assertTrue(last is Resource.Success && last.data.size == 2)
    }

    @Test
    fun `emit Loading then Error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val emissions = mutableListOf<Resource<List<UserUiModel>>>()
        repo.fetchUsers().toList(emissions)

        assertTrue(emissions.first() is Resource.Loading)
        assertTrue(emissions.last() is Resource.Error)
    }
}
```

---

### 7) Buenas prácticas

-   **Unidireccionalidad**: `Intent → ViewModel → StateFlow → UI`. No actualices estado UI desde la vista directamente.
-   **Idempotencia**: evita múltiples cargas simultáneas; deshabilita botones durante `Loading`.
-   **Cancelaciones**: nuevas cargas deben cancelar la anterior cuando aplique.
-   **Errores legibles**: provee mensajes para red, timeouts y validaciones.
-   **Vacíos explícitos**: diferencia _Empty_ de _Error_.
-   **Previews realistas**: usa _Fakes_ con latencia simulada.

---

## Laboratorio guiado (paso a paso)

> Objetivo: Construir una pantalla `UsersScreen` que consume un endpoint `/users`, modela estados y soporta reintento.

1. **Dependencias** (Gradle módulo `app`):

```kotlin
implementation("androidx.lifecycle:lifecycle-runtime-ktx:<latest>")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:<latest>")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:<latest>")
implementation("com.squareup.retrofit2:retrofit:<latest>")
implementation("com.squareup.retrofit2:converter-moshi:<latest>")
implementation("com.squareup.okhttp3:okhttp:<latest>")
// Compose
implementation("androidx.compose.ui:ui:<latest>")
implementation("androidx.compose.material3:material3:<latest>")
implementation("androidx.activity:activity-compose:<latest>")
// Tests
testImplementation("junit:junit:<latest>")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:<latest>")
testImplementation("com.squareup.okhttp3:mockwebserver:<latest>")
```

2. **Crear `UsersApi` y `UsersRepositoryImpl`** (como arriba).

3. **Configurar Retrofit** (p. ej., en un módulo DI o _provider_ simple para el laboratorio):

```kotlin
object NetworkModule { // Simplificado para el lab
    fun provideRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    fun provideUsersApi(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)
}
```

4. **ViewModel y UI**: usa el código de `UsersViewModel` y Composables.

5. **Conectar todo en `Activity`/`NavHost`**:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Para demo: instancia manual (en producción usa Hilt)
                val retrofit = NetworkModule.provideRetrofit("https://example.org")
                val api = NetworkModule.provideUsersApi(retrofit)
                val repo = UsersRepositoryImpl(api)
                val vm = remember { UsersViewModelPreview(repo) }

                UsersScaffoldHost(vm)
            }
        }
    }
}

@Composable
private fun UsersScaffoldHost(repoVm: UsersViewModelPreview) {
    val state by repoVm.uiState.collectAsState()
    UsersScaffold(state, onRetry = repoVm::retry, onRefresh = repoVm::refresh)
}

// VM sin Hilt para laboratorio rápido
class UsersViewModelPreview(private val repo: UsersRepository) : ViewModel() {
    private val _ui = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val uiState: StateFlow<UsersUiState> = _ui
    init { refresh() }
    fun refresh() { viewModelScope.launch { repo.fetchUsers().collect { r ->
        _ui.value = when (r) {
            is Resource.Loading -> UsersUiState.Loading
            is Resource.Success -> if (r.data.isEmpty()) UsersUiState.Empty else UsersUiState.Success(r.data)
            is Resource.Error -> UsersUiState.Error(r.readableMessage)
        }
    } } }
    fun retry() = refresh()
}
```

6. **Pruebas con MockWebServer**: copia los tests de ejemplo, ajusta `baseUrl` al `server.url("/")`.

7. **Extensiones útiles (opcional)**:

```kotlin
// Mapear Throwable → mensaje legible (simple)
fun Throwable.toReadableMessage(): String = when (this) {
    is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
    is java.net.UnknownHostException -> "Sin conexión a internet"
    else -> message ?: "Error inesperado"
}
```

---

## Checklist de verificación

-   ViewModel expone **solo** `StateFlow<UiState>`.
-   La UI **no** dispara múltiples cargas simultáneas.
-   Estados **Empty** vs **Error** diferenciados.
-   Botones deshabilitados en **Loading**.
-   Previews y pruebas ejecutan con **Fake**/MockWebServer.

---

## Material de referencia

-   Documentación oficial: _Architecture guidance_, _UI state in Compose_, _Kotlin Flow_.
-   Código del laboratorio: adaptar a tu estructura `core/data/domain/presentation`.
