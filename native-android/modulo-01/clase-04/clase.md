# Sesión 4 --- Navegación con Jetpack Compose

> Programa: **Especialización en Desarrollo Móvil --- Android/Kotlin**\
> Módulo 01 · Sesión 04

------------------------------------------------------------------------
## Objetivos de aprendizaje
Al finalizar, el estudiante podrá:
- Configurar Navigation Compose en proyectos nuevos y existentes.
- Definir rutas (type-safe o basadas en strings), pasar argumentos y manejar deeplinks.
- Implementar gráficos anidados (auth, onboarding, app) y controlar el back stack (popUpTo, inclusive, restore/save state).
- Integrar ViewModel con navegación basada en eventos (unidireccional), usar SavedStateHandle y devolver resultados entre pantallas.

## Requisitos
- Android Studio actual.
- Kotlin 1.9+ y Compose BOM reciente.
- Conocimientos de estados, efectos (remember, rememberSaveable), ViewModel.

## Introducción conceptual a la navegación (glosario rápido)

- **Grafo de navegación (NavGraph)**: Conjunto de destinos y relaciones. Puede ser **anidado** (ej. `auth`, `app`).  
  *Cuándo lo uso*: Para encapsular sub‑flujos (onboarding, autenticación, tabs).
  
- **Destino (Screen/Destination)**: Composable registrado en el grafo. Es la “pantalla” a la que navegas.  
  *Cuándo lo uso*: Siempre que una UI tenga entrada propia en el grafo.

- **Ruta (Route/Path)**: Cadena que identifica un destino. Puede incluir *placeholders* (`detail/{id}`) y *queries*.  
  *Cuándo lo uso*: Para construir navegación explícita y pasar argumentos.

- **Argumento (NavArgument)**: Parámetro tipado que el destino recibe (`Int`, `Long`, `String`, `Bool`, etc.).  
  *Cuándo lo uso*: Cuando el destino necesita un dato concreto (id, filtro, modo).

- **NavHost**: Composable que *alberga* el grafo y renderiza el destino actual.  
  *Cuándo lo uso*: Una vez por “área” de navegación (raíz, pestañas con grafos propios).

- **NavController**: Orquestador del grafo; expone `navigate`, `popBackStack`, `currentBackStackEntry`.  
  *Cuándo lo uso*: Desde la UI para ejecutar transiciones entre destinos.

- **Back stack**: Pila de entradas de navegación (historial). Define el comportamiento del botón “atrás”.  
  *Cuándo lo uso*: Siempre; lo controlas con opciones de navegación.

- **startDestination**: Primer destino de un grafo.  
  *Cuándo lo uso*: Para definir la pantalla inicial del flujo.

- **Opciones de navegación**: `popUpTo`, `inclusive`, `launchSingleTop`, `saveState`, `restoreState`.  
  *Cuándo lo uso*: Para limpiar el stack, evitar duplicados y preservar/restaurar estado (especialmente en tabs).

- **Grafo anidado (Nested graph)**: Grafo dentro de otro (p. ej., `route = "auth"` con `signin`, `signup`).  
  *Cuándo lo uso*: Para limpiar todo un sub‑flujo con un único `popUpTo("auth"){ inclusive = true }`.

- **Deep link**: URL/Intent que abre un destino interno (ej. `https://site.com/detail/42`).  
  *Cuándo lo uso*: Para integrar notificaciones, enlaces externos o App Links.

- **Top‑level destinos**: Destinos raíz de navegación (pestañas/bottom bar).  
  *Cuándo lo uso*: Para navegación lateral o inferior con preservación de estado entre secciones.

- **SavedStateHandle**: Almacén clave‑valor asociado a una entrada del stack. Soporta argumentos y resultados.  
  *Cuándo lo uso*: Leer argumentos en el `ViewModel` y devolver resultados al destino anterior.

