# Alcance de los proyectos finales

### Curso: Desarrollo Móvil Nativo para Android

---

## Contenido

1. Cómo usar este documento
2. Resumen: dos proyectos, dos niveles
3. Requisitos comunes (todos los proyectos)
4. Proyecto 1 · Evently Mobile (eventos)
5. Proyecto 2 · TaxiGo Mobile (taxi con ubicación)
6. Entrega y presentación
7. Criterios de evaluación

---

## 1) Cómo usar este documento

1. **Elige un proyecto**: Evently (eventos) o TaxiGo (taxi).
2. **Elige un nivel**: **Básico** o **Avanzado**. El Avanzado incluye **todo** el Básico más sus propios requisitos.
3. **Cumple los requisitos comunes** (sección 3): aplican a cualquier proyecto y nivel.
4. **Usa los códigos** (`EB-01`, `TA-03`, …) en tu README para indicar qué requisito cumple cada parte de tu app.

| Nivel        | Para quién                                                         | Resultado esperado                                         |
| ------------ | ------------------------------------------------------------------ | ---------------------------------------------------------- |
| **Básico**   | Quien quiere consolidar lo visto en clase                          | App completa con login, API, base de datos local y estados |
| **Avanzado** | Quien ya domina el básico y quiere sumar tiempo real y segundo plano | Lo anterior + WebSocket, notificaciones, WorkManager y pruebas de UI |

> El nivel Básico es **obligatorio** para aprobar. Los requisitos Avanzados solo cuentan si el Básico está completo.

---

## 2) Resumen: dos proyectos, dos niveles

| | **Evently · Básico** | **Evently · Avanzado** | **TaxiGo · Básico** | **TaxiGo · Avanzado** |
| --- | --- | --- | --- | --- |
| **Idea** | Ver eventos y suscribirse | + cambios en vivo y recordatorios | Pasajero ve su ubicación y pide un viaje | + conductor en vivo sobre el mapa |
| **Login** | Correo y contraseña | Igual | OTP por teléfono | Igual |
| **API** | REST | REST + Socket.IO | REST | REST + Socket.IO |
| **Base local** | Room (eventos y suscripciones) | + expiración de caché | Room (viajes) | + historial de posiciones |
| **Dispositivo** | — | Notificaciones | GPS y mapa | GPS, mapa y notificaciones |
| **Segundo plano** | — | WorkManager | — | WorkManager |
| **Pruebas** | Unitarias | + UI (Compose Test) | Unitarias | + UI (Compose Test) |
| **Backend** | [`backend-evently`](./proyecto-final/app-events/backend-evently) | Igual | [`backend-app-taxi`](./proyecto-final/app-taxi/backend-app-taxi) | Igual |

---

## 3) Requisitos comunes (todos los proyectos)

Aplican a los cuatro casos. Son los requisitos mínimos del documento *Presentación de proyecto* del curso.

### 3.1 Tecnología

| Área            | Obligatorio                                                   |
| --------------- | ------------------------------------------------------------- |
| Lenguaje y UI   | Kotlin + Jetpack Compose (Material 3)                         |
| Arquitectura    | MVVM + capas `presentation`, `domain`, `data`                 |
| Estado          | `ViewModel` con `StateFlow` y estados en una `sealed interface` |
| Inyección       | Hilt                                                          |
| Asincronía      | Coroutines + Flow                                             |
| Red             | Retrofit + OkHttp                                             |
| Base local      | Room                                                          |
| Navegación      | Navigation Compose                                            |
| Versión mínima  | `minSdk` 29 o superior                                        |

### 3.2 Comportamiento

| Código | Requisito                                                                                         |
| ------ | ------------------------------------------------------------------------------------------------- |
| `C-01` | Cada pantalla que llama a la API muestra **carga, éxito y error**; el error tiene un mensaje claro y la opción de reintentar |
| `C-02` | Sin conexión, la app **no se cierra**: muestra lo guardado en Room o un mensaje de «sin conexión» |
| `C-03` | Los errores de red se traducen a mensajes para el usuario (nunca se muestra «HTTP 500» o una excepción) |
| `C-04` | Navegación clara: **lista → detalle → volver**, y el botón atrás funciona en todas las pantallas  |
| `C-05` | Al girar el teléfono no se pierde el estado ni se repiten peticiones                             |

