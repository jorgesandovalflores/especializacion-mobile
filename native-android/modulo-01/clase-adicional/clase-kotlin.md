# Kotlin para Android — Clase completa

> Programa: **Especialización en Desarrollo Móvil — Android/Kotlin**  
> Sesión: **Kotlin desde la base con comparaciones a Java y TypeScript**  

---

## Objetivo general
Comprender los fundamentos del lenguaje **Kotlin** (tipos, operadores, control de flujo, funciones, POO, colecciones, null-safety, coroutines e interoperabilidad con Java), con comparaciones a **Java** y **TypeScript**, y criterios claros de **cuándo y por qué** usar cada característica en Android.

## Requisitos previos
- Android Studio instalado (versión reciente).  
- Conocimientos básicos de Java **o** TypeScript/JavaScript.  
- Git, Gradle y SDK de Android configurados.

---

## Historia y contexto

**Teoría**  
### Nacimiento en JetBrains (2010 – 2011)
- Kotlin fue concebido en **2010** dentro de JetBrains (la compañía detrás de IntelliJ IDEA).
- El nombre viene de la isla **Kotlin**, cerca de San Petersburgo (Rusia), siguiendo la tradición de nombres tipo “Java”.
- En **julio de 2011**, JetBrains anunció públicamente el lenguaje.

### Primeros pasos (2012 – 2015)
- En **2012**, Kotlin se liberó como **open source** bajo licencia Apache 2.
- La idea era crear un lenguaje moderno, conciso, seguro contra `NullPointerException` y totalmente interoperable con Java.
- Durante estos años se publicaron versiones preliminares con fuerte retroalimentación de la comunidad.

### Versión 1.0 (2016)
- En **febrero de 2016**, JetBrains lanzó **Kotlin 1.0**, garantizando compatibilidad hacia adelante.
- Esto fue clave: cualquier código Kotlin 1.0 seguiría funcionando en el futuro.

### Impulso de Google (2017 – 2019)
- En **mayo de 2017**, Google anunció soporte oficial de Kotlin para Android en Google I/O.
- En **2019**, Google lo declaró **“el lenguaje preferido para Android”**, priorizando documentación, APIs y ejemplos en Kotlin sobre Java.

### Expansión (2020 – 2024)
- Kotlin se consolidó no solo en Android, sino también en backend (Ktor, Spring Boot), escritorio y web (Kotlin/JS).
- **Kotlin Multiplatform** comenzó a tomar fuerza para compartir lógica entre Android, iOS, web y escritorio.
- Versiones como Kotlin 1.5 y 1.6 mejoraron **coroutines, rendimiento y compilador**.
- Integración más estrecha con **Gradle, Spring, Jetpack Compose**.

### K2 y el futuro (2023 – presente)
- JetBrains trabaja en **K2**, una reescritura del compilador para hacerlo más rápido, escalable y flexible.
- La visión es que Kotlin sea un lenguaje **multiplataforma de propósito general**, no solo ligado a Android.

### Línea de tiempo rápida
- **2010** → Inicio en JetBrains  
- **2011** → Anuncio público  
- **2012** → Open Source (Apache 2)  
- **2016** → Kotlin 1.0 (estable)  
- **2017** → Google anuncia soporte Android  
- **2019** → Lenguaje preferido en Android  
- **2020 – 2024** → Expansión multiplataforma  
- **2023+** → Proyecto K2, futuro del compilador  

---

## 1) Tipos de datos e inferencia

**Concepto/Teoría**  
- En Kotlin **todo es objeto** (no hay primitivos “puros” como en Java). Tipos: `Int`, `Long`, `Float`, `Double`, `Boolean`, `Char`, `String`.  
- **Inferencia de tipos**: el compilador infiere el tipo a partir del valor.  
- Tipos especiales:  
  - `Any` (supertipo de todos), `Unit` (similar a `void`), `Nothing` (nunca retorna).  
  - **Tipos anulables** con `?` (profundizamos en Null Safety).  
  - **Value classes** para envolver un valor con *cero overhead*.  
  - `typealias` para alias legibles de tipos complejos.

**Sintaxis (Kotlin)**
```kotlin
val age = 28                      // inferido como Int
val price: Double = 10.5          // anotación explícita
val nickname: String? = null      // tipo anulable

typealias UserId = Long           // alias de tipo

@JvmInline
value class Email(val value: String) // clase de valor (optimización)
```