- **Eventos de navegación (one‑shot)**: Señales efímeras desde `ViewModel` (e.g., `SharedFlow`) que la UI traduce a `navigate`.  
  *Cuándo lo uso*: Para desacoplar navegación de la lógica de negocio y evitar repeticiones en recomposición.

- **Back vs Up**: *Back* (historial del sistema) vs *Up* (jerarquía de la app).  
  *Cuándo lo uso*: Configura correctamente el comportamiento del icono “up” en `TopAppBar` según la jerarquía.

- **BackHandler**: API para interceptar el botón “atrás” en Compose.  
  *Cuándo lo uso*: Confirmaciones de salida, bloqueo condicional, formularios con cambios sin guardar.

---

## Configuración de Navigation Compose
- Dependencias (Gradle Kotlin DSL)
``` kotlin
// build.gradle.kts (module)
dependencies {
    // Compose BOM recomendado
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // opcional si usas Hilt
}

```

## 1) Configuración de Navigation Compose

**Concepto**\
Navigation Compose es la librería oficial que permite gestionar
pantallas en Jetpack Compose de forma declarativa. Sustituye el uso de
`FragmentTransaction` y `NavHostFragment` del sistema imperativo. Toda
la navegación se organiza en un **NavHost** que define un grafo de
rutas.

**Caso de uso**\
Una aplicación que necesita moverse entre `Splash → Login → Home` sin
perder el estado y respetando el back stack.

**Ejemplo completo**

``` kotlin
@Composable
fun AppNavRoot() {
    // Controlador central de navegación
    val navController = rememberNavController()

    // Estructura base con Scaffold
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("home") { HomeScreen(navController) }
        }
    }
}
```
- Arranque en SplashScreen
    - La app inicia en la ruta "splash", porque en el NavHost el startDestination es "splash".
    - En SplashScreen hay un delay simulado (como si cargara configuración, token o sesión previa).
    - Una vez terminado, hace:
    ``` kotlin
    navController.navigate("login") {
        popUpTo("splash") { inclusive = true }
    }
    ```
    Esto significa:
    - Se navega a "login"
    - Se elimina "splash" del back stack (no se podrá volver atrás al Splash).
- Pantalla LoginScreen
    - Aquí el usuario ingresa email y contraseña (valores en rememberSaveable para no perderse en recomposición).
    - Si los campos son válidos, ejecuta:
    ``` kotlin
        navController.navigate("home") {
        popUpTo("login") { inclusive = true }
    }
    ```
    Esto significa:
    - Se navega a "home".
    - Se elimina "login" del back stack (no se podrá volver atrás al Login después de iniciar sesión).
    - Si presiona back en Login, como no queda nada más en el back stack, la app se cierra.
- Pantalla HomeScreen
    - Aquí el usuario ve su contenido principal (ejemplo: un contador que conserva su valor con rememberSaveable).
    - En el TopAppBar hay un botón “Logout”.
    - Al presionarlo, ejecuta:
    ``` kotlin
    navController.navigate("login") {
        popUpTo("home") { inclusive = true }
    }
    ```
    Esto significa:
    - Se navega de vuelta al Login.
    - Se elimina "home" del back stack (no se podrá volver atrás a Home una vez deslogueado).

------------------------------------------------------------------------

## 2) Definición de rutas y paso de argumentos

**Concepto**\
Las rutas identifican pantallas. Pueden ser simples (`"home"`) o
contener **placeholders** (`"detail/{id}"`). Navigation Compose permite
pasar **argumentos tipados** (`Int`, `String`, `Bool`) o complejos
(JSON, Parcelize).

**Caso de uso**\
Una app de tienda que desde `ProductList` abre `ProductDetail(id: Int)`.

**Ejemplo completo**

