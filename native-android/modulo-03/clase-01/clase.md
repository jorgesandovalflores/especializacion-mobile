# Módulo 3 · Sesión 1 — Room con Flow

## Objetivo

Al finalizar esta sesión, el estudiante será capaz de:

1. Diferenciar **SQLite** nativo vs **Room** (por qué existe y qué resuelve).
2. Configurar Room en un proyecto Android con **Kotlin Coroutines + Flow** e **Hilt**.
3. Definir **entidades, DAOs y relaciones** básicas, e integrar **Retrofit** para sincronizar datos.
4. Implementar un **patrón cache-first**: mostrar datos desde DB local de inmediato y **refrescar** desde red en segundo plano.
5. Aplicar **buenas prácticas** y manejar **migraciones** de esquema.

---

## Contenido de la clase

1. Teoría: SQLite en Android antes de Room → qué es Room y por qué usarlo.
2. Instalación y dependencias (Room, Retrofit/OkHttp, Coroutines/Flow, Hilt).
3. Modelado de dominio: **MenuOption** (id, text, icon, deeplink).
4. Room: **Entity**, **Dao**, **Database** (versionado, índices, converters).
5. Red: **Retrofit Service** y DTO.
6. Mapeo DTO ↔ Entity ↔ Domain.
7. Repositorio con **Flow** y estrategia **cache-first + refresh** (Network-Bound).
8. Caso de uso y ViewModel con **StateFlow**.
9. UI de ejemplo (Compose) observando cambios de Flow.
10. Buenas prácticas y **migraciones** (auto/explicitas, testing de migraciones).

---

## Desarrollo del contenido

### 1) Teoría: de SQLite a Room

#### 1.1. Contexto histórico: el manejo de datos en Android antes de Room

Antes de que Jetpack Room existiera, la forma tradicional de persistir datos estructurados en Android era a través de **SQLiteOpenHelper** y el uso directo del motor **SQLite**.

```kotlin
class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "mydb", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE menu_option (id TEXT PRIMARY KEY, text TEXT, icon TEXT, deeplink TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS menu_option")
        onCreate(db)
    }
}
```

Este enfoque tenía varios **problemas estructurales**:

1. **Código imperativo y repetitivo**: cada CRUD requería escribir consultas SQL manuales.
2. **Falta de tipado**: los errores de SQL solo se detectaban en tiempo de ejecución.
3. **Gestión manual de cursores** (`Cursor`) y valores (`ContentValues`), propenso a errores y fugas de memoria.
4. **Conversión manual de tipos** (por ejemplo, `Int` ↔ `Boolean`, `Date` ↔ `Long`).
5. **Dificultad para mantener migraciones** sin perder datos entre versiones de esquema.
6. **Poca integración con las arquitecturas modernas (MVVM, Clean, etc.)**.
7. **Sin soporte nativo para corrutinas ni flujos reactivos**, lo que obligaba a usar `AsyncTask` o `Executors`.

Ejemplo típico con `SQLiteOpenHelper`:

```kotlin
val cursor = db.rawQuery("SELECT * FROM menu_option", null)
while (cursor.moveToNext()) {
    val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
}
cursor.close()
```

Este tipo de código era **verboso, difícil de testear y mantener**.

---

#### 1.2. Nacimiento de Room

Con la evolución de **Jetpack Architecture Components**, Google introdujo **Room** como un _ORM ligero_ (Object-Relational Mapping) para Android.  
Su objetivo principal: **hacer que el acceso a datos fuera seguro, limpio y fácil de mantener.**

**Room** se apoya en **anotaciones** (`@Entity`, `@Dao`, `@Query`) y genera automáticamente el código SQL y los _mappers_ necesarios.

Ejemplo equivalente con Room:

```kotlin
@Entity(tableName = "menu_option")
data class MenuOptionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val icon: String,
    val deeplink: String
)

@Dao
interface MenuOptionDao {
    @Query("SELECT * FROM menu_option")
    fun observeAll(): Flow<List<MenuOptionEntity>>
}
```