**Comparación**  
- **Java**: primitivo vs wrapper (`int` vs `Integer`), `Object`, `void`; no hay anulables por tipo.  
- **TypeScript**: `number`, `string`, `boolean`, `unknown/any`, `void`, `never`; anulabilidad por unión (`string | null`).

**Cuándo y por qué**  
- Usa **inferencia** para legibilidad; **tipa explícito** en API públicas o cuando no sea obvio.  
- **Value classes** para IDs/VOs con validación y sin coste extra.  
- `Any/Unit/Nothing` comunican intención: “retorno sin valor útil”, “nunca retorna”, etc.

---

## 2) Variables, constantes y alcance (scope)

**Concepto/Teoría**  
- `val` (inmutable) vs `var` (mutable).  
- **Top-level**: funciones/propiedades a nivel de archivo (sin clases contenedoras).  
- Visibilidad: `public` (por defecto), `internal` (módulo), `private` (archivo/miembro), `protected` (herencia).

**Sintaxis**
```kotlin
val maxRetries = 3                       // inmutable
var attempts = 0                         // mutable
private val cache = mutableMapOf<String, String>() // privado a archivo
```

**Comparación**  
- **Java**: todo dentro de clases; `final` para inmutabilidad.  
- **TypeScript**: `const/let`; módulos/archivos top-level comunes.

**Cuándo y por qué**  
- Prefiere **`val` por defecto** (seguridad).  
- `internal` para ocultar detalles en el módulo.  
- **Top-level** para utilidades sin clases artificiales.

---

## 3) Operadores y expresiones

**Concepto/Teoría**  
- Aritméticos/lógicos/comparación similares a Java.  
- Kotlin promueve **expresiones** (casi todo retorna).  
- Operadores clave: templates de string, `?:` (Elvis), `?.` (safe call), `!!` (assert), `in` (pertenencia).

**Sintaxis**
```kotlin
val user = "Ana"
val msg = "Hello, $user"            // template de string
val fallback = user ?: "Guest"      // Elvis (valor por defecto)

val list = listOf(1, 2, 3)
val hasTwo = 2 in list              // pertenencia
```

**Comparación**  
- **Java**: concatenación con `+`; no hay Elvis/safe call.  
- **TypeScript**: templates con backticks; `??` (nullish) y `?.` (optional chaining) muy similar.

**Cuándo y por qué**  
- Templates → legibilidad.  
- `?:`/`?.` → evita `if` anidados.  
- Evita `!!` salvo certeza absoluta (tests/adaptadores temporales).

---

## 4) Control de flujo (if/when, bucles, rangos, smart casts)

**Concepto/Teoría**  
- `if` y `when` **son expresiones** (devuelven valor).  
- Rangos: `1..10`, `downTo`, `step`.  
- **Smart casts**: el compilador “entiende” el tipo tras un `is`/null-check.

**Sintaxis**
```kotlin
val label = if (age >= 18) "Adult" else "Minor" // if expresión

fun asText(x: Any): String =
    when (x) {
        is Int -> "Int: $x"        // smart cast a Int
        is String -> "Str: $x"     // smart cast a String
        else -> "Unknown"
    }

for (i in 10 downTo 1 step 2) { /* 10,8,6,4,2 */ }
```

**Comparación**  
- **Java**: `switch` no es expresión (hasta Java 14+ mejorado); sin smart casts automáticos.  
- **TypeScript**: `switch` clásico; sin rangos integrados (se usa índice o utilidades).

**Cuándo y por qué**  
- `when` → reemplaza múltiples `if/else` y es ideal con **sealed classes**.  
- Rangos → bucles claros sin errores off-by-one.  
- Smart casts → menos casting manual.

---

## 5) Funciones (default/named, extensiones, infix)

**Concepto/Teoría**  
- Parámetros **por defecto** y **argumentos nombrados** (menos overloads).  
- **Funciones de extensión**: agregan comportamiento a tipos existentes sin heredar.  
- **Infix**: llamadas estilo operador para funciones de un parámetro.  
- `tailrec` (opt. recursiva), `inline` (opt. orden superior) cuando corresponda.

**Sintaxis**
```kotlin
fun greet(name: String = "Guest") = "Hello, $name" // por defecto + expresión

fun String.words(): List<String> = this.split(" ") // extensión a String

infix fun Int.times(other: Int): Int = this * other // infix
val z = 3 times 4
```

**Comparación**  
- **Java**: sin named/default; no hay extensiones (se usan utilidades estáticas).  
- **TypeScript**: default params sí; no hay extensiones (prototipos no recomendados).