``` kotlin
// Definición de ruta con argumento
object Routes {
    const val ProductList = "product_list"
    const val ProductDetail = "product_detail/{id}"
}

// Navegar enviando un ID
navController.navigate("product_detail/42")

// Recibir argumento en destino
composable(
    route = Routes.ProductDetail,
    arguments = listOf(navArgument("id") { type = NavType.IntType })
) { backStackEntry ->
    val id = backStackEntry.arguments?.getInt("id") ?: 0
    ProductDetailScreen(productId = id)
}
```
- Punto de partida
    - El NavHost arranca en Routes.ProductList y registra dos destinos:
    - product_list (lista)
    - product_detail/{id} (detalle con placeholder {id})
- ProductList muestra datos y decide a dónde ir
    - En ProductListScreen, al tocar un producto se ejecuta:
    ``` kotlin
    navController.navigate(Routes.productDetail(id))
    // Equivale a: navController.navigate("product_detail/42")
    ```
    - Esto “resuelve” la ruta reemplazando {id} por el número real. No hay popUpTo aquí, así que se apila encima de ProductList.
- Resolución de la ruta con placeholder
    - El grafo tiene un destino:
    ``` kotlin
    composable(
        route = "product_detail/{id}",
        arguments = listOf(navArgument("id") { type = NavType.IntType })
    ) { entry -> … }
    ```
    - Navigation Compose reconoce el patrón product_detail/{id}, toma el último segmento de la URL (por ejemplo, 42) y lo intenta parsear como Int porque lo tipaste con NavType.IntType.
- Lectura del argumento en el destino
    - Ya en ProductDetail, recuperas el valor:
    ``` kotlin
    val id = entry.arguments?.getInt("id") ?: 0
    ```
    - Si por algún motivo no llegó (o falló), usas un valor por defecto (0) para no romper la UI.
- Back stack natural y esperable
    - Como no usaste popUpTo, el back stack queda:
    ``` kotlin
    product_list  →  product_detail/42
    ```
    - Al presionar “back” en ProductDetail, se hace popBackStack() implícito y vuelves a ProductList.
    - El estado de ProductList puede recomponerse; si quieres preservar scroll o filtros, usa rememberSaveable (o el estado de la LazyList) en esa pantalla.
- Tipado y validación en tiempo de navegación
    - Si declaras NavType.IntType y navegas con un valor no numérico (p. ej. product_detail/abc), la coincidencia fallará y no se abrirá el destino.
    - Esto evita pantallas con argumentos inválidos, porque el grafo exige el tipo correcto.
- Construcción segura de rutas
    - Definir un helper evita errores de concatenación:
    ``` kotlin
    object Routes {
        const val ProductDetail = "product_detail/{id}"
        fun productDetail(id: Int) = "product_detail/$id"
    }
    ```
    - Así centralizas el formato y reduces bugs.

------------------------------------------------------------------------

## 3) Navegación anidada y control del back stack

**Concepto**\
Un **grafo anidado** agrupa pantallas relacionadas (ej. "auth flow"). El
back stack se controla con opciones como `popUpTo`, `inclusive`,
`launchSingleTop` y `restoreState`.

**Caso de uso**\
Una app con flujo de autenticación (`SignIn`, `SignUp`) que, al
completarse, debe limpiar el back stack y saltar a `Home`.

**Ejemplo completo**

``` kotlin
NavHost(navController, startDestination = "splash") {
    navigation(startDestination = "signin", route = "auth") {
        composable("signin") {
            SignInScreen(
                onSuccess = {
                    navController.navigate("home") {
                        // Limpiar grafo auth del back stack
                        popUpTo("auth") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("signup") { SignUpScreen() }
    }
    composable("home") { HomeScreen() }
}
```
- Punto de partida: splash
    - El NavHost inicia en "splash".
    - Al terminar el arranque, según haya sesión o no:
        - Con sesión: navega a "home" y hace popUpTo("splash") { inclusive = true } para eliminar Splash del back stack.
        - Sin sesión: navega a "auth" y hace popUpTo("splash") { inclusive = true } para eliminar Splash y entrar al grafo anidado de autenticación.
