# Infra App Taxi — MySQL, Redis, API y backoffice con Docker Compose

Levanta en tu máquina todo lo que necesita la app [`../android-app-taxi`](../android-app-taxi/README.md) para iniciar sesión, mantenerla con el refresh token, cargar el menú y completar el registro: la **API** de [`../backend-app-taxi`](../backend-app-taxi/README.md), su base de datos **MySQL** y **Redis**. Desde el módulo 5 · clase 04 la misma API atiende la app del conductor ([`../android-driver-app-taxi`](../android-driver-app-taxi/README.md)) y el tiempo real por Socket.IO, y hay un cuarto archivo opcional para servir el **backoffice** ([`../backoffice-app-taxi`](../backoffice-app-taxi/README.md)) con nginx. Cada servicio tiene su propio archivo Compose y todos comparten una red y un `.env`.

---

## 1. Qué se levanta y cómo se conecta

![Topología](docs/gif/topology.gif)

| Servicio      | Contenedor       | Imagen                                           | Puerto en tu máquina → contenedor | Datos                        | Para qué                                              |
| ------------- | ---------------- | ------------------------------------------------ | --------------------------------- | ---------------------------- | ----------------------------------------------------- |
| API           | `http-app-taxi`  | `${BACKEND_IMAGE}:${BACKEND_TAG}` (se construye) | `3001 → 3001`                     | —                            | Endpoints `/auth/*`, `/menu/active/:application` y `/passenger/signup` |
| Base de datos | `mysql-app-taxi` | `mysql:9.7`                                      | `3306 → 3306`                     | volumen `mysql_volume`       | Pasajeros, códigos OTP y opciones de menú             |
| Caché         | `redis-app-taxi` | `redis:8.10-alpine`                              | `6379 → 6379`                     | volumen `redis_volume` (AOF) | Lock de reenvío, intentos de OTP, caché del menú, conductores en labores, ubicaciones y recorridos |
| Backoffice (opcional) | `backoffice-app-taxi` | `backoffice-app-taxi:0.0.1` (se construye) | `8080 → 80` | — | Panel web en Vue servido por nginx |

| Conexión            | Cómo funciona                                                                                                                        |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| Apps Android y backoffice → API | REST y Socket.IO (`/realtime`) por el mismo puerto 3001. En el emulador con Android 17 o superior usa `adb reverse tcp:3001 tcp:3001` y `http://127.0.0.1:3001/`: las apps con `targetSdk 37` no llegan a `10.0.2.2` sin el permiso de red local                    |
| API → MySQL / Redis | Dentro de la red `network-app-taxi`, **por nombre de contenedor** (`mysql-app-taxi:3306`, `redis-app-taxi:6379`), no por `localhost` |
| API → proveedor SMS | Sale a Internet por HTTPS (Brevo o LabsMobile)                                                                                       |
| API → OSRM          | Sale a Internet por HTTPS (`router.project-osrm.org`) para calcular rutas, salvo que `HTTP_OSRM_URL` apunte a un OSRM propio           |
| Tú → MySQL / Redis  | Por los puertos 3306 y 6379, con un cliente MySQL o `redis-cli`. La API no los usa                                                   |
| Datos               | Viven en volúmenes: sobreviven a `docker compose down` y a recrear los contenedores                                                  |

---

## 2. Archivos y variables

```
infra-app-taxi/
├── docker-compose.mysql.yml   # MySQL 9.7 + healthcheck + volumen
├── docker-compose.redis.yml   # Redis 8.10 con appendonly (AOF) + volumen
├── docker-compose.http.yml    # API: construye ../backend-app-taxi y le pasa las variables
├── docker-compose.backoffice.yml   # Opcional: construye ../backoffice-app-taxi y lo sirve con nginx
├── .env                       # Variables locales (no se versiona)
└── README.md
```

![Del .env al contenedor](docs/gif/env-variables.gif)

Compose reemplaza cada `${VARIABLE}` con el `.env` **de esta carpeta** antes de crear el contenedor. Por eso los comandos se ejecutan siempre desde aquí. La API recibe las variables con el nombre que espera NestJS: `HTTP_JWT_ACCESS_SECRET` del `.env` llega como `JWT_ACCESS_SECRET`, y `MYSQL_CONTAINER_NAME` llega como `DB_HOST`.

