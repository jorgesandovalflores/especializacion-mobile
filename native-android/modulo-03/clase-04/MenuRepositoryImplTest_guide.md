
# Guía de pruebas — `MenuRepositoryImplTest`

Esta guía explica **paso a paso** la clase de pruebas `MenuRepositoryImplTest`, los **dobles de prueba** (mocks), la **configuración del entorno**, y cada **caso de uso** validado para el repositorio `MenuRepositoryImpl` en un proyecto Android/Kotlin con corrutinas.

---

## 1) Propósito de la clase de prueba

Validar que `MenuRepositoryImpl`:
- **Obtiene** el menú desde la API remota y **mapea** correctamente `MenuDto → Menu`.
- **Observa** el menú en la base de datos local (`Room`) y **mapea** `MenuEntity → Menu` en orden.
- **Guarda** correctamente una lista de `Menu` como `MenuEntity` en la base de datos local (incluyendo conversiones de campos y tamaño de la lista).
- **Se comporta** correctamente frente a **listas vacías** tanto en remoto como en local.

En términos de arquitectura limpia, cubre flujos **Data Source Remoto**, **Data Source Local** y **conversión/almacenamiento**.

---

## 2) Dependencias y dobles de prueba

La clase crea **mocks** para desacoplar el repositorio de sus dependencias reales:
- `MenuDao` (local): acceso a base de datos (Room).
- `MenuApi` (remoto): acceso a API HTTP.
- `Log.d` (Android): se **mockea** para evitar errores de entorno JVM en pruebas de unidad.

Librerías usadas:
- `mockk`: para crear y verificar mocks (`mockk`, `coEvery`, `coVerify`, `mockkStatic`, `every`).
- `kotlinx-coroutines-test`: para testear funciones `suspend` y flujos con `runTest`.
- `flowOf`: para simular `Flow<List<MenuEntity>>` de `MenuDao.observeAll()`.

---

## 3) Fixtures de datos (datos de prueba)

Se definen tres colecciones equivalentes del mismo menú, en tres **capas** distintas del modelo:

1. **Dominio** (`List<Menu>`): `testMenuList`  
   ```kotlin
   Menu(key="home", text="Inicio", iconUrl="icon_home.png", deeplink="app://home", order=1)
   // ... profile, settings
   ```

2. **Base de datos** (`List<MenuEntity>`): `testMenuEntities`  
   ```kotlin
   MenuEntity(id="home", text="Inicio", icon="icon_home.png", deeplink="app://home", updatedAt=123456789L, position=1)
   // ... profile, settings
   ```

3. **Remoto** (`List<MenuDto>`): `testMenuDtos`  
   ```kotlin
   MenuDto(key="home", text="Inicio", iconUrl="icon_home.png", deeplink="app://home", order=1)
   // ... profile, settings
   ```

Estas colecciones permiten verificar **mapeos** (DTO/Entity ↔ Domain) y **comportamientos** del repositorio sin depender de infraestructura real.

---

## 4) `@Before setUp()`

1. **Mock del logger Android** para JVM:
   ```kotlin
   mockkStatic(Log::class)
   every { Log.d(any(), any()) } returns 0
   ```
   Evita fallas cuando el código del repositorio loguea en `Log.d`.

2. **Mocks relajados** de DAO y API:  
   ```kotlin
   mockDao = mockk(relaxed = true)
   mockApi = mockk(relaxed = true)
   ```
   `relaxed=true` minimiza boilerplate: los métodos no stubbeados devuelven valores por defecto razonables.

3. **Repositorio bajo prueba**:
   ```kotlin
   repository = MenuRepositoryImpl(
       dao = mockDao,
       api = mockApi,
       io = UnconfinedTestDispatcher()
   )
   ```
   Se inyecta un **dispatcher de pruebas** (no bloquea el test, ejecuta de forma determinista).

> Nota: Usar `UnconfinedTestDispatcher()` facilita ejecutar corrutinas sin necesidad de `Dispatchers.setMain()`.