- Grafo anidado auth
    - navigation(startDestination = "signin", route = "auth") agrupa pantallas de autenticación.
    - Dentro hay dos destinos:
        - "signin" (inicio del flujo)
        - "signup" (se llega desde SignIn con un navigate("signup"), lo que apila SignUp sobre SignIn).
- Del auth a home (login o registro exitoso)
    - Desde "signin" o "signup", al autenticar, se navega a "home" con:
    ``` kotlin
    navController.navigate("home") {
        popUpTo("auth") { inclusive = true }
        launchSingleTop = true
    }
    ```
    - Esto hace dos cosas clave:
    - Limpia por completo el grafo auth del back stack (incluye signin y signup, y también la propia entrada auth por inclusive = true).
    - Evita duplicar home si ya estuviera arriba (launchSingleTop = true).
- Estado del back stack en cada paso (vista rápida)
    - Inicio:
    ``` kotlin
    [splash]
    ```
    - Sin sesión → entrar a auth:
    ``` kotlin
    [auth, signin]
    ```
    - Desde SignIn → SignUp:
    ``` kotlin
    [auth, signin, signup]
    ```
    - Autenticación exitosa (desde cualquiera de las dos): ((El auth y todo su contenido se eliminaron).)
    ``` kotlin
    [home]
    ```
    - Con sesión desde Splash → Home directo:
    ``` kotlin
    [home]
    ```
- Comportamiento del botón “Atrás”
    - En signup, “atrás” vuelve a signin (se hace popBackStack() de manera natural).
    - En home, “atrás” cierra la app (no hay pantallas previas, porque auth se eliminó).
    - Este comportamiento garantiza que, tras autenticarse, el usuario no pueda regresar a SignIn/SignUp con “atrás”.
- Logout desde home
    - Si desde home ejecutas logout y navegas a "auth" con:
    ``` kotlin
    navController.navigate("auth") {
        popUpTo("home") { inclusive = true }
        launchSingleTop = true
    }
    ```
    - limpias home del stack y vuelves a auth (que abre en signin). El back stack queda:
    ``` kotlin
    [auth, signin]
    ```
- ¿Para qué sirven exactamente las banderas?
    - popUpTo("X"): elimina del back stack todo lo que esté por encima de X.
    - inclusive = true: además elimina X.
    - launchSingleTop = true: si estás navegando al mismo destino que ya está arriba, no crees otra copia.
    - restoreState = true: útil cuando alternas entre destinos de nivel superior (por ejemplo, tabs) y quieres recuperar el estado guardado de un destino previamente visitado. En este flujo de auth → home no es imprescindible, pero sería valioso en una bottom bar.
- Beneficio del grafo anidado
    - Encapsula el “sub-flujo” de autenticación.
    - Permite limpiar de una sola vez todo el bloque auth con popUpTo("auth") { inclusive = true }, evitando que queden pantallas de login/registro en la pila tras el éxito.


------------------------------------------------------------------------

## 4) Integración con ViewModel y eventos de navegación

**Concepto**\
El ViewModel no debe tener referencia directa al `NavController`. En su
lugar, emite **eventos de navegación** que la UI recoge y traduce a
`navController.navigate(...)`.

**Caso de uso**\
Pantalla de `Login` donde, tras un login exitoso, se navega a `Home`. El
ViewModel solo emite el evento.

**Ejemplo completo**

``` kotlin
sealed class NavEvent {
    object GoHome : NavEvent()
    object GoRegister : NavEvent()
}

class LoginViewModel : ViewModel() {
    private val _nav = MutableSharedFlow<NavEvent>()
    val nav: SharedFlow<NavEvent> = _nav

    fun onLogin(email: String, pass: String) {
        viewModelScope.launch {
            // Simulación de login
            if (email == "a@b.com" && pass == "1234") {
                _nav.emit(NavEvent.GoHome)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, vm: LoginViewModel = viewModel()) {
    val navEvent = vm.nav.collectAsState(initial = null)

    LaunchedEffect(navEvent.value) {
        when (navEvent.value) {
            NavEvent.GoHome -> navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            NavEvent.GoRegister -> navController.navigate("register")
            else -> {}
        }
    }

    // UI simplificada
    Button(onClick = { vm.onLogin("a@b.com", "1234") }) { Text("Login") }
}
```
- Punto de partida (NavHost)
    - El NavHost define las rutas "login", "register" y "home".
    - En el destino "login", NO pasamos NavController al ViewModel; en su lugar, el Composable LoginScreen recibe callbacks (onNavigateHome, onNavigateRegister) que llaman a navController.navigate(...).
