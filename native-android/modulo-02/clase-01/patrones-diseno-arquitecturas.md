# Patrones de diseño de software y su aplicación en MVC, MVP y MVVM

> 📍 Documento 1 de la clase — ver el [menú completo en README.md](./README.md#menú-de-la-clase).

## 1. Patrones de diseño de software

### Creacionales

- **Factory Method**
  - Define una interfaz para crear objetos, dejando que subclases decidan qué instanciar.
  - *Ejemplo:* `ViewModelProvider.Factory` en Android; en `Example/`, la `Factory` de [`ProductListViewModel.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListViewModel.kt) construye el ViewModel inyectando un `SavedStateHandle` real, sin reflexión.
  ```
  [Creator] ---> factoryMethod() ---> [ConcreteProduct]
  ```

- **Singleton**
  - Garantiza una única instancia accesible globalmente.
  - *Ejemplo real:* [`RetrofitProvider.kt`](./Example/app/src/main/java/com/example/example/common/data/remote/RetrofitProvider.kt) — un único cliente `Retrofit`/`OkHttp` (con `by lazy`) compartido por toda la app.
  ```
  Client --> [Singleton Instance]
  ```

- **Builder**
  - Construye objetos complejos paso a paso, o con múltiples parámetros opcionales.
  - *Ejemplo real:* [`ProductListUiState.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListUiState.kt) — `data class` con valores por defecto (`loading`, `data`, `error`) que se construye con argumentos nombrados y se actualiza con `.copy(...)`, evitando constructores telescópicos.

---

### Estructurales

- **Adapter**
  - Convierte la interfaz de una clase en otra esperada por el cliente.
  - *Ejemplo real:* [`ProductMapper.kt`](./Example/app/src/main/java/com/example/example/common/data/remote/ProductMapper.kt) — traduce `ProductDto` (forma exacta del JSON de `fakestoreapi.com`) al `Product` de dominio que ya conocen MVC/MVP/MVVM.
  ```
  Client -> [Adapter] -> [IncompatibleClass]
  ```

- **Facade**
  - Ofrece una interfaz unificada y simple sobre un subsistema complejo.
  - *Ejemplo real:* [`ProductController.kt`](./Example/app/src/main/java/com/example/example/features/mvc/ProductController.kt) — oculta la llamada de red, el manejo de errores y el `CoroutineScope` detrás de dos miembros públicos: `uiState` y `load()`.

- **Composite**
  - Compone objetos en estructuras jerárquicas.
  - *Ejemplo:* jerarquía de UI en Compose (`Column`, `LazyColumn`), presente en las tres pantallas de `Example/`.

- **Proxy**
  - Sustituye a otro objeto para controlar el acceso.
  - *Ejemplo:* el `Presenter` en MVP actúa como proxy entre la `View` y el `Model` — ver [`ProductListPresenter.kt`](./Example/app/src/main/java/com/example/example/features/mvp/ProductListPresenter.kt).

- **Decorator**
  - Agrega responsabilidades a un objeto sin modificarlo ni heredar de él.
  - *Ejemplo real:* [`LoggingProductRepository.kt`](./Example/app/src/main/java/com/example/example/common/data/LoggingProductRepository.kt) — envuelve cualquier `ProductRepository` (por defecto `ProductRemoteRepository`) y agrega logs de inicio/éxito/error alrededor de `fetchProducts()`, sin tocar su lógica.

- **Bridge**
  - Desacopla abstracción de implementación.
  - *Ejemplo real:* `ProductListContract.View` en [`ProductListContract.kt`](./Example/app/src/main/java/com/example/example/features/mvp/ProductListContract.kt) — el contrato de View es independiente de su implementación en Compose (`ProductListScreenMVP.kt` la implementa como un `object` anónimo).

---

### Comportamiento

- **Observer**
  - Relación 1:N, los observadores reaccionan a cambios.
  - *Ejemplo real:* `controller.uiState`/`vm.ui` (`StateFlow`) observados con `collectAsStateWithLifecycle()` en las tres pantallas de `Example/`.
  ```
  [Subject] ---> notifica ---> [Observers]
  ```

- **State**
  - Permite representar y cambiar comportamiento según un estado interno.
  - *Ejemplo real:* `MVCState` (`Loading`/`Success`/`Error`) en MVC y `ProductListUiState` en MVVM — ambos `sealed class`/`data class` que la View usa con `when`.

- **Command**
  - Encapsula una petición como objeto o función.
  - *Ejemplo real:* `load()` (todas las arquitecturas) y `retry()` en [`ProductListViewModel.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListViewModel.kt) — acciones de UI que se invocan sin que quien las llama conozca cómo se resuelven.

- **Mediator**
  - Centraliza la comunicación entre objetos.
  - *Ejemplo real:* `ProductListPresenter` media entre la View (contrato) y el Model (repositorio) en MVP.

- **Strategy**
  - Define una familia de algoritmos intercambiables.
  - *Ejemplo real:* `ProductRepository` como interfaz permite intercambiar la estrategia de obtención de datos (`ProductRemoteRepository` ↔ `FakeProductRepository`) sin tocar la capa de presentación — `ProductController`, `ProductListPresenter` y `ProductListViewModel` reciben cualquiera de las dos por el mismo parámetro.

- **Template Method**
  - Define el esqueleto de un algoritmo, delegando pasos a subclases o a callbacks.
  - *Ejemplo real:* el flujo `showLoading() → fetch → showProducts()/showError()` de `ProductListPresenter.load()` — el mismo esqueleto se repite en `ProductController.load()` y `ProductListViewModel.load()`.

- **Memento**
  - Guarda y restaura el estado de un objeto sin exponer sus detalles internos.
  - *Ejemplo real:* `savedStateHandle.getStateFlow(KEY_RETRY_COUNT, 0)` en [`ProductListViewModel.kt`](./Example/app/src/main/java/com/example/example/features/mvvm/ProductListViewModel.kt) — el contador de reintentos sobrevive incluso a que el sistema mate el proceso (ver README.md §5).

---

## 2. Aplicación en el ejemplo: listado de productos

### MVC
- **Facade** → `ProductController` simplifica el acceso al repositorio.
- **Observer** → la View observa `uiState` del Controller.
- **State** → `MVCState` representa Loading/Success/Error.

### MVP
- **Mediator** → el Presenter centraliza la comunicación View-Model.
- **Proxy** → el Presenter actúa como proxy de la View hacia el Model.
- **Template Method** → flujo de carga: `showLoading → fetch → showProducts/showError`.
- **Observer** → el Presenter reacciona al resultado del repositorio (`runCatching { }.onSuccess/onFailure`).

### MVVM
- **Observer** → la View observa el `StateFlow` (y el `LiveData`) del ViewModel.
- **State** → `ProductListUiState` como estado inmutable de la UI.
- **Facade** → el ViewModel expone una API simplificada (`ui`, `lastUpdatedAt`, `load()`, `retry()`).
- **Memento** → `SavedStateHandle` persiste el contador de reintentos.
- **Command** → `load()`/`retry()` como acciones de UI enviadas al ViewModel.

### Transversal a las tres (capa de datos)
- **Singleton** → `RetrofitProvider` (una sola instancia de Retrofit/OkHttp).
- **Adapter** → `ProductMapper` (`ProductDto` → `Product`).
- **Decorator** → `LoggingProductRepository` (logging alrededor del repositorio real).
- **Strategy / Dependency Inversion** → `ProductRepository` como interfaz, con `ProductRemoteRepository` y `FakeProductRepository` como implementaciones intercambiables.

---

## Conclusión

- **MVC**: se apoya en *Facade, Observer, State*.
- **MVP**: usa *Mediator/Proxy* y *Template Method*, y enfatiza contratos explícitos.
- **MVVM**: combina *State + Observer* como núcleo, más *Memento* y *Command* para el ciclo de vida y las acciones de usuario, integrados naturalmente con Compose.
- Las tres comparten la misma capa de datos (*Singleton*, *Adapter*, *Decorator*, *Strategy*), que es justamente donde vive el endpoint real — ver [README.md §6](./README.md#6-proyecto-example-todo-junto-contra-un-endpoint-real).

> ⬅️ Siguiente en el menú: [comparacion-arquitecturas.md](./comparacion-arquitecturas.md).