Room traduce automáticamente esta consulta SQL y la valida en **tiempo de compilación**, asegurando que el nombre de la tabla, las columnas y los tipos coincidan.  
Esto elimina una categoría completa de errores comunes.

---

#### 1.3. Arquitectura de Room

Room está compuesto por tres capas principales:

| Componente    | Descripción                                                              | Ejemplo            |
| ------------- | ------------------------------------------------------------------------ | ------------------ |
| **@Entity**   | Representa una tabla de base de datos.                                   | `MenuOptionEntity` |
| **@Dao**      | Define las operaciones de acceso a datos (consultas, inserciones, etc.). | `MenuOptionDao`    |
| **@Database** | Punto de entrada principal, vincula DAOs y controla las migraciones.     | `AppDatabase`      |

**Flujo de uso típico:**

```
App → Repository → DAO → Room → SQLite
```

Y a su vez, Room puede integrarse con:

-   **Coroutines/Flow** → ejecución asíncrona y reactiva.
-   **Hilt** → inyección de dependencias para DAOs y DB.
-   **Compose y LiveData** → observación automática de cambios.
-   **WorkManager** → sincronización de datos offline.

---

#### 1.4. Integración con Flow y ciclo de vida

Room tiene soporte nativo para **Flow**.  
Cada vez que cambian los datos en la tabla observada, el Flow **emite automáticamente un nuevo valor**:

```kotlin
@Query("SELECT * FROM menu_option")
fun observeAll(): Flow<List<MenuOptionEntity>>
```

Ventajas:

-   Se integra naturalmente con `viewModelScope` y `collectAsState()` en Compose.
-   Se actualiza la UI automáticamente sin recargar manualmente.
-   Optimiza el rendimiento al no reemitir datos innecesarios.

---

#### 1.5. Ventajas de Room frente a SQLite tradicional

| Categoría                              | SQLite tradicional            | Room                           |
| -------------------------------------- | ----------------------------- | ------------------------------ |
| **Consultas SQL**                      | Manuales y sin verificación   | Verificadas en compilación     |
| **Mapeo de objetos**                   | Manual con cursores           | Automático con entidades       |
| **Migraciones**                        | Difíciles y propensas a error | Controladas con `Migration`    |
| **Compatibilidad con Flow/Coroutines** | No disponible                 | Integración nativa             |
| **Testing**                            | Complejo                      | Simplificado con in-memory DB  |
| **Compatibilidad con Hilt**            | Manual                        | Integración directa            |
| **Seguridad**                          | Riesgo de SQL injection       | Prevenido por bindings de Room |

---

#### 1.6. Limitaciones y consideraciones

Room **no reemplaza completamente SQLite**.  
Sigue siendo una capa encima, y algunas operaciones complejas pueden requerir SQL nativo.  
También hay que tener en cuenta:

-   Las relaciones 1:N o N:M deben definirse explícitamente con `@Relation`.
-   Las transacciones complejas pueden requerir `@Transaction` o consultas personalizadas.
-   Al usar `Flow`, las operaciones de escritura no emiten de inmediato; el trigger depende del commit real en DB.

---

#### 1.7. Cuándo usar Room (y cuándo no)

**Usar Room cuando:**

-   Necesitas **persistencia offline** o sincronización eventual.
-   Quieres cachear datos de red para **mostrar instantáneamente**.
-   Necesitas manejar **listas grandes** o relaciones complejas.
-   Deseas una solución moderna compatible con **MVVM, Clean Architecture y Compose**.

**No usar Room cuando:**

-   Solo guardas configuraciones simples (usa `DataStore` o `SharedPreferences`).
-   No necesitas relaciones ni persistencia duradera (usa una cache en memoria).
-   Trabajas con **bases encriptadas** (usa Room + SQLCipher o librerías especializadas).

---

#### 1.8. Conclusión

Room representa la **maduración del ecosistema Android**:

-   Reemplaza la complejidad de SQLite con un enfoque declarativo y seguro.
-   Reduce el código boilerplate.
-   Se integra de forma natural con corutinas, Flow, Hilt y Compose.
-   Permite un desarrollo **offline-first** más limpio, escalable y mantenible.