- Responsabilidad del ViewModel
    - LoginViewModel solo conoce la lógica de negocio (validar credenciales) y emite eventos efímeros (NavEvent) cuando corresponde:
        - GoHome si el login es exitoso.
        - GoRegister si el usuario elige registrarse.
    - El VM usa un Channel (consumo one-shot, sin replay) y lo expone como Flow vía receiveAsFlow().
- Recolección de eventos en la UI
    - En LoginScreen, un LaunchedEffect(Unit) colecta vm.navEvents.collectLatest { event -> ... }.
    - Cuando llega GoHome, la UI ejecuta onNavigateHome(); cuando llega GoRegister, llama onNavigateRegister().
- Traducción de eventos a navegación
    - onNavigateHome() hace:
    ``` kotlin
    navController.navigate("home") {
        popUpTo("login") { inclusive = true }  // limpia Login del back stack
        launchSingleTop = true                 // evita duplicar Home
    }
    ```
    - Resultado: la pila queda solo con home. Al presionar “atrás”, la app se cierra.
    - onNavigateRegister() hace:
    ``` kotlin
    navController.navigate("register")
    ```
    - Resultado: register se apila sobre login. “Atrás” vuelve a login.
- Por qué el VM no conoce el NavController
    - Evita acoplar la capa de dominio (VM) con la capa de presentación (navegación/UI).
    - Permite testear el VM sin framework de navegación: solo verificas que emite el NavEvent correcto.
- Por qué usar eventos “one-shot”
    - La navegación es una acción transitoria (no un estado permanente).
    - Channel (o SharedFlow con replay = 0) evita que la UI repita la navegación al recomponerse o al rotar la pantalla.
- Ciclo de vida y recomposición
    - La colección en LaunchedEffect(Unit) se inicia una vez por composición y se cancela cuando LoginScreen sale del árbol; no quedan colecciones “zombies”.
    - El estado de campos (email, pass) es local de la UI (rememberSaveable), así que sobrevive a recomposiciones/cambios de configuración, pero se descarta cuando limpias login del stack (lo esperado tras navegar a Home).
- Resumen del back stack
    - login → register: se apila register encima; “atrás” vuelve a login.
    - login → home (éxito): popUpTo("login", inclusive = true) limpia login; “atrás” desde home cierra la app.

------------------------------------------------------------------------

## 5) Paso de argumentos y resultados

**Concepto**\
Compose permite enviar datos de una pantalla a otra y devolver
resultados usando `SavedStateHandle` del back stack.

