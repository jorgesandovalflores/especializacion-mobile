# Alcance Técnico — Proyectos Finales
### Curso: Desarrollo Móvil Nativo para Android (2025-1)

---

## Proyecto 1 — Evently Mobile (Gestión de eventos)

### Visión
App móvil en Android/Compose para **descubrir, ver y suscribirse** a eventos. Recibe **actualizaciones en tiempo real** (mock WebSocket) y **notificaciones** (FCM opcional).

---

### Alcance funcional

| Nivel | Funcionalidades principales |
|-------|------------------------------|
| **Normal (mínimo)** | - Listado y detalle de eventos<br>- Suscripción/Desuscripción (estado local)<br>- Autenticación simple (mock/Firebase opcional)<br>- Persistencia local con Room (cache-first)<br>- Estados UI (`loading/success/error`) y renderizado condicional<br>- Manejo de errores tipado (sealed class/Result)<br>- Navegación con Navigation Compose<br>- Pruebas unitarias básicas de ViewModel<br>- Mock API con JSON o MockWebServer |
| **Avanzado (superior)** | - WebSocket simulado (Socket.IO mock)<br>- Notificaciones FCM (topics por categoría)<br>- Cache avanzada con reglas de expiración<br>- Métricas y logs estructurados<br>- Pruebas de UI con Compose Test<br>- Deep Links a detalle de evento |

---

### Arquitectura móvil
- **Presentación:** Jetpack Compose + Navigation; ViewModel con **StateFlow** y patrón **UiState**.  
- **Dominio:** UseCases puros (Kotlin) con flujos y validaciones.  
- **Datos:** Repository que combina **Remote (Retrofit mock)** + **Local (Room)**; mapeadores DTO→Model.  
- **DI:** Hilt (modules, scopes).  
- **Segundo plano:** WorkManager opcional para refrescos.  

---

### API y Persistencia
- **Mock API:** `GET /events`, `GET /events/{id}`, `POST /events/{id}/subscribe`.  
- **Room:** tablas `event` y `subscription`, DAOs reactivos (Flow).  
- Estrategia **cache-first**: DB local + refresco remoto.  

---

### Realtime y Notificaciones
- **Socket.IO client** (mock): `event-updated`, `event-cancelled`.  
- **FCM opcional:** notificaciones con deep link al detalle.  

---

### Calidad y Publicación
- **Unit tests:** ViewModel y UseCases.  
- **UI tests:** Compose Test (avanzado).  
- **Entrega:** APK/.aab firmado y documentado.  

---

## Proyecto 2 — TaxiGo Mobile (Solicitudes de taxi con tracking básico)

### Visión
App móvil con dos perfiles (flavors o módulos): **Pasajero** (solicita viaje) y **Conductor** (acepta y completa). Incluye **ubicación en tiempo real**, permisos y almacenamiento local.

---

### Alcance funcional

| Nivel | Funcionalidades principales |
|-------|------------------------------|
| **Normal (mínimo)** | - Autenticación simple (mock/Firebase opcional)<br>- Flujo de viaje (`requested → accepted → on_ride → completed`)<br>- Solicitar viaje (pasajero) / Aceptar e iniciar (conductor)<br>- Mapa con Google Maps, marcadores y polilínea mínima<br>- Permisos de ubicación (foreground, fallback de última conocida)<br>- Persistencia local (Room)<br>- Estados UI coherentes y manejo de errores<br>- Mock API local o MockWebServer |
| **Avanzado (superior)** | - Simulación de GPS del conductor (timer local)<br>- Socket.IO simulado para `travel-update`/`position-update`<br>- Notificaciones FCM por cambio de estado<br>- WorkManager para envíos periódicos<br>- Testing de UI (Compose Test)<br>- Deep Links a detalle de viaje |

---

### Arquitectura móvil
- **Presentación:** Compose + Navigation (Passenger/Driver).  
- **Dominio:** UseCases (`request`, `accept`, `start`, `complete`, `cancel`).  
- **Datos:** Repository combinando **Retrofit mock** + **Room**.  
- **DI:** Hilt.  
- **Segundo plano:** WorkManager para “position heartbeat”.  

---

### API y Persistencia
- **Mock API:** `POST /travels/request`, `/accept`, `/start`, `/complete`, `/cancel`, `GET /me/travels`.  
- **Room:** tablas `travel`, `location_snapshot`, `user_profile`.  
- Caché de viajes activos e historial.  

---

### Ubicación y Realtime
- **FusedLocationProviderClient** con validación de precisión y fallback.  
- **Socket.IO client (mock)** para actualizaciones en tiempo real.  

---

### Notificaciones y Calidad
- **FCM opcional:** “Tu conductor llegó”, “Viaje completado”.  
- **Tests:** unitarios y UI (Compose Test).  
- **Entrega:** APK/.aab firmado y documentado.  

---

## Entregables comunes
- Código fuente Android/Kotlin con **MVVM, Hilt, UseCases, Repositorios**.  
- **Mocks reproducibles**: JSON, MockWebServer o Socket.IO simulado.  
- **README** con pasos de ejecución y configuración.  
- **Suite de pruebas** mínima (unitaria, UI en avanzado).  
- **APK/.aab** final firmado.  

---

## Criterios de Evaluación
| Criterio | Peso |
|-----------|-------|
| Funcionalidades mínimas | 40% |
| Calidad técnica (arquitectura, estados, DI) | 30% |
| Pruebas y estabilidad | 20% |
| Plus avanzado (realtime, FCM, WorkManager) | 10% |

---
