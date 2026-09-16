# Implementación paso a paso — Listado de productos

Proceso seguido para construir esta demo (listado de productos consumido por MVC, MVP y MVVM contra `GET https://fakestoreapi.com/products`). Para el detalle de conceptos y la comparación de arquitecturas, ver el [README de la clase](../README.md).

---

## 1. Revisar el diseño (`design-m02-c01.pen`)

Antes de codear se revisa el mockup en Pencil para identificar pantallas, componentes (header, card, filtro) y estados (carga/error).

```
design-m02-c01.pen
        │
        ▼
Header · ProductCard · CategoryFilter · Loading / Error
```

## 2. Crear el proyecto

Se crea el proyecto Android con Compose y se agregan las dependencias (Retrofit, Coroutines) en `libs.versions.toml` / `build.gradle.kts`.

```
Example/
 ├── app/build.gradle.kts     (Compose · Retrofit · Coroutines)
 └── gradle/libs.versions.toml
```

## 3. Implementar la UI

Se construyen composables reutilizables sin lógica de negocio (solo reciben datos por parámetro), compartidos por MVC, MVP y MVVM.

```
ProductListContent
 ├── ProductListHeader
 ├── ProductCategoryFilter
 └── ProductCard × N
```

## 4. Implementar la capa de red/repositorio

Retrofit trae `ProductDto`, `ProductMapper` lo adapta a `Product` de dominio, y `ProductRepository` (interfaz) lo expone sin acoplar a la implementación real.

```
ProductApiService → ProductDto → ProductMapper → Product
                                       │
                                       ▼
                          ProductRepository (interfaz)
                         ↙                          ↘
        ProductRemoteRepository            FakeProductRepository
```

**Librerías (`gradle/libs.versions.toml` / `app/build.gradle.kts`):**

| Dependencia (`libs.*`) | Coordenada Maven | Uso |
| --- | --- | --- |
| `retrofit-core` | `com.squareup.retrofit2:retrofit:2.11.0` | Cliente HTTP declarativo; define `ProductApiService`. |
| `retrofit-gson` | `com.squareup.retrofit2:converter-gson:2.11.0` | Convierte el JSON de la API a `ProductDto`/`RatingDto`. |
| `okhttp-logging` | `com.squareup.okhttp3:logging-interceptor:4.12.0` | Interceptor que loguea cada request/response en Logcat (filtro `ProductRepository`). |

## 5. Conectar hacia la vista con MVVM

`ProductListViewModel` consulta al repositorio y expone `ProductListUiState` como `StateFlow`; `ProductListScreenMVVM` lo observa con `collectAsStateWithLifecycle()`.

```
ProductListScreenMVVM
        │ collectAsStateWithLifecycle()
        ▼
ProductListViewModel ──usa──▶ ProductRepository
        │ StateFlow<ProductListUiState>
        ▼
 Header + CategoryFilter + ProductCard × N
```

**Librerías (`gradle/libs.versions.toml` / `app/build.gradle.kts`):**

| Dependencia (`libs.*`) | Coordenada Maven | Uso |
| --- | --- | --- |
| `androidx-lifecycle-viewmodel-compose` | `androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4` | `viewModel(factory = ...)` para instanciar `ProductListViewModel` en Compose. |
| `androidx-lifecycle-runtime-compose` | `androidx.lifecycle:lifecycle-runtime-compose:2.9.4` | `collectAsStateWithLifecycle()` para observar el `StateFlow<ProductListUiState>`. |
| `androidx-lifecycle-runtime-ktx` | `androidx.lifecycle:lifecycle-runtime-ktx:2.9.4` | `viewModelScope`, `SavedStateHandle`/`createSavedStateHandle()` para el contador de reintentos. |
| `androidx-runtime-livedata` | `androidx.compose.runtime:runtime-livedata` | `observeAsState()` para exponer `lastUpdatedAt` (`LiveData`) en Compose. |

---

> 🔗 Detalle de patrones, comparación MVC/MVP/MVVM y ciclo de vida del ViewModel: [README de la clase](../README.md).
