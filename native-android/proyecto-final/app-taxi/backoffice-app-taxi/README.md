# Backoffice · App Taxi

Panel web de administración del curso **App Taxi** (Android, módulo 05, clase 04). Desde aquí un administrador sigue en tiempo real a los conductores y los viajes, gestiona las cuentas de administradores, pasajeros y conductores, y configura la tarifa con la que el backend estima el precio de cada viaje.

El backoffice es un cliente más del mismo backend que usan las apps del pasajero y del conductor: consume la API REST bajo `/admin/*` con un token de tipo `admin` y se conecta por Socket.IO al namespace `/realtime`, donde el servidor lo une a la sala `admins`.

## Stack

- Vue 3.5 con `<script setup>` y TypeScript.
- Vite 8 como servidor de desarrollo y empaquetador; `vue-tsc` para el chequeo de tipos.
- vue-router 5 (rutas protegidas con un guard) y Pinia 4 (sesión, toasts y estado de la conexión en tiempo real).
- axios para REST y socket.io-client 4.8 para los eventos en vivo.
- Leaflet 1.9 con teselas de OpenStreetMap, sin API key.
- CSS propio con variables (la misma paleta de la app Android: morado `#7843E6`, cian `#00FFFF`, superficies claras y modo oscuro opcional). Sin librerías de componentes.

La polyline de las rutas (formato Google, precisión 5, la que devuelve OSRM) se decodifica con una función propia en `src/utils/polyline.ts`, sin dependencias extra.

## Cómo correrlo en local

Requisitos: Node 24 y pnpm.

```bash
pnpm install
pnpm dev
```

La app queda en `http://localhost:5173`. Por defecto apunta al backend en `http://localhost:3001`; para cambiarlo crea un archivo `.env.local` (no se versiona) a partir de `.env.example`:

```bash
VITE_API_URL=http://localhost:3001
```

`VITE_API_URL` se usa para la API REST y para el socket, que se conecta a `${VITE_API_URL}/realtime` enviando el token en `auth: { token }`. Como la variable se resuelve en tiempo de compilación, si la cambias hay que reiniciar `pnpm dev` o volver a compilar.

Otros comandos:

```bash
pnpm build     # vue-tsc -b && vite build → dist/
pnpm preview   # sirve dist/ para probar el build
```

El backend ya permite CORS (`app.enableCors` en `main.ts`); para restringir los orígenes usa `HTTP_CORS_ORIGINS` en el `.env` de `infra-app-taxi` (por ejemplo `http://localhost:5173,http://localhost:8080`).

### Credenciales del seed

```
admin@apptaxi.pe / Admin123*
```

## Con Docker

El `Dockerfile` es multi-etapa: compila con `node:24-alpine` (pnpm activado con corepack) y sirve el contenido de `dist` con `nginx:alpine`. `nginx.conf` hace el fallback de la SPA a `index.html` para que funcionen las rutas del history mode.

```bash
docker build --build-arg VITE_API_URL=http://localhost:3001 -t backoffice-app-taxi .
docker run --rm -p 8080:80 backoffice-app-taxi
```

Luego abre `http://localhost:8080`. La URL del API queda fija en la imagen, así que se construye una imagen por entorno.

## Estructura de carpetas

```
backoffice-app-taxi/
├── Dockerfile, nginx.conf, .dockerignore, .env.example
├── index.html
├── public/favicon.svg
└── src/
    ├── main.ts                 arranque: Pinia, router, manejo global del 401
    ├── App.vue                 RouterView + contenedor de toasts
    ├── api/                    axios (token, idioma, errores) y endpoints /admin/*
    ├── realtime/socket.ts      conexión Socket.IO y suscripción tipada a eventos
    ├── stores/                 auth, toasts y estado de la conexión en vivo
    ├── composables/            mapa Leaflet, listas paginadas, eventos, tema, reloj
    ├── router/index.ts         rutas y guard de autenticación
    ├── layouts/AppLayout.vue   barra lateral, conexión del socket y cierre de sesión
    ├── components/             modal, confirmación, paginación, estados, badges, íconos
    ├── views/                  una vista por pantalla
    ├── utils/                  polyline, formatos (S/, km, fechas), íconos del mapa, sesión
    ├── types/models.ts         tipos del contrato (DTOs, estados, Page<T>)
    ├── styles/main.css         variables de color, componentes base y marcadores
    └── assets/feature_splash_logo.png
```

## Pantallas

| Ruta | Pantalla | REST | Socket.IO |
| --- | --- | --- | --- |
| `/login` | Ingreso con correo y contraseña | `POST /admin/auth/login` | — |
| (layout) | Barra lateral, perfil y cierre de sesión | `GET /admin/me` | conecta a `/realtime` |
| `/map/drivers` | Mapa de conductores: libre, ocupado o fuera de labores; panel con búsqueda y filtro | `GET /admin/drivers/locations` | `driver:location`, `driver:availability`, `trip:updated` |
| `/map/trips` | Mapa de viajes activos: origen, destino, ruta y conductor en vivo | `GET /admin/trips/active`, `GET /admin/drivers/locations` | `trip:updated`, `driver:location` |
| `/trips` | Historial con filtro por estado y paginación | `GET /admin/trips?status=&page=&size=` | `trip:updated` (refresca filas visibles) |
| `/trips/:id` | Detalle: ruta planificada, recorrido real, conductor en vivo, línea de tiempo, datos y ofertas | `GET /admin/trips/:id` | `driver:location`, `trip:updated` |
| `/drivers` | CRUD de conductores con vehículo, estado y columna "En labores" | `GET/POST /admin/drivers`, `PUT/DELETE /admin/drivers/:id` | `driver:availability` |
| `/passengers` | Pasajeros con búsqueda y paginación (solo lectura) | `GET /admin/passengers?search=&page=&size=` | — |
| `/admins` | CRUD de administradores; no permite eliminar ni desactivar la propia cuenta | `GET/POST /admin/admins`, `PUT/DELETE /admin/admins/:id` | — |
| `/tariff` | Tarifa base y precio por km, con calculadora de vista previa | `GET/PUT /admin/tariff` | — |

Todas las pantallas muestran estados de carga, vacío y error (con reintento) y avisan con toasts el resultado de cada acción.

## Cómo funciona la sesión

El token del login se guarda en `localStorage` junto con los datos del administrador. Un interceptor de axios lo agrega como `Authorization: Bearer` y envía `Accept-Language: es`. Cuando cualquier petición responde 401, se borra la sesión, se cierra el socket y se vuelve al login conservando la ruta en `?redirect=`. El token de admin dura 8 horas y no tiene refresh, así que al vencer hay que volver a ingresar.

Los errores del backend llegan como `{ status_code, message, errors? }`; el toast muestra `message` y, si vienen `errors` de validación, cada mensaje se coloca debajo de su campo en el formulario.

## Tiempo real

Al entrar al layout se abre un único socket hacia `${VITE_API_URL}/realtime` (transporte websocket, reconexión automática). Cada vista se suscribe solo a los eventos que necesita mientras está montada:

- `driver:location` mueve el marcador del conductor y su rumbo; en el detalle del viaje además alarga la línea del recorrido real.
- `driver:availability` cambia el estado "en labores" en el mapa y en la tabla de conductores.
- `trip:updated` agrega, actualiza o retira viajes del mapa de viajes, marca a un conductor como ocupado o libre y refresca el detalle abierto.

El indicador de la barra lateral muestra si la conexión está activa, reconectando o caída.