---

## 5) `@After tearDown()`

El bloque está vacío en el código mostrado. En general, si se usa `mockkStatic`, es buena práctica limpiar con `unmockkAll()` o `unmockkStatic(Log::class)` para evitar fugas entre tests.

---

## 6) Casos de prueba

A continuación, cada prueba con su **escenario (Given)**, **acción (When)** y **verificación (Then)**.

### 6.1 `getMenuRemote()`

**Objetivo:** validar que el repositorio llama a la API, mapea `MenuDto → Menu` y devuelve la lista correcta.

- **Given:**  
  ```kotlin
  coEvery { mockApi.getMenu() } returns testMenuDtos
  ```

- **When:**  
  ```kotlin
  val result = repository.getMenuRemote()
  ```

- **Then:**  
  - El tamaño coincide con `testMenuList.size`.
  - Cada campo del primer elemento (`key/text/iconUrl/deeplink/order`) coincide con el esperado en dominio.
  - La API se invoca exactamente **1 vez**:  
    ```kotlin
    coVerify(exactly = 1) { mockApi.getMenu() }
    ```

**Qué valida realmente:** el **mapeo DTO → dominio** y la **orquestación** del repositorio sobre la **fuente remota**.

---

### 6.2 `getMenuRemote_withEmptyList()`

**Objetivo:** asegurar manejo correcto de **listas vacías** desde el backend.

- **Given:** API devuelve `emptyList()`.
- **When:** llamada a `getMenuRemote()`.
- **Then:** el resultado es **vacío** y la API se invoca **1 vez**.

**Qué valida realmente:** que no haya excepciones/errores al mapear **lista vacía**, y que el repositorio **propaga** esa condición sin alterarla.

---

### 6.3 `getMenuLocal()`

**Objetivo:** validar suscripción al **origen local** (Room) y mapeo `Entity → dominio`.

- **Given:**  
  ```kotlin
  coEvery { mockDao.observeAll() } returns flowOf(testMenuEntities)
  ```
  Se simula un `Flow<List<MenuEntity>>` con datos.

- **When:**  
  ```kotlin
  val result = repository.getMenuLocal()
  ```

- **Then:**  
  - Tamaño igual a `testMenuList.size`.
  - Chequeos de **campos** (`key/text/deeplink/order`).
  - Verificación de que `observeAll()` se llama **exactamente 1 vez**.

**Qué valida realmente:** el **mapeo Entity → dominio** y la **observación** de la base de datos local.

> Nota: Según la implementación, `getMenuLocal()` podría **recoger el primer valor** del Flow o **convertir** el Flow en una lista. La prueba asume que el método devuelve una **colección** ya derivada del flujo.

---

### 6.4 `getMenuLocal_withEmptyDatabase()`

**Objetivo:** comportamiento con una base local **sin registros**.

- **Given:** `observeAll()` devuelve `flowOf(emptyList())`.
- **When:** `repository.getMenuLocal()`.
- **Then:** resultado **vacío** y `observeAll()` invocado **1 vez**.

**Qué valida realmente:** la rama de **lista vacía** en el origen local.

---

### 6.5 `saveMenuLocal()`

**Objetivo:** asegurar que se **convierta** `List<Menu>` a `List<MenuEntity>` y se **persista** con el DAO.

- **Given:**  
  ```kotlin
  coEvery { mockDao.upsertAll(any()) } returns Unit
  coEvery { mockDao.observeAll() } returns flowOf(emptyList())
  ```

- **When:**  
  ```kotlin
  repository.saveMenuLocal(testMenuList)
  ```

- **Then:** se verifica que:
  - `upsertAll()` es llamado **1 vez**.
  - El **tamaño** de entidades coincide con el de `testMenuList` (verificación con `match { entities -> … }`).

**Qué valida realmente:** el **mapeo dominio → Entity** y la **orquestación** de guardado.