```env
PROJECT_NAME="app-taxi"
NETWORK="network-app-taxi"
TZ="America/Lima"

# MySQL
MYSQL_CONTAINER_NAME="mysql-app-taxi"
MYSQL_ROOT_PASSWORD="root"
MYSQL_DATABASE="db_app_taxi"
MYSQL_VOLUME="mysql_volume"

# Redis
REDIS_CONTAINER_NAME="redis-app-taxi"
REDIS_PORT="6379"
REDIS_VOLUME="redis_volume"

# API
BACKEND_IMAGE="http-app-taxi"
BACKEND_TAG="0.0.1"
HTTP_CONTAINER_NAME="http-app-taxi"
HTTP_DOCKER_PLATFORM="linux/amd64"
HTTP_NODE_ENV="development"
HTTP_NODE_DEBUG="false"
HTTP_DB_POOL="10"
HTTP_APPLICATION_PORT="3001"
HTTP_JWT_ACCESS_TTL_SEC="900"
HTTP_JWT_REFRESH_TTL_SEC="2592000"
HTTP_JWT_ACCESS_SECRET="cambia-esto"
HTTP_JWT_REFRESH_SECRET="cambia-esto-tambien"

# OTP (opcionales; valores por defecto)
HTTP_OTP_TTL_SEC="120"
HTTP_OTP_RATE_TTL_SEC="60"
HTTP_OTP_MAX_ATTEMPTS="5"

# Menú (opcional): segundos que la API guarda el menú en Redis
HTTP_MENU_CACHE_TTL_SEC="60"

# SMS: "brevo" (por defecto) o "labsmobile". Sin credenciales = el código se escribe en el log
HTTP_SMS_PROVIDER="brevo"
HTTP_BREVO_API_KEY=""
HTTP_BREVO_TEXT_SMS="Tu código de App Taxi es:"
HTTP_BREVO_SENDER="AppTaxi"
HTTP_LABSMOBILE_USER=""
HTTP_LABSMOBILE_API_KEY=""
HTTP_LABSMOBILE_TEXT_SMS="Tu código de App Taxi es:"
HTTP_LABSMOBILE_SENDER="AppTaxi"

# Firebase (opcional): cuenta de servicio. Sin ella = el push se escribe en el log y la foto de perfil responde 503
HTTP_FIREBASE_PROJECT_ID=""
HTTP_FIREBASE_CLIENT_EMAIL=""
HTTP_FIREBASE_PRIVATE_KEY=""
HTTP_FIREBASE_STORAGE_BUCKET=""      # vacío = <project-id>.firebasestorage.app

# Viajes y tiempo real (opcionales; si faltan se usan estos valores)
HTTP_OSRM_URL="https://router.project-osrm.org"
HTTP_DRIVER_SEARCH_RADIUS_KM="10"
HTTP_DRIVER_LOCATION_STALE_SEC="120"
HTTP_TRIP_REQUEST_TTL_SEC="600"
HTTP_ADMIN_JWT_TTL_SEC="28800"
HTTP_CORS_ORIGINS=""                 # vacío = cualquier origen
HTTP_SEED_ADMIN_PASSWORD="Admin123*"
HTTP_SEED_DRIVER_PASSWORD="Driver123*"

# Backoffice (opcional)
BACKOFFICE_PORT="8080"
BACKOFFICE_API_URL="http://localhost:3001"   # URL de la API vista desde el navegador
```

