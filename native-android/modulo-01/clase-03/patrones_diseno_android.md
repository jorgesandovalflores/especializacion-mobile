# Patrones de diseño en Android

> Catálogo de referencia rápida para reconocer y nombrar los patrones de UI/UX y de arquitectura que más aparecen en apps Android modernas construidas con Jetpack Compose y Material 3. Complementa la [Sesión 3 — States y Recomposición](./README.md): allí se explica *cómo* modelar el estado detrás de estos patrones; aquí se cataloga *qué* patrón usar y cuándo.

## 1. Navegación

-   **Toolbar / AppBar**: Cabecera superior con título, acciones y
    navegación.
    ![Diagrama](./img/toolbar.png)
-   **BottomNavigation**: Barra inferior para moverse entre secciones
    principales.
    ![Diagrama](./img/bottom_navigation.png)
-   **Drawer Navigation**: Menú lateral deslizable con accesos a
    secciones secundarias.
    ![Diagrama](./img/drawer_navigation.png)
-   **Navigation Rail**: Variante de navegación vertical en pantallas
    grandes (tablets, foldables).
    ![Diagrama](./img/reail_navigation.png)
-   **BottomSheet Navigation**: Navegación o acciones emergentes desde
    la parte inferior.
    ![Diagrama](./img/bottom_sheet_navigation.png)
-   **Stepper / Wizard**: Flujo paso a paso (ej. onboarding o
    formularios largos).
    ![Diagrama](./img/stepper_wizard.png)
-   **Predictive Back**: Gesto de retroceso que muestra una vista previa
    (encogimiento/cross-fade) de la pantalla destino antes de soltar el
    dedo. Requiere `enableOnBackInvokedCallback="true"` en el manifest y,
    en Compose, `PredictiveBackHandler`/`BackHandler`; con `targetSdk 36`
    ya es el comportamiento por defecto del sistema.

## 2. Estructura de Pantallas

-   **Single Activity + Fragments/Compose**: Toda la app en una sola
    actividad, pantallas gestionadas por navegación.
    ![Diagrama](./img/fragment_compose.png)
-   **Master-Detail (Responsive)**: Lista + detalle en tablet, solo
    lista o detalle en mobile.
    ![Diagrama](./img/master_detail.png)
-   **Tabs (Top Tabs / ViewPager2 + TabLayout)**: Cambio de vistas
    mediante pestañas.
    ![Diagrama](./img/tab_viewpager.png)
-   **Dashboard / Grid**: Pantallas principales con accesos rápidos en
    cuadrícula o tarjetas.
    ![Diagrama](./img/dashboard_grid.png)
-   **Full Screen / Immersive**: Pantallas completas (ej. video, mapas,
    juegos).
    ![Diagrama](./img/full_screen.png)
-   **Adaptive / Canonical Layouts (`ListDetailPaneScaffold`,
    `NavigationSuiteScaffold`)**: Una sola composable que reorganiza
    lista+detalle, o la barra de navegación, según la clase de ancho
    (`WindowSizeClass`) — compacto (celular), medio (foldable/tablet
    vertical) o expandido (tablet/desktop). Vienen de
    `androidx.compose.material3.adaptive` y son la evolución
    recomendada de "Master-Detail" manual.

## 3. Componentes Interactivos

-   **Dialogs**: Confirmaciones, alertas o formularios.
    ![Diagrama](./img/dialog.png)
-   **BottomSheet (Modal / Persistent)**: Contenido flotante inferior,
    fijo o modal.
    ![Diagrama](./img/bottomsheet_dialog.png)
-   **Snackbars**: Mensajes breves en la parte inferior.
    ![Diagrama](./img/snackbar.png)
-   **Chips**: Selección rápida o categorización.
    ![Diagrama](./img/chips.png)
-   **Cards**: Presentación de información en bloques visuales.
    ![Diagrama](./img/card.png)
-   **FAB (Floating Action Button)**: Acción principal destacada.
    ![Diagrama](./img/fab.webp)

## 4. Patrones de Lista y Contenido

-   **List / LazyColumn**: Listas simples o complejas. En Compose,
    `RecyclerView` se reemplaza por `LazyColumn`/`LazyRow`, que ya
    hacen *composition* y *recycling* de items automáticamente; usar
    siempre `key = { it.id }` para preservar estado y animar bien los
    reordenamientos.
-   **Sectioned List**: Listas agrupadas por categorías. En Compose se
    logra con múltiples bloques `item {}` / `items(...)` dentro de un
    mismo `LazyColumn`, intercalando encabezados sticky con
    `stickyHeader {}`.
