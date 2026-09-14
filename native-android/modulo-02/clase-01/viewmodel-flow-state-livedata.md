# ViewModel, State, Flow y LiveData en Android

> 📍 Documento 3 de la clase — ver el [menú completo en README.md](./README.md#menú-de-la-clase). Requiere haber visto [comparacion-arquitecturas.md](./comparacion-arquitecturas.md).

Los cuatro conceptos de este documento conviven en un mismo archivo real del proyecto `Example/`: [`ProductListViewModel.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListViewModel.kt) y su pantalla, [`ProductListScreenMVVM.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListScreenMVVM.kt), que consumen `GET https://fakestoreapi.com/products`. Cada sección de abajo señala exactamente qué línea de esos dos archivos lo implementa.

## ViewModel
- Clase de **Android Jetpack** para separar la lógica de negocio de la UI.
- Pertenece al paquete `androidx.lifecycle`.
- Sobrevive a cambios de configuración (rotación de pantalla), pero no a la muerte de proceso salvo que use `SavedStateHandle` (ver más abajo).
- Se utiliza en el patrón **MVVM** para exponer datos a la interfaz.

**Ejemplo mínimo:**
```kotlin
class ProductListViewModel : ViewModel() {
    val products = MutableLiveData<List<Product>>()
}
```

**Ejemplo real** (`ProductListViewModel.kt`): recibe sus dependencias por constructor con valores por defecto — un `ProductRepository` (el endpoint real, decorado con logging) y un `SavedStateHandle` — y se crea con una `Factory` sin reflexión:
```kotlin
class ProductListViewModel(
    private val repository: ProductRepository = LoggingProductRepository(ProductRemoteRepository()),
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() { /* ... */ }
```

---

## State
- Concepto de **Jetpack Compose**.
- Representa un valor observable que, al cambiar, dispara la **recomposición**.
- Se crea con `remember { mutableStateOf(...) }`.
- Ideal para estados **locales y efímeros** de UI: no vive en el ViewModel porque no es una regla de negocio.

**Ejemplo mínimo:**
```kotlin
var counter by remember { mutableStateOf(0) }
Button(onClick = { counter++ }) {
    Text("Clicks: $counter")
}
```

**Ejemplo real** (`ProductListScreenMVVM.kt`): el filtro "Solo en stock" es un `State` local a la pantalla, que filtra la lista ya cargada por el ViewModel sin pedirle nada nuevo:
```kotlin
var onlyInStock by remember { mutableStateOf(false) }
val visibleProducts = remember(ui.data, onlyInStock) {
    if (onlyInStock) ui.data.filter { it.inStock } else ui.data
}
```

---

## Flow
- Parte de **Kotlin Coroutines**.
- Representa un flujo **asíncrono de datos** (stream).
- Puede emitir múltiples valores en el tiempo; su variante `StateFlow` siempre tiene un valor actual.
- Se usa en repositorios y ViewModels; en Compose se recolecta con `collectAsStateWithLifecycle()` (no `collectAsState()`, que sigue recolectando en segundo plano).

**Ejemplo mínimo:**
```kotlin
val products: Flow<List<Product>> = repository.getProducts()
```

**Ejemplo real** (`ProductListViewModel.kt` + `ProductListScreenMVVM.kt`): el resultado de la llamada de red se expone como `StateFlow<ProductListUiState>` y se recolecta de forma lifecycle-aware:
```kotlin
// ViewModel
private val _ui = MutableStateFlow(ProductListUiState(loading = true))
val ui: StateFlow<ProductListUiState> = _ui

// Compose
val ui by vm.ui.collectAsStateWithLifecycle()
```

---

## LiveData
- Clase de **Jetpack Lifecycle**.
- Contenedor observable **ligado al ciclo de vida** de Activities/Fragments.
- Evita fugas de memoria.
- Hoy en día suele reemplazarse por **StateFlow** en proyectos nuevos con Compose, pero sigue siendo muy común en apps de producción con historia (mantenimiento, migraciones progresivas).

**Ejemplo mínimo:**
```kotlin
val userLiveData = MutableLiveData<User>()
```

**Ejemplo real** (`ProductListViewModel.kt` + `ProductListScreenMVVM.kt`): el mismo evento de "productos actualizados" que dispara el `StateFlow` también se publica como `LiveData<Long?>`, únicamente para poder comparar ambas formas de observar en la misma pantalla:
```kotlin
// ViewModel
private val _lastUpdatedAt = MutableLiveData<Long?>(null)
val lastUpdatedAt: LiveData<Long?> = _lastUpdatedAt

// Compose (requiere la dependencia androidx.compose.runtime:runtime-livedata)
val lastUpdatedAt by vm.lastUpdatedAt.observeAsState()
```

---

## Comparación rápida

| Concepto   | Origen             | Uso principal                                   | Dónde vive en `Example/` |
|------------|--------------------|--------------------------------------------------|----------------------------|
| ViewModel  | Jetpack Lifecycle  | Guardar y exponer datos de la UI                | `ProductListViewModel` |
| State      | Jetpack Compose    | Estado observable local de UI                   | `onlyInStock` en `ProductListScreenMVVM` |
| Flow       | Kotlin Coroutines  | Flujo asíncrono de datos (streams)              | `ui: StateFlow<ProductListUiState>` |
| LiveData   | Jetpack Lifecycle  | Estado observable ligado al ciclo de vida       | `lastUpdatedAt: LiveData<Long?>` |

> ⬅️ Siguiente: vuelve a [README.md §5 y §6](./README.md#5-ciclo-de-vida-del-viewmodel) para ver el ciclo de vida del ViewModel (con `SavedStateHandle`) y el proyecto `Example/` completo, corriendo contra el endpoint real.