| Variable                            | La usa         | Qué hace                                                                                                      |
| ----------------------------------- | -------------- | ------------------------------------------------------------------------------------------------------------- |
| `NETWORK`                           | los 3 archivos | Nombre de la red externa (debe existir antes de levantar)                                                     |
| `MYSQL_*`                           | mysql, http    | Nombre del contenedor, contraseña de `root`, base de datos y volumen                                          |
| `REDIS_*`                           | redis, http    | Nombre del contenedor, puerto publicado y volumen                                                             |
| `BACKEND_IMAGE`, `BACKEND_TAG`      | http           | Nombre y tag de la imagen que se construye y ejecuta                                                          |
| `HTTP_DOCKER_PLATFORM`              | http           | `linux/amd64` (funciona en Apple Silicon por emulación) o `linux/arm64` (nativo, más rápido en Apple Silicon) |
| `HTTP_JWT_*`                        | http           | Secretos y duración de los tokens. Con `HTTP_JWT_ACCESS_TTL_SEC="20"` el access token vence en 20 s y se puede ver el refresh en clase |
| `HTTP_OTP_*`                        | http           | Vigencia del código, espera entre envíos e intentos permitidos (120 / 60 / 5 si faltan)                       |
| `HTTP_MENU_CACHE_TTL_SEC`           | http           | Tiempo de vida de la caché del menú en Redis (60 s si falta)                                                  |
| `HTTP_SMS_PROVIDER`                 | http           | `brevo` o `labsmobile`; otro valor detiene el arranque de la API                                              |
| `HTTP_BREVO_*`, `HTTP_LABSMOBILE_*` | http           | Credenciales del proveedor activo. Sin ellas y fuera de `production`, el código OTP se escribe en el log      |
| `HTTP_FIREBASE_*`                   | http           | Cuenta de servicio de Firebase (`project_id`, `client_email`, `private_key` del JSON). Sin ellas y fuera de `production`, el push se escribe en el log y `POST /passenger/storage-token` responde 503. `HTTP_FIREBASE_STORAGE_BUCKET` es opcional |
| `HTTP_OSRM_URL`, `HTTP_DRIVER_*`, `HTTP_TRIP_REQUEST_TTL_SEC` | http | Servidor de rutas, radio de búsqueda de conductores, antigüedad máxima de su ubicación y ventana de solicitudes abiertas |
| `HTTP_ADMIN_JWT_TTL_SEC`, `HTTP_CORS_ORIGINS`, `HTTP_SEED_*` | http | Duración del token del backoffice, orígenes permitidos y contraseñas del seed (admin `admin@apptaxi.pe`, conductores `51900000001` y `51900000002`) |
| `BACKOFFICE_PORT`, `BACKOFFICE_API_URL` | backoffice | Puerto publicado y URL de la API que se incrusta en el build de Vite |
| `HTTP_NODE_DEBUG`                   | http           | Déjala en `"false"`: con `"true"` la imagen no arranca (`pino-pretty` no está en la imagen final)             |

> El `.env` tiene credenciales: no se sube a git. Si `HTTP_*_API_KEY` tiene una clave real, pedir una OTP **envía un SMS de verdad**.

---

## 3. Imagen de la API

![Construcción de la imagen](docs/gif/image-build.gif)

`docker-compose.http.yml` construye la imagen con el `Dockerfile` de `../backend-app-taxi`:

| Etapa        | Qué hace                                                                                          | Pasa a la imagen final |
| ------------ | ------------------------------------------------------------------------------------------------- | ---------------------- |
| 1 · `base`   | `node:24-alpine` + pnpm 12.5.1 con corepack                                                       | —                      |
| 2 · `deps`   | `pnpm install --frozen-lockfile` con dependencias de desarrollo                                   | —                      |
| 3 · `build`  | `nest build`: compila TypeScript a `dist/`                                                        | `dist/`                |
| 4 · `pruned` | `pnpm prune --prod`: deja solo dependencias de producción                                         | `node_modules/`        |
| 5 · `runner` | Imagen limpia con `dist/`, `node_modules/`, `entrypoint.sh` y `netcat`; corre como usuario `node` | Es la imagen final     |

La imagen final no lleva TypeScript, el código fuente ni dependencias de desarrollo.

---

## 4. Levantar el stack

![Orden de arranque](docs/gif/startup.gif)