### 3.3 Seguridad

| Código | Requisito                                                                                         |
| ------ | ------------------------------------------------------------------------------------------------- |
| `S-01` | Los tokens se guardan **cifrados** (Android Keystore + SharedPreferences o DataStore)             |
| `S-02` | El access token se envía con un interceptor; si vence, se renueva con el refresh token una sola vez |
| `S-03` | «Cerrar sesión» borra tokens y datos locales                                                      |
| `S-04` | Ninguna clave (Maps, Firebase, etc.) ni `.env` se sube al repositorio: van en `local.properties` o variables de entorno |
| `S-05` | Los permisos (ubicación, notificaciones) se piden **en el momento en que se necesitan**, con una explicación previa |

### 3.4 Calidad y documentación

| Código | Requisito                                                                                         |
| ------ | ------------------------------------------------------------------------------------------------- |
| `Q-01` | Pruebas unitarias de al menos **2 casos de uso** y **1 ViewModel**, usando fakes                   |
| `Q-02` | README con: objetivo, nivel elegido, requisitos cumplidos (con sus códigos), cómo ejecutar backend y app, dependencias principales y capturas o GIFs |
| `Q-03` | Repositorio Git con commits descriptivos (no un único commit final)                                |
| `Q-04` | APK de *debug* instalable; el nivel Avanzado entrega además un **.aab firmado**                    |

---

## 4) Proyecto 1 · Evently Mobile (eventos)

### 4.1 Visión

Una app para **descubrir eventos, ver su detalle y suscribirse**. Funciona sin conexión mostrando lo último que se descargó. En el nivel avanzado, la app se entera **en vivo** cuando un evento cambia o se cancela, y recuerda al usuario sus eventos.

### 4.2 Backend disponible

Proyecto: [`proyecto-final/app-events/backend-evently`](./proyecto-final/app-events/backend-evently) (NestJS). Infra en `infra-evently`.

| Método | Ruta                          | Uso en la app                          | Requiere token |
| ------ | ----------------------------- | -------------------------------------- | -------------- |
| POST   | `/auth/register`              | Crear cuenta                           | No             |
| POST   | `/auth/login`                 | Iniciar sesión                         | No             |
| POST   | `/auth/refresh-token`         | Renovar la sesión                      | Refresh        |
| POST   | `/auth/logout`                | Cerrar sesión                          | Sí             |
| GET    | `/events`                     | Listado de eventos públicos            | No             |
| GET    | `/events/{id}`                | Detalle de un evento                   | No             |
| POST   | `/events/{id}/subscribe`      | Suscribirse a un evento                | Sí             |
| GET    | `/me/subscriptions`           | Mis suscripciones                      | Sí             |
| WS     | `/notifications/ws` (Socket.IO) | Eventos `event-updated` y `event-cancelled` de los eventos suscritos | Sí |

### 4.3 Pantallas

| Pantalla             | Básico | Avanzado |
| -------------------- | :----: | :------: |
| Splash               | ✓      | ✓        |
| Login y registro     | ✓      | ✓        |
| Listado de eventos   | ✓      | ✓ (+ búsqueda y filtro) |
| Detalle de evento    | ✓      | ✓ (+ cambios en vivo) |
| Mis suscripciones    | ✓      | ✓        |
| Ajustes              | —      | ✓ (tema, recordatorios) |

### 4.4 Nivel Básico

