# ViewModel, State, Flow y LiveData en Android

## ViewModel
- Clase de **Android Jetpack** para separar la lógica de negocio de la UI.
- Pertenece al paquete `androidx.lifecycle`.
- Sobrevive a cambios de configuración (rotación de pantalla).
- Se utiliza en el patrón **MVVM** para exponer datos a la interfaz.

**Ejemplo:**
```kotlin
class ProductListViewModel : ViewModel() {
    val products = MutableLiveData<List<Product>>()
}
```

---

## State
- Concepto de **Jetpack Compose**.
- Representa un valor observable que, al cambiar, dispara la **recomposición**.
- Se crea con `remember { mutableStateOf(...) }`.
- Ideal para estados locales en UI.

**Ejemplo:**
```kotlin
var counter by remember { mutableStateOf(0) }
Button(onClick = { counter++ }) {
    Text("Clicks: $counter")
}
```

---

## Flow
- Parte de **Kotlin Coroutines**.
- Representa un flujo **asíncrono de datos** (stream).
- Puede emitir múltiples valores en el tiempo.
- Se usa en repositorios y ViewModels.

**Ejemplo:**
```kotlin
val products: Flow<List<Product>> = repository.getProducts()
```

---

## LiveData
- Clase de **Jetpack Lifecycle**.
- Contenedor observable **ligado al ciclo de vida** de Activities/Fragments.
- Evita fugas de memoria.
- Hoy en día suele reemplazarse por **StateFlow** en muchos casos.

**Ejemplo:**
```kotlin
val userLiveData = MutableLiveData<User>()
```

---

## Comparación rápida

| Concepto   | Origen             | Uso principal                                   |
|------------|--------------------|--------------------------------------------------|
| ViewModel  | Jetpack Lifecycle  | Guardar y exponer datos de la UI                |
| State      | Jetpack Compose    | Estado observable local de UI                   |
| Flow       | Kotlin Coroutines  | Flujo asíncrono de datos (streams)              |
| LiveData   | Jetpack Lifecycle  | Estado observable ligado al ciclo de vida       |