**Requisitos:** Docker Engine 24 o superior y Docker Compose v2 (`docker compose`). En Windows y macOS vienen con [Docker Desktop](https://www.docker.com/products/docker-desktop/); en Linux instala `docker-ce` y `docker-compose-plugin` desde el [repositorio oficial](https://docs.docker.com/engine/install/). Comprueba con `docker --version` y `docker compose version`.

Ejecuta todo **desde esta carpeta**:

```bash
# 0) Una sola vez: la red que comparten los tres archivos
docker network create network-app-taxi

# 1) MySQL: espera a que el healthcheck marque "healthy"
docker compose -f docker-compose.mysql.yml -p app-taxi up -d
docker compose -f docker-compose.mysql.yml -p app-taxi ps

# 2) Redis
docker compose -f docker-compose.redis.yml -p app-taxi up -d

# 3) API: construye la imagen y arranca
docker compose -f docker-compose.http.yml -p app-taxi up -d --build
docker compose -f docker-compose.http.yml -p app-taxi logs -f http
```

Opcional, el backoffice (queda en `http://localhost:8080`):

```bash
docker compose -f docker-compose.backoffice.yml -p app-taxi up -d --build
```

Al arrancar, el `entrypoint.sh` de la API espera a MySQL (`nc -z`), aplica las migraciones, carga los seeders y recién entonces levanta Nest. En el log verás `Migrations OK`, `Seeders OK` y `Nest application successfully started`.

Los tres archivos comparten el proyecto `-p app-taxi`, así que Compose avisa `Found orphan containers` al levantar el segundo y el tercero. Es normal: **no** uses `--remove-orphans`, porque borraría los otros servicios.

**Probar:**

```bash
# Documentación de la API
open http://localhost:3001/api/docs

# Pedir un código (si el teléfono no existe se crea como pasajero nuevo)
curl -s -X POST http://localhost:3001/auth/otp-generate \
  -H 'content-type: application/json' -d '{"phone":"51987654321"}'

# Sin credenciales de SMS, el código aparece en el log
docker compose -f docker-compose.http.yml -p app-taxi logs http | grep "OTP para"

# Validar el código
curl -s -X POST http://localhost:3001/auth/otp-validate \
  -H 'content-type: application/json' -d '{"phone":"51987654321","code":"<codigo>"}'

# Renovar la sesión con el refreshToken que devuelve otp-validate
curl -s -X POST http://localhost:3001/auth/refresh \
  -H 'content-type: application/json' -d '{"refreshToken":"<refreshToken>"}'

# Menú (requiere el accessToken que devuelve otp-validate)
curl -s http://localhost:3001/menu/active/PASSENGER -H "Authorization: Bearer <accessToken>"

# Completar el registro de un pasajero nuevo
curl -s -X PUT http://localhost:3001/passenger/signup \
  -H 'content-type: application/json' -H "Authorization: Bearer <accessToken>" \
  -d '{"givenName":"Ana","familyName":"Perez","email":"ana@example.com"}'
```

**Probar viajes** (sin apps; los tokens salen del login de cada usuario):

```bash
# Login de un conductor y del admin del seed
curl -s -X POST http://localhost:3001/driver/auth/login \
  -H 'content-type: application/json' -d '{"phone":"51900000001","password":"Driver123*"}'
curl -s -X POST http://localhost:3001/admin/auth/login \
  -H 'content-type: application/json' -d '{"email":"admin@apptaxi.pe","password":"Admin123*"}'

# Cotizar un viaje con el accessToken del pasajero (ruta de OSRM + tarifa)
curl -s -X POST http://localhost:3001/trips/quote -H "Authorization: Bearer <accessToken>" \
  -H 'content-type: application/json' \
  -d '{"origin":{"lat":-12.0464,"lng":-77.0428},"destination":{"lat":-12.1211,"lng":-77.0297}}'
```

**Apagar** (los volúmenes se conservan):

```bash
docker compose -f docker-compose.backoffice.yml -p app-taxi down
docker compose -f docker-compose.http.yml -p app-taxi down
docker compose -f docker-compose.redis.yml -p app-taxi down
docker compose -f docker-compose.mysql.yml -p app-taxi down
```

---

## 5. Actualizar un stack existente de la clase anterior

![Actualizar sin perder datos](docs/gif/update-stack.gif)

Si ya levantaste la infra del módulo 5 (clases 01 a 03), del módulo 4 (clases 01 a 04) o del módulo 3 · clase 04, **no borres nada**. Todas las clases usan el mismo proyecto (`-p app-taxi`), los mismos nombres de contenedor, los mismos volúmenes y la misma red. Al ejecutar Compose desde **esta** carpeta, cada contenedor se recrea con la definición de esta clase y monta los mismos datos.

```bash
cd native-android/modulo-05/clase-04/infra-app-taxi

docker compose -f docker-compose.mysql.yml -p app-taxi up -d
docker compose -f docker-compose.mysql.yml -p app-taxi ps      # espera "healthy"
docker compose -f docker-compose.redis.yml -p app-taxi up -d
docker compose -f docker-compose.http.yml -p app-taxi build
docker compose -f docker-compose.http.yml -p app-taxi up -d
docker logs -f http-app-taxi
```

| Estado de la base de datos        | Migraciones que se aplican al arrancar la API                               |
| --------------------------------- | --------------------------------------------------------------------------- |
| Viene del módulo 5 · clases 01 a 03, o del módulo 4 · clase 03 o 04 | Solo `Schema1793664000000` (`Executed: 1`): administradores, conductores, tarifa, viajes y ofertas; el seeder carga el admin, dos conductores y la tarifa |
| Viene del módulo 4 · clase 01 o 02, o del módulo 3 | `entity_passenger_device` y la de viajes (`Executed: 2`) |
| Viene del módulo 2 · clase 04     | `entity_menu`, `entity_passenger_device` y la de viajes (`Executed: 3`); pasajeros y OTP se conservan |
| Nueva o reiniciada                | Las cinco (`Executed: 5`)                                                   |

- `build` usa `BACKEND_IMAGE:BACKEND_TAG`. Con el mismo tag de la clase anterior, la imagen anterior se reemplaza; para conservar ambas, cambia `BACKEND_TAG` antes de construir.
- **Volver al módulo 5 · clase 03:** la API anterior ignora las tablas nuevas, así que basta con levantarla desde `../../clase-03/infra-app-taxi` con `up -d --build`. Para quitar las tablas, ejecuta `pnpm migration:revert` desde el backend de esta clase antes de cambiar.

### Enviar un push de prueba

Con la app en Home (ya registró su token), envía un push al pasajero por su teléfono:

```bash
docker exec http-app-taxi node dist/features/notifications/cli/send-push.js 51987654321
docker exec http-app-taxi node dist/features/notifications/cli/send-push.js 51987654321 app-taxi://passenger/menu
```

Sin `HTTP_FIREBASE_*`, el log muestra `[DEV] Push a 1 dispositivo(s): …` y no llega nada al teléfono.

**Comprobar sin enviar SMS reales:**

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:3001/api/docs      # 200
curl -s -X POST http://localhost:3001/auth/otp-validate \
  -H 'content-type: application/json' -d '{"phone":"51900000001","code":"1234"}'
# {"status_code":422,"message":"No existe un pasajero registrado con este número de teléfono."}
curl -s http://localhost:3001/menu/active/PASSENGER
# {"status_code":401,"message":"Tu sesión expiró. Inicia sesión nuevamente.","errors":[]}
docker exec -it mysql-app-taxi mysql -uroot -p db_app_taxi \
  -e "SELECT name FROM migrations_history; SHOW TABLES;"
```

### OSRM propio (opcional)

Por defecto la API usa el servidor público de OSRM, pensado para pruebas y con límite de uso. Para no depender de Internet, prepara un OSRM con el mapa de Perú siguiendo la [guía oficial de osrm-backend](https://github.com/Project-OSRM/osrm-backend#using-docker): descarga `peru-latest.osm.pbf` de Geofabrik (unos 250 MB), procésalo con `osrm-extract`, `osrm-partition` y `osrm-customize` (necesita varios GB de RAM y algunos minutos) y sírvelo con `osrm-routed --algorithm mld`. Luego apunta la API a él dentro de la red del proyecto, por ejemplo `HTTP_OSRM_URL="http://osrm-app-taxi:5000"`. Este paso no se automatiza en esta carpeta porque el procesamiento es pesado para una clase.