**Caso de uso**\
Abrir `DetailScreen` desde `Home` y devolver un resultado ("Favorito
agregado").

**Ejemplo completo**

``` kotlin
// En DetailScreen
Button(onClick = {
    navController.previousBackStackEntry
        ?.savedStateHandle
        ?.set("result_key", "Favorito agregado")
    navController.popBackStack()
}) { Text("Guardar y volver") }

// En HomeScreen
val result = navController.currentBackStackEntry
    ?.savedStateHandle
    ?.getStateFlow("result_key", "")

val resultValue by result?.collectAsState()
LaunchedEffect(resultValue) {
    if (!resultValue.isNullOrEmpty()) {
        Log.d("Home", "Resultado: $resultValue")
    }
}
```
- Inicio en Home
    - El NavHost arranca en home.
    - Home renderiza una lista de ítems. Al tocar uno, navega a detail/{id} construyendo la ruta con el ID real: navController.navigate("detail/12")
    - No se usa popUpTo aquí: Detail se apila encima de Home.
- Entrada a Detail (recibir el argumento)
    - El destino detail/{id} declara el argumento tipado: arguments = listOf(navArgument("id") { type = NavType.IntType }).
    - Navigation Compose hace “match” con la ruta y parsea el id como Int.
    - En el cuerpo del destino, se lee con: val id = backStackEntry.arguments?.getInt("id") ?: 0.
- Devolver el resultado a Home
    - Antes de volver, Detail escribe el resultado en el SavedStateHandle de la entrada previa del back stack (que es Home):
    ``` kotlin
    navController.previousBackStackEntry
    ?.savedStateHandle
    ?.set("detail_result", "Favorite for #$id added")
    ```
    - Luego llama navController.popBackStack() para regresar a Home.
    - Importante: primero se setea el resultado y después se hace popBackStack(). Así te aseguras de que Home pueda leerlo.
- Consumo del resultado en Home
    - En el bloque composable(Routes.Home) { backStackEntry -> … }, se toma el savedStateHandle del entry actual (Home).
    - Se observa el resultado como StateFlow con un valor por defecto vacío: val resultFlow = savedStateHandle.getStateFlow("detail_result", "").
    - Se colecta en estado Compose: val resultValue by resultFlow.collectAsState().
    -  Con un LaunchedEffect(resultValue), cuando llega un valor no vacío:
        - Se muestra (p. ej., un Snackbar con “Result: Favorite for #12 added”).
        - Se limpia la clave para no procesarlo de nuevo tras recomposiciones/rotaciones: savedStateHandle["detail_result"] = ""
- Qué garantiza la robustez del patrón
    - El dato “viaja” por el back stack: Detail escribe en el SavedStateHandle de Home, que es la previousBackStackEntry en el momento del pop.
    - Al observarlo como StateFlow, Home reacciona una sola vez y tú controlas la re-consumición limpiando el valor.
    - El resultado sobrevive a recomposiciones y cambios de configuración mientras la entrada de Home exista en el stack.
- Comportamiento del back stack
    - Tras abrir detail/12: home → detail/12.
    - Al guardar y volver: popBackStack() regresa a home.
    - “Atrás” en Home ahora sale de la app (si no hay más pantallas debajo), como de costumbre.
- Variaciones y buenas prácticas
    - Usa claves únicas por tipo de resultado ("detail_result", "edit_result", etc.) para evitar colisiones.
    - Puedes devolver tipos más ricos: Int, Boolean o Parcelable (con @Parcelize).
    - Si limpias Home del stack con popUpTo(inclusive = true) antes de que lea el resultado, ya no habrá previousBackStackEntry válido; este patrón está pensado para push → pop clásico.
    - Si necesitas resultados entre grafos o más de un salto, considera un repositorio compartido o un ViewModel de nivel de grafo.

------------------------------------------------------------------------

## 6) Control del back stack

**Concepto**\
El back stack es la pila de pantallas activas. Podemos controlarlo para
evitar duplicados o limpiar rutas innecesarias.

**Caso de uso**\
En una app con bottom navigation (`Feed`, `Search`, `Profile`), queremos
que al tocar un tab no se creen múltiples instancias de la misma
pantalla.

**Ejemplo completo**

``` kotlin
navController.navigate("feed") {
    popUpTo("root") { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```
- Cada tab es un grafo anidado (FeedGraph, SearchGraph, ProfileGraph) con su propia ruta de inicio (Feed, Search, Profile) y, opcionalmente, subpantallas (ej. FeedDetail).
- Al tocar un tab, navegamos a su graphRoute con:
    ``` kotlin
    navController.navigate(tab.graphRoute) {
        popUpTo(Routes.Root) { saveState = true }  // guarda el estado del grafo actual
        launchSingleTop = true                     // no duplica si ya está arriba
        restoreState = true                        // restaura el estado del grafo destino
    }
    ```
- Resultado:
    - Si estabas en FeedDetail y cambias a Search, luego vuelves a Feed: regresas exactamente a FeedDetail (stack del tab restaurado).
    - No se crean múltiples instancias de Feed al re-tocar su tab.
    - “Atrás” dentro de un tab hace pop dentro de ese grafo; “atrás” desde la pantalla raíz de un tab sale de la app (si no hay más debajo).

------------------------------------------------------------------------

## 7) Deeplinks

**Concepto**\
Un deeplink abre directamente una pantalla interna de la app a partir de
una URL externa o notificación.

**Caso de uso**\
Abrir `DetailScreen` con la URL `https://misitio.com/detail/42`.

**Ejemplo completo**

``` kotlin
composable(
    route = "detail/{id}",
    arguments = listOf(navArgument("id") { type = NavType.LongType }),
    deepLinks = listOf(navDeepLink { uriPattern = "https://misitio.com/detail/{id}" })
) { entry ->
    val id = entry.arguments?.getLong("id") ?: 0L
    DetailScreen(itemId = id)
}
```
- Entrada del deeplink (sistema → tu Activity)
    - El usuario toca una URL externa (https://misitio.com/detail/42) o una notificación con ese enlace.
    - Android envía un Intent.ACTION_VIEW a tu MainActivity porque el AndroidManifest.xml tiene un intent-filter que coincide (scheme https, host misitio.com).
    - Tu app se abre y entrega ese intent al NavHost de Navigation Compose.
- Resolución en Navigation Compose
    - En el NavHost, el destino composable("detail/{id}", deepLinks = [navDeepLink { uriPattern = "https://misitio.com/detail/{id}" }]) declara el patrón del deeplink y el placeholder {id}.
    - Navigation hace “match” entre la URL y el patrón, extrae id y lo convierte al tipo declarado (por ejemplo NavType.LongType).
    - El backStackEntry.arguments ya contiene el id; se invoca DetailScreen(itemId = id).
- Back stack y comportamiento del botón “atrás”
    - App cerrada (cold start): el back stack se crea con el destino de deeplink como tope (y los grafos padre). Al presionar “atrás”, normalmente sales de la app (no vuelves a Home salvo que tú lo navegues explícitamente).
    - App abierta (warm): el deeplink navega dentro del stack actual. “Atrás” te regresa a la pantalla donde estabas antes de abrir el deeplink.
- Navegación interna vs externa
    - Navegación interna: navController.navigate("detail/42") (o Routes.detail(42)), controlas flags como launchSingleTop, restoreState, popUpTo.
    - Deeplink externo: lo resuelve Navigation automáticamente según deepLinks. Si te preocupa duplicar destinos cuando ya estás en esa pantalla, puedes, desde tu manejo interno, navegar a la Uri con launchSingleTop = true.
- Argumentos válidos / inválidos
    - Si id no es convertible al tipo declarado, el destino no matchea y no se abre.
    - Si declaras un valor por defecto al leer (?: 0L), tu UI puede manejar gracefully el caso límite.
- Compatibilidad con autenticación
    - Si Detail requiere sesión, al recibir el deeplink puedes redirigir a Login y guardar la Uri (en un ViewModel/SavedStateHandle) para navegar a Detail tras loguear. Así no pierdes el destino.
- App Links verificados (opcional)
    - Para que https://misitio.com/detail/42 abra tu app sin “chooser”, configura assetlinks.json en tu dominio (App Links). Si no lo haces, seguirá funcionando, pero Android puede mostrar el selector navegador ↔ app.
- Pruebas:
    - ADB: adb shell am start -a android.intent.action.VIEW -d "https://misitio.com/detail/42" com.example.deeplinks
    - Notificación: crea un PendingIntent con Intent(ACTION_VIEW, Uri.parse(...)).