---

### 6.6 `saveMenuLocal_withEmptyList()`

**Objetivo:** al guardar **lista vacía**, el repositorio **no** debe fallar y debe **propagar** esa condición al DAO.

- **Given:** stubs de `upsertAll` y `observeAll`.
- **When:** `repository.saveMenuLocal(emptyList())`.
- **Then:**  
  ```kotlin
  coVerify(exactly = 1) { mockDao.upsertAll(emptyList()) }
  ```

**Qué valida realmente:** política de **no-op seguro** cuando no hay elementos que persistir.

---

### 6.7 `saveMenuLocal_verifyConversion()`

**Objetivo:** validar la **conversión de campos** concretos en un caso simple (un solo elemento).

- **Given:**  
  ```kotlin
  val menuToSave = listOf(
      Menu(key="cart", text="Carrito", iconUrl="icon_cart.png", deeplink="app://cart", order=4)
  )
  coEvery { mockDao.upsertAll(any()) } returns Unit
  coEvery { mockDao.observeAll() } returns flowOf(emptyList())
  ```

- **When:**  
  ```kotlin
  repository.saveMenuLocal(menuToSave)
  ```

- **Then:** verificación **field-by-field** con `match { entities -> … }`:
  - `id == "cart"`
  - `text == "Carrito"`
  - `icon == "icon_cart.png"`
  - `deeplink == "app://cart"`
  - `position == 4`

**Qué valida realmente:** que el **mapper** `Menu → MenuEntity` asigna **nombres y posiciones** correctas (e.g., `key → id`, `iconUrl → icon`, `order → position`).

---

## 7) Decisiones de diseño validadas

- **Separación de modelos por capa**: DTO/Entity/Dominio poseen nombres de campos distintos y se **mapean** de forma explícita.
- **Resiliencia a listas vacías**: tanto remoto como local, el repositorio responde sin fallos ni valores inesperados.
- **Uso de corrutinas y Flow**: las pruebas corren en entorno controlado (`runTest`, `UnconfinedTestDispatcher`) y simulan streams de datos locales.
- **Verificación de interacción**: cada test asegura que se invocan **exactamente** los métodos esperados (`coVerify(exactly = 1)`).

---

## 8) Posibles mejoras en las pruebas

1. **Limpieza en `@After`**: llamar a `unmockkAll()` o `unmockkStatic(Log::class)`.
2. **Ordenamiento**: si el repositorio garantiza orden por `order/position`, verificar la lista completa en ese orden.
3. **Errores de red/BD**: agregar pruebas de **excepciones** (e.g., `IOException`) y políticas de reintento si existen.
4. **Zonas horarias/`updatedAt`**: si `updatedAt` influye en sincronización, cubrir esos casos.
5. **Cobertura de nulos**: si algún campo es opcional en DTO/Entity, validar cómo se mapea.

---

## 9) Cómo ejecutar las pruebas

Desde Gradle:
```bash
./gradlew test
```

Con filtros por clase:
```bash
./gradlew test --tests "*MenuRepositoryImplTest"
```

Si usas Android Studio, puedes ejecutar directamente con el botón **Run Test** en la clase/prueba.

---

## 10) Checklist mental de lectura de pruebas

- ¿Los **mocks** tienen los `coEvery` adecuados?
- ¿Los **Then** verifican tamaño **y** campos clave?
- ¿Se verifica **exactamente 1 llamada** a DAO/API cuando aplica?
- ¿Las **conversiones** entre modelos cambian nombres de campos correctamente?
- ¿Se prueban **listas vacías** y casos positivos?

---

## 11) Resumen

La suite valida el **circuito completo** del repositorio: lectura **remota** (con mapeo correcto), lectura **local** (observación + mapeo) y **persistencia** (conversión + upsert), incluyendo ramas para **listas vacías**. Con esto se obtiene una **confianza** razonable en el comportamiento del repositorio sin depender de red ni base de datos reales.