**Cuándo y por qué**  
- **Named/default** para APIs legibles (builders/constructores complejos).  
- **Extensiones** para utilidades de dominio y legibilidad.  
- `infix` para operaciones binarias semánticas (e.g., `to` en `Pair`).

---

## 6) POO en Kotlin

**Concepto/Teoría**  
- **Clases** con constructor primario/secundario; **propiedades** con getters/setters sintéticos.  
- **Data classes**: `equals/hashCode/toString/copy/componentN`.  
- **Enum** y **Sealed classes** (jerarquías cerradas, exhaustividad en `when`).  
- **Interfaces** con métodos por defecto; **clases abstractas**.  
- **Objects** (singletons) y **companion objects** (miembros “estáticos”).  
- **Delegación** con `by` para composición sobre herencia.

**Sintaxis**
```kotlin
class Person(val id: Long, var name: String) // propiedades en el constructor

data class User(val id: Long, val email: String) // data class para DTO/Entidad

enum class Status { ACTIVE, INACTIVE }

sealed class Result {
    data class Ok(val data: String): Result()
    data class Err(val cause: Throwable): Result()
}

object AppConfig { val version = "1.0.0" } // singleton

interface Logger { fun log(msg: String) }
class ConsoleLogger : Logger { override fun log(msg: String) = println(msg) }

class Service(private val logger: Logger) : Logger by logger // delegación
```

**Comparación**  
- **Java**: data class requiere boilerplate; sealed en Java 17+ pero más verboso; sin delegación `by`.  
- **TypeScript**: similar en clases/interfaces; sin `sealed` (se usa unión de tipos) ni `data class` nativa.

**Cuándo y por qué**  
- **Data class** para DTOs/entidades (Room/Retrofit).  
- **Sealed + when** para estados de UI (loading/success/error).  
- **Delegación** para reutilizar comportamiento sin herencia múltiple.

---

## 7) Colecciones y orden superior

**Concepto/Teoría**  
- **Inmutables** por defecto: `listOf/setOf/mapOf`; **mutables** con `mutableListOf`, etc.  
- Funciones de orden superior: `map`, `filter`, `flatMap`, `fold`, `groupBy`, `associateBy`, `mapNotNull`.  
- **Sequence**: procesamiento *lazy* para pipelines largas o colecciones grandes.

**Sintaxis**
```kotlin
data class Book(val id: Int, val title: String, val pages: Int)

val books = listOf(
    Book(1, "Kotlin in Action", 350),
    Book(2, "Effective Java", 420),
    Book(3, "Clean Code", 300)
)

val longTitles = books
    .filter { it.pages >= 350 }   // filtra
    .map { it.title }             // transforma

val byId: Map<Int, Book> = books.associateBy { it.id } // índice por id

val lazyCount = books.asSequence()
    .filter { it.pages > 300 }
    .map { it.title.length }
    .count()
```

**Comparación**  
- **Java**: Streams (`stream().map().filter()...`), más verboso y tipos ruidosos.  
- **TypeScript**: `Array.map/filter/reduce` similar; no hay `Sequence` nativa.

**Cuándo y por qué**  
- **Inmutables** para seguridad; mutables solo donde sea necesario.  
- **Sequence** cuando la colección es grande o la pipeline es costosa (evita listas intermedias).  
- `associateBy/groupBy` para reindexar datos en memoria (cache/viewmodels).

---

## 8) Null Safety

**Concepto/Teoría**  
- Tipos anulables con `?`; operadores `?.`, `?:`, `!!`; patrón `let` y null-checks.  
- `lateinit var` para inyección/configuración diferida (no para tipos primitivos).  
- Interop con Java: anotaciones de nullabilidad importan.

**Sintaxis**
```kotlin
var phone: String? = null
val lengthOrZero = phone?.length ?: 0 // seguro

phone?.let { p ->
    println("Phone length: ${p.length}") // se ejecuta solo si no es nulo
}

// '!!' lanza NPE si es nulo (evitar en producción)
val mustHaveValue = phone!!
```

**Comparación**  
- **Java**: `null` en cualquier referencia; NPE común.  
- **TypeScript**: `?.` y `??` similares; anulabilidad con uniones de tipos.

**Cuándo y por qué**  
- Prefiere `?.` y `?:` para ramificaciones simples.  
- `let` cuando quieras **limitar el alcance** con no-nulos.  
- Evita `!!` salvo pruebas o garantías estrictas.

---

## 9) Coroutines