| Código  | Requisito                                                                                                  | Se cumple cuando…                                                         |
| ------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `EB-01` | Registro e inicio de sesión con correo y contraseña                                                        | Validación local (correo válido, contraseña ≥ 8) y errores del backend en pantalla |
| `EB-02` | Splash decide el destino según haya sesión guardada                                                        | Con sesión abre el listado; sin sesión, el login                          |
| `EB-03` | Listado de eventos con **Room como fuente única** (*cache-first*)                                          | La lista guardada aparece al instante y se actualiza al llegar la red    |
| `EB-04` | Detalle de evento (título, fecha, lugar, descripción, cupos)                                               | Se abre desde el listado con el `id` como argumento de navegación         |
| `EB-05` | Botón «Suscribirme» en el detalle                                                                          | Muestra carga, confirma con un mensaje y cambia a «Suscrito»              |
| `EB-06` | Pantalla «Mis suscripciones»                                                                               | Lista desde `GET /me/subscriptions` y se guarda en Room                   |
| `EB-07` | Estados de UI en listado, detalle y suscripción (`C-01`)                                                   | Hay skeleton o spinner, mensaje de error y «Reintentar»                   |
| `EB-08` | Cerrar sesión (`S-03`)                                                                                     | Vuelve al login y la base local queda vacía                              |
| `EB-09` | Pruebas unitarias (`Q-01`)                                                                                 | Casos de uso de listado y suscripción + ViewModel del listado             |

**Room mínimo:** tablas `event` y `subscription`; los DAO devuelven `Flow`.

### 4.5 Nivel Avanzado

Incluye todo el Básico más:

| Código  | Requisito                                                                                                  | Se cumple cuando…                                                         |
| ------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `EA-01` | Conexión Socket.IO a `/notifications/ws` con el token                                                      | Al recibir `event-updated` se vuelve a pedir el evento y Room se actualiza solo |
| `EA-02` | Evento cancelado en vivo                                                                                   | Con `event-cancelled`, el detalle muestra «Cancelado» sin recargar a mano |
| `EA-03` | La conexión sigue el ciclo de vida                                                                         | Se abre con la app visible y se cierra en segundo plano                  |
| `EA-04` | Notificación local cuando un evento suscrito cambia o se cancela                                           | Canal propio, permiso `POST_NOTIFICATIONS` pedido con explicación         |
| `EA-05` | Tocar la notificación abre el detalle (**deep link**)                                                      | Funciona con la app cerrada y abierta                                    |
| `EA-06` | Recordatorio 1 hora antes de cada evento suscrito                                                          | Programado con WorkManager; se puede desactivar en Ajustes               |
| `EA-07` | Sincronización periódica con **WorkManager** (cada 6 h, solo con red)                                      | `CoroutineWorker` con restricción de red que refresca eventos y suscripciones |
| `EA-08` | Caché con expiración                                                                                       | Si los datos tienen más de 15 min, se muestra «Actualizado hace X min» y se refresca |
| `EA-09` | Búsqueda por texto y filtro por fecha en el listado                                                        | Se filtra sobre Room, sin pedir de nuevo a la API                         |
| `EA-10` | Tema claro/oscuro persistente (DataStore)                                                                  | La elección se mantiene al reiniciar la app                              |
| `EA-11` | Pruebas de UI con Compose Test                                                                             | Al menos login con error y listado → detalle                              |
| `EA-12` | `.aab` firmado (`Q-04`)                                                                                    | Generado con keystore propio; las claves no están en el repositorio      |

### 4.6 Fuera de alcance

Crear o editar eventos (rol organizador), pagos y chat.

---

## 5) Proyecto 2 · TaxiGo Mobile (taxi con ubicación)

### 5.1 Visión

La app del **pasajero**: inicia sesión con su teléfono, ve su ubicación en el mapa, elige un destino y solicita un viaje. En el nivel avanzado ve al **conductor acercarse en vivo** sobre el mapa y recibe avisos del viaje. El conductor lo simula el backend: no se construye una app de conductor.

> Es el mismo proyecto que construimos en clase (`android-app-taxi`). El Básico parte de lo visto hasta el módulo 4 · clase 01.

### 5.2 Backend disponible

Proyecto: [`proyecto-final/app-taxi/backend-app-taxi`](./proyecto-final/app-taxi/backend-app-taxi) (NestJS). Infra en `infra-app-taxi`.

