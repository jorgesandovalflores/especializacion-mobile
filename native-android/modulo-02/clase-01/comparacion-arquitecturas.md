# Comparación de Arquitecturas en Android con Jetpack Compose

> 📍 Documento 2 de la clase — ver el [menú completo en README.md](./README.md#menú-de-la-clase). Requiere haber visto [patrones-diseno-arquitecturas.md](./patrones-diseno-arquitecturas.md).

Las tres arquitecturas de este documento consumen el **mismo endpoint real** (`GET https://fakestoreapi.com/products`, ver [README.md §6](./README.md#6-proyecto-example-todo-junto-contra-un-endpoint-real)) a través de la misma interfaz `ProductRepository`. Eso permite comparar solo la capa de presentación, sin que el origen de los datos cambie la comparación.

## MVC (Model–View–Controller)

### Dónde se ve el patrón
- **Controller**: [`ProductController`](./Example/app/src/main/java/com/example/example/features/mvc/ProductController.kt) coordina el *Model* (`ProductRepository`) y produce un **estado** (`MVCState`) para la **View**.
- **View**: [`ProductListScreenMVC`](./Example/app/src/main/java/com/example/example/features/mvc/ProductListScreenMVC.kt) observa `controller.uiState` con `collectAsStateWithLifecycle()` y no conoce al repositorio.
- **Model**: `ProductRepository` — por defecto `LoggingProductRepository(ProductRemoteRepository())`, consumiendo el endpoint real.

### Rasgos clave
- El **Controller NO conoce a la View** (no hay callbacks).
- La **View NO invoca al repo**; solo al Controller.
- El estado se expone como `MVCState` (`Loading`, `Success`, `Error`).
- Ante un error de red, la View muestra un botón "Reintentar" que vuelve a llamar a `controller.load()`.

### Flujo
```
View → Controller → Model
 ^                    |
 |--------------------| (estado observado)
```

### Ventajas y cautelas
- + Claro y testable.
- – Scope manual (`CoroutineScope(SupervisorJob() + Dispatchers.IO)`), posible "Controller gordo".

---

## MVP (Model–View–Presenter)

### Dónde se ve el patrón
- **Presenter**: [`ProductListPresenter`](./Example/app/src/main/java/com/example/example/features/mvp/ProductListPresenter.kt) conoce a la **View** mediante un contrato ([`ProductListContract.View`](./Example/app/src/main/java/com/example/example/features/mvp/ProductListContract.kt)) y le empuja resultados.
- **View**: [`ProductListScreenMVP`](./Example/app/src/main/java/com/example/example/features/mvp/ProductListScreenMVP.kt) implementa la interfaz como un `object` anónimo y delega en el Presenter.
- **Model**: `ProductRepository` — mismo endpoint real que MVC y MVVM.

### Rasgos clave
- **Acoplamiento explícito Presenter→View**.
- La **View no tiene lógica**, solo renderiza y traduce callbacks a `State` local de Compose.
- El Presenter invoca métodos como `showProducts`, `showError`.
- `attach()`/`detach()` se gestionan en un `DisposableEffect(Unit)`, para no filtrar la View tras salir de pantalla.

### Flujo
```
View ↔ Presenter → Model
(Presenter empuja resultados a la View)
```

### Ventajas y cautelas
- + Contrato claro, test fácil del Presenter (se le puede inyectar un `ProductRepository` fake y una `View` de prueba).
- – Gestión manual `attach/detach`.
- – Riesgo de Presenter "gordo".

---

## MVVM (Model–View–ViewModel)

### Dónde se ve el patrón
- **ViewModel**: [`ProductListViewModel`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListViewModel.kt) no conoce a la View, expone un **UI State inmutable** vía `StateFlow` (y, para comparar, el mismo dato también como `LiveData`).
- **View**: [`ProductListScreenMVVM`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListScreenMVVM.kt) observa el `StateFlow` con `collectAsStateWithLifecycle()`.
- **Model**: `ProductRepository` — mismo endpoint real que MVC y MVP.

### Rasgos clave
- Usa `viewModelScope` (lifecycle-aware): no hay `CoroutineScope` manual que cancelar.
- Patrón unidireccional: View→ViewModel→Model y ViewModel→View mediante estado observado.
- El contador de reintentos vive en `SavedStateHandle`: sobrevive incluso a la muerte de proceso (ver [README.md §5](./README.md#5-ciclo-de-vida-del-viewmodel)), algo que MVC y MVP no resuelven de fábrica.

### Flujo
```
View → ViewModel → Model
 ^                      |
 |----------------------| (estado observado)
```

### Ventajas y cautelas
- + Ideal con Compose.
- + Encaja con UI state inmutable.
- + Ciclo de vida y persistencia de estado (`SavedStateHandle`) resueltos por el framework.
- – Puede inflarse si maneja navegación compleja.

---

## ¿Por qué las tres parecen "observar mutables"?

El **mecanismo de entrega** (Flow, LiveData, callbacks) no define el patrón.
Lo que define el patrón es **quién conoce a quién** y **cómo viaja la información** (ver [patrones-diseno-arquitecturas.md](./patrones-diseno-arquitecturas.md) para el detalle de cada patrón GoF involucrado):

- **MVC**: View observa Controller. Controller no conoce a la View.
- **MVP**: Presenter conoce a la View y le empuja datos.
- **MVVM**: View observa ViewModel. ViewModel no conoce a la View.

---

## Tabla comparativa rápida

| Aspecto                    | MVC                                  | MVP                                              | MVVM                                        |
|-----------------------------|---------------------------------------|----------------------------------------------------|-----------------------------------------------|
| ¿Quién conoce a la View?    | Nadie                                | **Presenter conoce View (contrato)**             | Nadie                                       |
| Entrega de resultados       | Estado observado                     | Callbacks (`showX(...)`)                         | Estado observado                             |
| Ciclo de vida               | Manual (`CoroutineScope`)             | Manual (`attach/detach`)                         | Automático (`viewModelScope`)               |
| Persistencia ante muerte de proceso | No                             | No                                                | **Sí, vía `SavedStateHandle`**              |
| Contrato explícito          | No                                   | **Sí** (`ProductListContract`)                   | No (UI State tipado)                        |
| Fit con Compose             | Correcto pero artesanal               | Correcto, con boilerplate extra                  | **Ideal y recomendado**                     |
| Riesgo de inflado           | Controller "gordo"                    | Presenter "gordo"                                | ViewModel "gordo" si asume navegación, etc. |
| Origen de datos en `Example/` | `ProductRepository` (endpoint real) | `ProductRepository` (endpoint real)              | `ProductRepository` (endpoint real)         |

> 🔜 **Y MVI?** El siguiente paso natural desde MVVM en apps grandes es **MVI/UDF**: un único `UiState` inmutable (que `ProductListUiState` ya se parece a) más `Intent`/eventos tipados en vez de varios métodos sueltos como `load()`/`retry()`. No se implementa en esta clase — queda propuesto como ejercicio en [README.md](./README.md#ejercicios-propuestos).

> ⬅️ Siguiente en el menú: [viewmodel-flow-state-livedata.md](./viewmodel-flow-state-livedata.md).