**Concepto/Teoría**  
- Concurrencia ligera con **structured concurrency** y cancelación propagada.  
- `suspend` suspende sin bloquear.  
- `launch` (fire-and-forget) y `async` (retorna `Deferred`).  
- **Dispatchers**: `Main` (UI), `IO` (bloqueo IO), `Default` (CPU).  
- En Android: `lifecycleScope` y `viewModelScope` para integrarse con el ciclo de vida.

**Sintaxis**
```kotlin
suspend fun loadScreen(): Pair<User, List<Post>> = coroutineScope {
    val userDef = async { api.getUser() }      // paralelo
    val postsDef = async { api.getPosts() }    // paralelo
    userDef.await() to postsDef.await()
}

suspend fun readFile(path: String): String =
    withContext(Dispatchers.IO) { file(path).readText() } // cambiar dispatcher
```

**Comparación**  
- **Java**: Threads/Futures/CompletableFuture; más pesado y verboso.  
- **TypeScript**: `async/await` sobre Promises; parecido en concepto.

**Cuándo y por qué**  
- Redes/IO sin bloquear UI en Android.  
- `async` solo para **paralelo real**; si no, `launch`.  
- `withContext(IO)` para disco/red.

---

## 10) Interoperabilidad con Java

**Concepto/Teoría + Sintaxis**
```kotlin
class Foo @JvmOverloads constructor(val x: Int = 0) {
    companion object {
        @JvmStatic fun hello() = "hi" // acceso estático desde Java
    }
}

// SAM adapters: pasar lambda donde Java espera interfaz funcional
val thread = Thread { println("Hello from Kotlin!") }
thread.start()
```
- Anotaciones: `@JvmOverloads`, `@JvmStatic`, `@JvmName`.  
- Respeta `@Nullable/@NotNull` para tipos desde Kotlin.

**Cuándo y por qué**  
- Cuando expongas APIs Kotlin a consumidores Java (SDKs, módulos legacy).

---

## 11) Beneficios clave en Android

- **KTX**: extensiones idiomáticas (menos boilerplate).  
- **Compose**: UI declarativa, Kotlin-first.  
- **Coroutines + Flow**: asincronía y reactividad segura con el ciclo de vida.  
- **Null Safety**: menos crashes (NPE).  
- **Data/Sealed + when**: modelado claro de estados de UI.

---

## Apéndice A — Ejemplo integrado en Android (ViewModel + Compose)

```kotlin
class MainViewModel(
    private val repo: Repository
) : ViewModel() {

    // Estado modelado con sealed
    sealed class UiState {
        data object Loading : UiState()
        data class Ready(val data: List<Item>) : UiState()
        data class Error(val cause: Throwable) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch {
            runCatching { repo.fetchItems() }
                .onSuccess { _state.value = UiState.Ready(it) }
                .onFailure { _state.value = UiState.Error(it) }
        }
    }
}
```

```kotlin
@Composable
fun Screen(state: MainViewModel.UiState) {
    when (state) {
        is MainViewModel.UiState.Loading -> Text("Loading...")
        is MainViewModel.UiState.Ready -> Text("Items: ${state.data.size}")
        is MainViewModel.UiState.Error -> Text("Error: ${state.cause.message}")
    }
}
```

---

## Apéndice B — Cheat-sheet “cuándo y por qué”

- **`val` > `var`**: inmutabilidad por defecto.  
- **Named/default args**: reemplaza sobrecargas verbosas.  
- **Extensiones**: legibilidad y cohesión sin herencia.  
- **Data + Sealed + when**: estados y DTOs sólidos.  
- **Colecciones inmutables** + `Sequence` en pipelines grandes.  
- **`?.` y `?:`** siempre; **evita `!!`**.  
- **Coroutines**: `withContext(IO)` para trabajo pesado; `async` solo si hay paralelo real.  
- **Interop Java**: `@JvmOverloads/@JvmStatic` al exponer APIs.

---

## Actividades sugeridas
1. Reescribe un POJO Java de `User` como **data class** Kotlin con `copy`.  
2. Convierte un `switch` Java a un `when` Kotlin con exhaustividad.  
3. Implementa una función `suspend` que llame dos endpoints en paralelo con `async` y combine resultados.

## Lecturas recomendadas
- *Kotlinlang.org* — Secciones: *Basics*, *Classes and Objects*, *Coroutines*.  
- *Android Developers* — *Kotlin in Android*, *KTX*, *Coroutines & Flow*, *Compose*.