En esta clase, implementaremos Room para **persistir un listado de opciones de menú**, observando en tiempo real los cambios y aplicando la estrategia **cache-first + refresh**, demostrando el verdadero poder del flujo **Room + Flow + Retrofit + Hilt**.

### 2) Instalación y dependencias

En `build.gradle` del módulo `app`:

```kotlin
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
}
```

Gradle (KSP):

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
```

---

### 3) Dominio del ejemplo

```json
[
    {
        "id": "home",
        "text": "Inicio",
        "icon": "ic_home",
        "deeplink": "app://home"
    }
]
```

```kotlin
data class MenuOption(
    val id: String,
    val text: String,
    val icon: String,
    val deeplink: String
)
```

---

### 4) Room: Entity, Dao y Database

```kotlin
@Entity(tableName = "menu_option")
data class MenuOptionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val icon: String,
    val deeplink: String,
    val updatedAt: Long
)

@Dao
interface MenuOptionDao {
    @Query("SELECT * FROM menu_option ORDER BY text ASC")
    fun observeAll(): Flow<List<MenuOptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MenuOptionEntity>)

    @Query("DELETE FROM menu_option")
    suspend fun clear()
}

@Database(entities = [MenuOptionEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuOptionDao(): MenuOptionDao
}
```

**Hilt Module:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMenuOptionDao(db: AppDatabase) = db.menuOptionDao()
}
```

---

### 5) Capa de red (Retrofit)

```kotlin
data class MenuOptionDto(
    val id: String,
    val text: String,
    val icon: String,
    val deeplink: String
)

interface MenuApi {
    @GET("menu/options")
    suspend fun getMenuOptions(): List<MenuOptionDto>
}
```

---

### 6) Mapeo DTO ↔ Entity ↔ Domain

```kotlin
fun MenuOptionDto.toEntity(now: Long) = MenuOptionEntity(
    id = id, text = text, icon = icon, deeplink = deeplink, updatedAt = now
)
fun MenuOptionEntity.toDomain() = MenuOption(
    id = id, text = text, icon = icon, deeplink = deeplink
)
```

---

### 7) Repositorio (Flow + cache-first + refresh)

```kotlin
@Singleton
class MenuRepository @Inject constructor(
    private val dao: MenuOptionDao,
    private val api: MenuApi,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    fun observeMenu(): Flow<List<MenuOption>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    suspend fun refresh() = withContext(io) {
        val now = System.currentTimeMillis()
        val remote = api.getMenuOptions()
        dao.upsertAll(remote.map { it.toEntity(now) })
    }
}
```

---

### 8) Caso de uso

```kotlin
class ObserveMenuWithRefreshUseCase @Inject constructor(
    private val repo: MenuRepository
) {
    fun execute(): Flow<List<MenuOption>> = repo.observeMenu()
    suspend fun refresh() = repo.refresh()
}
```

---

### 9) ViewModel con StateFlow

```kotlin
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val useCase: ObserveMenuWithRefreshUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(MenuUiState())
    val state: StateFlow<MenuUiState> = _state

    init {
        viewModelScope.launch {
            useCase.execute().collectLatest {
                _state.value = _state.value.copy(loading = false, items = it)
            }
        }
        viewModelScope.launch { useCase.refresh() }
    }
}

data class MenuUiState(
    val loading: Boolean = true,
    val items: List<MenuOption> = emptyList(),
    val error: String? = null
)
```

---

### 10) UI de ejemplo (Compose)

```kotlin
@Composable
fun MenuScreen(vm: MenuViewModel = hiltViewModel(), onClick: (MenuOption) -> Unit) {
    val state by vm.state.collectAsState()
    LazyColumn {
        items(state.items) { opt ->
            ListItem(
                headlineText = { Text(opt.text) },
                supportingText = { Text(opt.deeplink) },
                modifier = Modifier.clickable { onClick(opt) }
            )
            Divider()
        }
    }
}
```

---

### 11) Migración ejemplo

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE menu_option ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    }
}
```