| Método | Ruta                               | Uso en la app                                   | Requiere token |
| ------ | ---------------------------------- | ----------------------------------------------- | -------------- |
| POST   | `/auth/otp-generate`               | Enviar código al teléfono                       | No             |
| POST   | `/auth/otp-validate`               | Validar el código e iniciar sesión              | No             |
| POST   | `/auth/refresh`                    | Renovar la sesión                               | Refresh        |
| PUT    | `/passenger/signup`                | Completar el registro (nombres y correo)        | Sí             |
| GET    | `/menu/active/PASSENGER`           | Opciones del menú                               | Sí             |
| GET    | `/address/search`                  | Buscar una dirección de destino                 | Sí             |
| GET    | `/address/details`                 | Coordenadas de la dirección elegida             | Sí             |
| POST   | `/simulation/start/{id}`           | Iniciar la ruta simulada del conductor          | —              |
| DELETE | `/simulation/stop/{id}`            | Detener la simulación                           | —              |
| WS     | namespace `events` (Socket.IO)     | `join_passengers_main` y evento `route` con `latitude`, `longitude`, `bearing`, `progress` | Sí |

Los viajes **no** tienen endpoints en el backend: se guardan en Room en el teléfono.

### 5.3 Pantallas

| Pantalla                      | Básico | Avanzado |
| ----------------------------- | :----: | :------: |
| Splash                        | ✓      | ✓        |
| Login OTP (teléfono y código) | ✓      | ✓        |
| Registro                      | ✓      | ✓        |
| Home con mapa                 | ✓      | ✓ (+ conductor en vivo) |
| Elegir destino                | ✓      | ✓        |
| Confirmar viaje               | ✓      | ✓        |
| Viaje en curso                | ✓ (estados manuales) | ✓ (tracking en vivo) |
| Historial y detalle de viaje  | ✓      | ✓ (+ recorrido en el mapa) |

### 5.4 Nivel Básico

| Código  | Requisito                                                                                                  | Se cumple cuando…                                                         |
| ------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `TB-01` | Login por OTP y registro de pasajero nuevo                                                                 | Flujo teléfono → código → Home o Registro, con errores del backend        |
| `TB-02` | Splash decide el destino según la sesión y el registro pendiente                                           | Sin sesión → login; registro pendiente → registro; resto → Home           |
| `TB-03` | Home con **Google Maps** y la ubicación del pasajero                                                       | Marcador y círculo de precisión; la cámara se centra en el primer fix     |
| `TB-04` | Permiso de ubicación en tiempo de ejecución (`S-05`)                                                       | Panel explicativo, precisa o aproximada, y «Abrir ajustes» si está bloqueado |
| `TB-05` | Errores de ubicación con salida                                                                            | GPS apagado, sin señal (15 s) y sin permiso tienen mensaje y botón         |
| `TB-06` | El GPS se apaga en segundo plano                                                                           | `callbackFlow` + `awaitClose` + `WhileSubscribed`                         |
| `TB-07` | Elegir destino con búsqueda de direcciones                                                                 | `GET /address/search` mientras se escribe (con *debounce*) y `GET /address/details` al elegir |
| `TB-08` | Confirmar viaje: origen, destino y línea recta entre ambos en el mapa                                      | Se guarda un viaje en Room con estado `REQUESTED`                         |
| `TB-09` | Estados del viaje: `REQUESTED → ACCEPTED → ON_RIDE → COMPLETED` (o `CANCELLED`)                            | Botones de prueba avanzan el estado; la pantalla cambia con cada uno      |
| `TB-10` | Historial de viajes (lista → detalle)                                                                      | Lee de Room con `Flow`; el detalle muestra origen, destino, fecha y estado |
| `TB-11` | Pruebas unitarias (`Q-01`)                                                                                 | Casos de uso de ubicación y de cambio de estado del viaje + ViewModel de Home |

**Room mínimo:** tablas `travel` y `menu`; los DAO devuelven `Flow`.

### 5.5 Nivel Avanzado

Incluye todo el Básico más:

| Código  | Requisito                                                                                                  | Se cumple cuando…                                                         |
| ------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `TA-01` | Conexión Socket.IO al namespace `events` con el token y `join_passengers_main`                             | Se conecta al confirmar el viaje y se desconecta al terminarlo           |
| `TA-02` | Conductor en vivo                                                                                          | Al recibir `route`, el marcador del auto se mueve y gira según `bearing`  |
| `TA-03` | El viaje avanza solo con la simulación                                                                     | `progress` pasa el viaje a `ACCEPTED`, `ON_RIDE` y `COMPLETED`            |
| `TA-04` | Recorrido del conductor dibujado como **polilínea**                                                        | Se guardan las posiciones en Room (`location_snapshot`) y se ven en el detalle del viaje |
| `TA-05` | Notificaciones locales del viaje                                                                           | «Tu conductor está en camino», «Tu conductor llegó», «Viaje completado»  |
| `TA-06` | Tocar la notificación abre el viaje en curso (**deep link**)                                               | Funciona con la app cerrada y abierta                                    |
| `TA-07` | Reconexión                                                                                                 | Si se pierde la red durante el viaje, se reconecta y muestra «Reconectando…» |
| `TA-08` | **WorkManager**: envía al backend los viajes guardados que no se sincronizaron (o los registra en un log si no hay endpoint) | `CoroutineWorker` con restricción de red y reintento con *backoff*        |
| `TA-09` | Sensor: agitar el teléfono recentra el mapa                                                                | Acelerómetro con `callbackFlow`, registrado solo con Home visible        |
| `TA-10` | Animaciones                                                                                                | Panel de viaje con `AnimatedVisibility` y movimiento suave del marcador   |
| `TA-11` | Pruebas de UI con Compose Test                                                                             | Al menos login con código inválido y confirmación de viaje               |
| `TA-12` | `.aab` firmado (`Q-04`)                                                                                    | Generado con keystore propio; las claves no están en el repositorio      |

### 5.6 Fuera de alcance

App del conductor, cálculo de tarifas, pagos y ubicación en segundo plano (`ACCESS_BACKGROUND_LOCATION`).

---

## 6) Entrega y presentación

### 6.1 Qué entregar

| Entregable           | Básico                 | Avanzado                       |
| -------------------- | ---------------------- | ------------------------------ |
| Repositorio Git      | ✓                      | ✓                              |
| README (`Q-02`)      | ✓                      | ✓                              |
| APK de *debug*       | ✓                      | ✓                              |
| `.aab` firmado       | —                      | ✓                              |
| Capturas o GIFs      | ✓ (flujo principal)    | ✓ (incluye el tiempo real)     |

### 6.2 Presentación (5 a 10 minutos por Zoom)

1. **Objetivo** del proyecto y nivel elegido (1 diapositiva opcional).
2. **Demo en vivo** del flujo principal:
    - Evently: login → listado → detalle → suscribirse → mis suscripciones → sin conexión.
    - TaxiGo: login OTP → mapa con ubicación → destino → confirmar → estados del viaje → historial.
3. **Avanzado**: mostrar el tiempo real (cambio de evento o conductor en movimiento) y una notificación.
4. **Una decisión técnica** explicada (por ejemplo, por qué Room es la fuente única o cómo se apaga el GPS).

---

## 7) Criterios de evaluación

| Criterio                                          | Peso | Qué se revisa                                                              |
| ------------------------------------------------- | ---- | -------------------------------------------------------------------------- |
| Funcionalidad y requisitos técnicos               | 40 % | Requisitos del nivel elegido (`EB`/`EA` o `TB`/`TA`) y comunes `C-01` a `C-05` |
| Calidad del código y buenas prácticas             | 20 % | Capas, estados en `sealed interface`, Hilt, manejo de errores, pruebas `Q-01` |
| Seguridad, autenticación y documentación          | 20 % | `S-01` a `S-05`, README `Q-02`, ninguna clave en el repositorio            |
| Presentación y explicación                        | 20 % | Demo fluida, claridad en las decisiones técnicas                           |

Cómo se aplica el nivel:

-   Cada criterio se evalúa contra la lista del **nivel elegido**.
-   Un proyecto Avanzado con el Básico incompleto se evalúa como Básico.
-   Los requisitos Avanzados cumplidos a medias suman, pero no reemplazan a los Básicos que falten.
