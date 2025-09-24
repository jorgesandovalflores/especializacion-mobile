# Comparación de Arquitecturas en Android con Jetpack Compose

## MVC (Model–View–Controller)

### Dónde se ve el patrón
- **Controller**: `ProductController` coordina el *Model* (repo) y produce un **estado** (`MVCState`) para la **View**.
- **View**: observa `controller.uiState` y no conoce el repositorio.
- **Model**: `FakeProductRepository`.

### Rasgos clave
- El **Controller NO conoce a la View** (no hay callbacks).  
- La **View NO invoca al repo**; solo al Controller.  
- El estado se expone como `MVCState` (`Loading`, `Success`, `Error`).

### Flujo
```
View → Controller → Model
 ^                    |
 |--------------------| (estado observado)
```

### Ventajas y cautelas
- + Claro y testable.  
- – Scope manual, posible "Controller gordo".

---

## MVP (Model–View–Presenter)

### Dónde se ve el patrón
- **Presenter**: `ProductListPresenter` conoce a la **View** mediante un contrato (`ProductListContract.View`) y le empuja resultados.  
- **View**: implementa la interfaz y delega en el Presenter.  
- **Model**: `FakeProductRepository`.

### Rasgos clave
- **Acoplamiento explícito Presenter→View**.  
- La **View no tiene lógica**, solo renderiza.  
- El Presenter invoca métodos como `showProducts`, `showError`.

### Flujo
```
View ↔ Presenter → Model
(Presenter empuja resultados a la View)
```

### Ventajas y cautelas
- + Contrato claro, test fácil del Presenter.  
- – Gestión manual `attach/detach`.  
- – Riesgo de Presenter “gordo”.

---

## MVVM (Model–View–ViewModel)

### Dónde se ve el patrón
- **ViewModel**: `ProductListViewModel` no conoce a la View, expone `StateFlow` con un **UI State inmutable**.  
- **View**: observa el `StateFlow`.  
- **Model**: `FakeProductRepository`.

### Rasgos clave
- Usa `viewModelScope` (lifecycle-aware).  
- Patrón unidireccional: View→ViewModel→Model y ViewModel→View mediante estado observado.

### Flujo
```
View → ViewModel → Model
 ^                      |
 |----------------------| (estado observado)
```

### Ventajas y cautelas
- + Ideal con Compose.  
- + Encaja con UI state inmutable.  
- – Puede inflarse si maneja navegación compleja.

---

## ¿Por qué las tres parecen “observar mutables”?

El **mecanismo de entrega** (Flow, LiveData, callbacks) no define el patrón.  
Lo que define el patrón es **quién conoce a quién** y **cómo viaja la información**:

- **MVC**: View observa Controller. Controller no conoce a la View.  
- **MVP**: Presenter conoce a la View y le empuja datos.  
- **MVVM**: View observa ViewModel. ViewModel no conoce a la View.

---

## Tabla comparativa rápida

| Aspecto                    | MVC                                  | MVP                                              | MVVM                                        |
|-----------------------------|---------------------------------------|--------------------------------------------------|---------------------------------------------|
| ¿Quién conoce a la View?    | Nadie                                | **Presenter conoce View (contrato)**             | Nadie                                       |
| Entrega de resultados       | Estado observado                     | Callbacks (`showX(...)`)                         | Estado observado                             |
| Ciclo de vida               | Manual (`CoroutineScope`)             | Manual (`attach/detach`)                         | Automático (`viewModelScope`)               |
| Contrato explícito          | No                                   | **Sí** (`ProductListContract`)                   | No (UI State tipado)                        |
| Fit con Compose             | Correcto pero artesanal               | Correcto, con boilerplate extra                  | **Ideal y recomendado**                     |
| Riesgo de inflado           | Controller “gordo”                    | Presenter “gordo”                                | ViewModel “gordo” si asume navegación, etc. |

