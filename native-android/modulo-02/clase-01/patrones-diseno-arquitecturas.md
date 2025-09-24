# Patrones de diseño de software y su aplicación en arquitecturas MVC, MVP y MVVM

## 1. Patrones de diseño de software

### Creacionales

- **Factory Method**
  - Define una interfaz para crear objetos, dejando que subclases decidan qué instanciar.
  - *Ejemplo:* `ViewModelProvider.Factory` en Android.
  ```
  [Creator] ---> factoryMethod() ---> [ConcreteProduct]
  ```

- **Singleton**
  - Garantiza una única instancia accesible globalmente.
  - *Ejemplo:* un repositorio compartido de productos.
  ```
  Client --> [Singleton Instance]
  ```

- **Builder**
  - Construye objetos complejos paso a paso.
  - *Ejemplo:* construir un `UiState` con múltiples parámetros opcionales.

---

### Estructurales

- **Adapter**
  - Convierte la interfaz de una clase en otra esperada por el cliente.
  - *Ejemplo:* transformar `Product` en `ProductUiModel` para la UI.
  ```
  Client -> [Adapter] -> [IncompatibleClass]
  ```

- **Facade**
  - Ofrece una interfaz unificada y simple sobre un subsistema complejo.
  - *Ejemplo:* un `ProductController` que orquesta repositorios.

- **Composite**
  - Compone objetos en estructuras jerárquicas.
  - *Ejemplo:* jerarquía de UI en Compose (`Column`, `LazyColumn`).

- **Proxy**
  - Sustituye a otro objeto para controlar el acceso.
  - *Ejemplo:* Presenter como proxy entre View y Model.

- **Decorator**
  - Agrega responsabilidades dinámicamente.
  - *Ejemplo:* agregar logging a un repositorio.

- **Bridge**
  - Desacopla abstracción de implementación.
  - *Ejemplo:* contrato `View` separado de su implementación.

---

### Comportamiento

- **Observer**
  - Relación 1:N, los observadores reaccionan a cambios.
  - *Ejemplo:* `StateFlow` observado por la UI.
  ```
  [Subject] ---> notifica ---> [Observers]
  ```

- **State**
  - Permite cambiar comportamiento según estado interno.
  - *Ejemplo:* `UiState` con Loading/Success/Error.

- **Command**
  - Encapsula una petición como objeto.
  - *Ejemplo:* acción “cargar productos”.

- **Mediator**
  - Centraliza la comunicación entre objetos.
  - *Ejemplo:* Presenter media entre View y Model.

- **Strategy**
  - Define una familia de algoritmos intercambiables.
  - *Ejemplo:* diferentes estrategias de filtrado.

- **Template Method**
  - Define el esqueleto de un algoritmo, delegando pasos a subclases.
  - *Ejemplo:* Presenter con flujo estándar `showLoading → fetch → render`.

- **Memento**
  - Guarda y restaura el estado de un objeto.
  - *Ejemplo:* `SavedStateHandle` en ViewModel.

---

## 2. Aplicación en el ejemplo: listado de productos

### MVC
- **Observer** → la View observa `uiState` del Controller.
- **Facade** → el Controller simplifica el acceso al repositorio.
- **State** → `MVCState` representa Loading/Success/Error.

### MVP
- **Mediator** → el Presenter centraliza la comunicación View-Model.
- **Proxy** → el Presenter actúa como proxy de la View hacia el Model.
- **Template Method** → flujo de carga: `showLoading → fetch → showProducts/showError`.
- **Observer** → Presenter observa resultados del repositorio.

### MVVM
- **Observer** → la View observa el `StateFlow` del ViewModel.
- **State** → `ProductListUiState` como estado inmutable de la UI.
- **Facade** → el ViewModel expone un API simplificado.
- **Memento** → `SavedStateHandle` para persistir estado.
- **Command** → eventos de UI enviados al ViewModel (`load()`).

---

## Conclusión

- **MVC**: se apoya en *Observer, Facade, State*.  
- **MVP**: usa *Mediator/Proxy* y enfatiza contratos explícitos.  
- **MVVM**: combina *State + Observer* como núcleo, integrados naturalmente con Compose.