-   **Infinite Scroll / Pagination**: Carga progresiva de elementos,
    normalmente con la librería `androidx.paging` (`Pager` +
    `collectAsLazyPagingItems()`), que además maneja loading/error por
    página.
-   **Swipe to Refresh**: Refrescar contenido con gesto hacia abajo.
    En Compose Material 3: `PullToRefreshBox`.
-   **Swipe Actions**: Acciones rápidas en items (eliminar, archivar).
    En Compose: `SwipeToDismissBox` combinado con `rememberSwipeToDismissBoxState`.
-   **Staggered Grid**: Tarjetas de tamaño variable (ej. Pinterest).
    En Compose: `LazyVerticalStaggeredGrid`.

## 5. Arquitectura y Organización

-   **MVC (Model-View-Controller)**: Clásico, hoy poco usado en Android
    moderno.
-   **MVP (Model-View-Presenter)**: Separación entre lógica y vista, con
    presenter.
-   **MVVM (Model-View-ViewModel)**: Estándar en Android actual con
    LiveData/StateFlow.
-   **MVI (Model-View-Intent)**: Flujo unidireccional de eventos y
    estados.
-   **Clean Architecture (UseCases + Layers)**: Separación en capas de
    dominio, datos y presentación.
-   **Repository Pattern**: Abstracción de acceso a datos (DB, API,
    cache).
-   **State Holder Pattern**: Una clase (no un `ViewModel`) que agrupa
    y valida el estado de **UI pura** de una pantalla o componente
    (ej. estado de un formulario, de un `Pager`, de un `Snackbar`).
    Se crea con `remember { MyFormState(...) }` y vive solo mientras el
    composable está en pantalla. Es el punto intermedio entre
    `mutableStateOf` suelto (ver [README, sección 2](./README.md#2-remember-mutablestateof-y-statet))
    y un `ViewModel` completo: úsalo cuando el estado no necesita
    sobrevivir a la Activity ni requiere lógica de dominio/repositorio.

## 6. Carga y Estado

-   **Skeleton Screens**: Placeholder simulando contenido mientras
    carga.
-   **Shimmer Effect**: Animación que indica carga en progreso.
-   **Empty State**: Pantalla con mensaje cuando no hay datos.
-   **Error State**: Manejo visual de errores (ej. desconexión, API
    fallida).
-   **Loading Overlay / Spinner**: Indicador central de carga.

## 7. Comunicación y Eventos

-   **Observer Pattern**: LiveData, Flow, StateFlow para observar
    cambios.
-   **Event Bus / SharedFlow**: Comunicación desacoplada entre
    componentes.
-   **Unidirectional Data Flow (UDF)**: Datos fluyen en una sola
    dirección (muy usado con Compose).

## 8. Persistencia y Data

-   **DAO (Data Access Object)**: Patrón con Room/SQL para acceso a DB.
-   **Singleton**: Instancia única para servicios (ej. Retrofit, Room).
-   **Cache Pattern**: Guardar en memoria/DB para evitar llamadas
    innecesarias.
-   **DataStore (Preferences / Proto)**: Reemplazo recomendado de
    `SharedPreferences`; expone los datos como `Flow`, es asíncrono
    (no bloquea el hilo principal) y `Proto DataStore` además tipa el
    esquema con Protocol Buffers. Úsalo para settings de usuario, no
    para datos relacionales (eso sigue siendo Room).

## 9. Layouts adaptativos y Material 3 Expressive

-   **`WindowSizeClass`**: Punto de entrada para diseño responsive;
    clasifica el ancho/alto disponible en `Compact`/`Medium`/`Expanded`
    y reemplaza el uso de `dp` fijos o `sw600dp` de la era de Views.
-   **`NavigationSuiteScaffold`**: Composable único que alterna
    automáticamente entre `BottomNavigation` (compacto),
    `NavigationRail` (medio) y `Drawer` permanente (expandido) según el
    `WindowSizeClass` — evita mantener tres layouts de navegación por
    separado.
-   **Material 3 Expressive**: Evolución de Material 3 (desde 2025) con
    formas más dinámicas, nuevas variantes de botones
    (`ButtonGroup`, `ToggleButton`) y motion "spring-based" por
    defecto en vez de curvas de easing estáticas. Se activa mayormente
    al actualizar el Compose BOM y `material3`; revisa el
    changelog antes de fijar tokens de diseño custom encima.
-   **Foldables / Postura del dispositivo**: `WindowInfoTracker` expone
    los *hinges* (bisagras) de un plegable para evitar colocar
    contenido crítico justo sobre el pliegue.
