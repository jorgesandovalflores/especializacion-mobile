# Backend AppTaxi — API de OTP, sesión, menú, registro, push, foto de perfil, viajes en tiempo real y backoffice

API NestJS que implementa el **inicio de sesión por OTP** (código de 4 dígitos por SMS), la **renovación de sesión**, el **menú** y el **registro** del pasajero que consume la app [`../android-app-taxi`](../android-app-taxi/README.md). Desde el módulo 5 · clase 04 también atiende **viajes en tiempo real** (app del pasajero y [`../android-driver-app-taxi`](../android-driver-app-taxi/README.md)) con un gateway de **Socket.IO**, rutas de **OSRM** y una tarifa configurable, y la API `/admin/*` del [`../backoffice-app-taxi`](../backoffice-app-taxi/README.md):

1. `POST /auth/otp-generate` → crea el pasajero si no existe, genera la OTP y la envía por SMS (Brevo o LabsMobile).
2. `POST /auth/otp-validate` → valida la OTP y devuelve `accessToken`, `refreshToken` y el perfil del pasajero.
3. `POST /auth/refresh` → cambia un refresh token vigente por un access token y un refresh token nuevos.
4. `GET /menu/active/:application` → lista las opciones de menú activas. Requiere `Authorization: Bearer <accessToken>`.
5. `PUT /passenger/signup` → completa el perfil del pasajero nuevo y lo deja `ACTIVE`. Requiere `accessToken`.
6. `PUT /passenger/device` → guarda el token de Firebase Cloud Messaging (FCM) del teléfono. Requiere `accessToken`.
7. `send-push` (CLI) → envía una notificación push a todos los dispositivos de un pasajero con `firebase-admin`.
8. `GET /passenger/me` → perfil del pasajero autenticado.
9. `POST /passenger/storage-token` → custom token de Firebase para que la app suba su foto a **Firebase Storage**.
10. `PUT /passenger/photo` → guarda la URL de la foto ya subida (solo si es la ruta del propio pasajero).
11. `POST /trips/quote` y `POST /trips` → ruta con OSRM, tarifa estimada y solicitud del viaje a los conductores cercanos ([8](#8-feature-trips--viajes-y-ofertas)).
12. `/driver/*` → login del conductor, inicio y fin de labores, ofertas y estados del viaje ([10](#10-feature-drivers--conductores)).
13. `/realtime` (Socket.IO) → eventos `trip:*` y `driver:*` hacia pasajeros, conductores y administradores ([9](#9-gateway-socketio--realtime)).
14. `/admin/*` → login de administradores, CRUD de administradores y conductores, pasajeros, viajes, ubicaciones y tarifa ([11](#11-backoffice-admins-tariff-y-admin)).

---

## 1. Stack técnico y arquitectura

### Stack técnico

| Área | Tecnología | Versión | Para qué se usa |
| --- | --- | --- | --- |
| Runtime | Node.js | 24 LTS (mínimo `^22.22.3`) | Ejecución; `fetch` nativo para el SMS |
| Paquetes | pnpm | 12.5.1 (`packageManager`) | Siempre pnpm, nunca npm ni yarn |
| Framework | NestJS (`common`, `core`, `platform-express`) | 12.0 | Módulos, inyección de dependencias, HTTP |
| Lenguaje | TypeScript | 6.0 | Tipado estático |
| Validación | `class-validator` + `class-transformer` | 0.15 / 0.5 | DTOs de entrada con `ValidationPipe` |
| Base de datos | TypeORM + `@nestjs/typeorm` + `mysql2` | 1.1 / 12 / 3.24 | Entidades, DAOs y migraciones sobre MySQL 9.7 |
| Caché | `redis` (cliente) | 6.2 | Lock de reenvío e intentos de OTP en Redis 8.10 |
| Seguridad | `@nestjs/jwt` + `crypto.randomInt` | 12 / nativo | Tokens JWT y OTP con CSPRNG |
| Configuración | `@nestjs/config` + `dotenv` | 12 / 18 | Variables de `.env` vía `ConfigService` |
| Idiomas | `nestjs-i18n` | 10.8 | Mensajes ES/EN según `Accept-Language` |
| Logs | `nestjs-pino` + `pino` | 5.2 / 10 | Logs JSON con `x-request-id` |
| Documentación | `@nestjs/swagger` | 12 | OpenAPI en `/api/docs` |
| Firebase | `firebase-admin` (`app`, `messaging`, `auth`) | 14.5 | Push FCM (HTTP v1) y custom tokens para Firebase Storage, con una cuenta de servicio |
| Tiempo real | `@nestjs/websockets` + `@nestjs/platform-socket.io` + `socket.io` | 12.0.3 / 12.0.3 / 4.8 | Gateway `/realtime` con salas por usuario. Fijadas en 12.0.3: la 12.3 exige `@nestjs/common` 12.3 |
| Contraseñas | `argon2` | 0.45 | Hash `argon2id` de conductores y administradores |
| Rutas | OSRM (HTTP, `fetch` nativo) | API v5 | Distancia, duración y polyline de cada viaje; por defecto `https://router.project-osrm.org` |
| Pruebas | Jest + `ts-jest` (`socket.io-client` para pruebas manuales) | 30 / 29 | Pruebas unitarias con dependencias simuladas |

### Arquitectura en capas

![Arquitectura en capas](docs/gif/architecture.gif)

| Capa | Piezas | Responsabilidad | No debe |
| --- | --- | --- | --- |
| **Presentación** | `AuthController`, `MenuController`, `PassengerController`, `PassengerTripController`, `DriverTripController`, `Admin*Controller`, `RealtimeGateway`, DTOs, `ValidationPipe`, `AccessTokenGuard` y sus variantes por tipo de usuario | HTTP, eventos de Socket.IO, validación de entrada, autenticación, Swagger, idioma | Tener reglas de negocio |
| **Aplicación** | `AuthService`, `MenuService`, `PassengerService`, `PassengerTripService`, `DriverTripService`, `DriverLocationService`, `DriverService`, `AdminService`, `TariffService` | Reglas de OTP, viajes, ofertas, estados, tarifa, presencia del conductor, emisión de JWT | Escribir SQL ni llamar APIs directamente |
| **Datos** | `PassengerDao`, `PassengerOtpDao`, `MenuDao`, entidades | Consultas TypeORM | Decidir reglas de negocio |
| **Infraestructura** | `CacheService`, `SmsSender`, `JwtService`, `FirebaseAdminService`, `PushSender`, `RoutePlanner` (`OsrmRoutePlanner`), `RealtimeEmitter`, `TripNotifier` | Redis, proveedor SMS, firma de tokens, Firebase, OSRM, emisión de eventos | Conocer HTTP |

Nest resuelve todas las dependencias por constructor (inyección de dependencias). Los errores de **cualquier** capa terminan en `HttpExceptionFilter`, que responde siempre con el mismo formato (ver [3.1](#31-corehttp--errores-con-un-solo-formato)).

---

## 2. Organización del código: core, commons y features

![core, commons y features](docs/gif/modules.gif)

| Carpeta | Qué va aquí | Ejemplos |
| --- | --- | --- |
| **`core/`** | Infraestructura transversal que usa toda la API | Filtro de errores, base de datos, Redis, i18n, runners de migraciones, `firebase/FirebaseAdminService` |
| **`commons/`** | Utilidades sin reglas de negocio | `utils/UtilDate` |
| **`features/`** | Un módulo Nest por dominio, con todas sus capas | `passengers/`, `menu/`, `notifications/`, `drivers/`, `admins/`, `tariff/`, `routing/`, `trips/`, `realtime/` |

`AppModule` arma la infraestructura global (`ConfigModule`, `LoggerModule`, `I18nModule`, `TypeOrmModule`, `APP_FILTER`) e importa cada feature. Las features usan `core` y `commons`; **`core` nunca importa una feature**.

```
src/
├─ app.module.ts                 # Config, Pino, i18n, TypeORM, PassengerModule, MenuModule, NotificationModule, APP_FILTER
├─ main.ts                       # ValidationPipe, filtros globales, Swagger
├─ commons/utils/UtilDate.ts
├─ core/
│  ├─ cache/cache.service.ts     # Cliente Redis (get / set con TTL / del)
│  ├─ cli/                       # run-migrations.ts, run-seeders.ts
│  ├─ firebase/                  # FirebaseAdminService: credenciales, bucket y custom tokens
│  ├─ database/                  # DataSource, migrations/, seeders/
│  ├─ http/                      # HttpCustomException, HttpExceptionFilter, guard/AccessTokenGuard (+ spec)
│  └─ i18n/{es,en}/              # auth.json, otp.json, passenger.json, common.json, signup.json, device.json, photo.json
├─ features/passengers/
   ├─ passenger.module.ts
   ├─ controllers/               # AuthController (/auth/otp-*, /auth/refresh), PassengerController (signup, me, storage-token, photo)
   ├─ services/                  # AuthService (+ spec), PassengerService (+ spec), StorageTokenService (+ spec)
   ├─ utils/passenger-avatar.ts  # Ruta del avatar y validación de su URL (+ spec)
   ├─ dao/                       # PassengerDao, PassengerOtpDao
   ├─ entities/                  # PassengerEntity, PassengerOtpEntity
   ├─ dto/                       # Requests y responses (Swagger + class-validator)
   ├─ remote/                    # SmsSender, Brevo, LabsMobile, sms-sender.provider (+ spec)
   ├─ mapper/                    # Entity → DTO (+ spec)
   └─ enum/                      # PassengerStatus: ACTIVE, INACTIVE_REGISTER, SUSPENDED
└─ features/menu/
   ├─ menu.module.ts
   ├─ controllers/               # MenuController (GET /menu/active/:application)
   ├─ services/                  # MenuService (+ spec): Redis primero, MySQL después
   ├─ dao/ · entities/           # MenuDao, MenuEntity (entity_menu)
   ├─ dto/ · mapper/             # MenuDto, toMenuDto (+ spec)
   └─ enum/                      # MenuApplication, MenuStatus
└─ features/notifications/
   ├─ notification.module.ts
   ├─ controllers/               # DeviceController (PUT /passenger/device)
   ├─ services/                  # NotificationService (+ spec): registrar, enviar y limpiar tokens
   ├─ dao/ · entities/           # PassengerDeviceDao, PassengerDeviceEntity (entity_passenger_device)
   ├─ remote/                    # PushSender (+ spec), FcmPushService (firebase-admin)
   ├─ cli/send-push.ts           # Push de prueba por número de teléfono
   └─ dto/ · enum/               # PassengerDeviceRequestDto, DevicePlatform
└─ features/drivers/             # DriverModule: entity_driver, login, availability, CRUD admin, DriverPresenceService (Redis)
└─ features/admins/              # AdminModule: entity_admin, login, CRUD de administradores
└─ features/tariff/              # TariffModule: entity_tariff, calculateFare (+ spec), GET/PUT /admin/tariff
└─ features/routing/             # RoutingModule: RoutePlanner (contrato) y OsrmRoutePlanner (+ spec)
└─ features/trips/               # TripModule: entity_trip, entity_trip_offer, servicios de pasajero, conductor y admin (+ specs),
│                                #   TripNotifier, TripTrackService, DriverLocationService, testing/trip-fixtures.ts
└─ features/realtime/            # RealtimeModule: RealtimeGateway (namespace /realtime)
core/realtime/                   # RealtimeEmitter (emite a salas sin conocer las features) y nombres de eventos y salas
core/http/guard/                 # AccessTokenGuard + PassengerAccessGuard, DriverAccessGuard, AdminAccessGuard
core/http/dto/page.dto.ts        # PageQueryDto (search, page, size) y PageDto<T>
```

---

## 3. Core

### 3.1 `core/http` — errores con un solo formato

![Cuatro orígenes de error, un formato](docs/gif/core-errors.gif)

| Archivo | Qué hace |
| --- | --- |
| `exception/http.exception.ts` | `HttpCustomException(message, status = 422)`: error de negocio con texto ya traducido |
| `filters/http-exception.filter.ts` | `@Catch()` global: formatea **toda** excepción como `{status_code, message, errors}`. Traduce los errores de validación con i18n y, ante un error no controlado, registra el stack y responde 500 genérico |
| `guard/access-token.guard.ts` | `AccessTokenGuard`: exige `Authorization: Bearer <accessToken>`, verifica firma y expiración, rechaza refresh tokens y deja el payload en `request.user` |
| `main.ts` | `ValidationPipe({whitelist, forbidNonWhitelisted, transform})` y registro de filtros |

| Origen | Código | Cuerpo |
| --- | --- | --- |
| DTO inválido (`ValidationPipe`) | 400 | `{"status_code":400,"message":"Bad Request","errors":[{"field":"general","message":"…"}]}` |
| `AccessTokenGuard` (sin token, inválido o expirado) | 401 | `{"status_code":401,"message":"Tu sesión expiró. Inicia sesión nuevamente.","errors":[]}` |
| `HttpCustomException` de un service | 422 / 429 | `{"status_code":422,"message":"Ya existe un código activo…"}` |
| Error no controlado (MySQL o Redis caídos, bug) | 500 | `{"status_code":500,"message":"Ocurrió un error inesperado en el servidor.","errors":[]}` |

Librerías: `@nestjs/common` (excepciones y filtros), `class-validator` y `class-transformer` (DTOs), `nestjs-i18n` (mensajes).

### 3.2 `core/i18n` — mensajes en español e inglés

| Archivo | Claves | Quién las usa |
| --- | --- | --- |
| `auth.json` | `activeCodeExists`, `deliveryFailed`, `invalidOrExpired`, `alreadyUsed`, `tooManyAttempts`, `passenger.notExists`, `unauthorized`, `invalidRefreshToken`, `refreshTokenRequired` | `AuthService`, `AccessTokenGuard`, DTO de refresh |
| `signup.json` | `validation.givenName`, `validation.familyName`, `validation.email`, `emailAlreadyUsed` | DTO de registro y `PassengerService` |
| `passenger.json` | `validation.*` del teléfono | DTOs |
| `otp.json` | `validation.*` del código | DTOs |
| `common.json` | `internalError` | `HttpExceptionFilter` (500) |

`I18nModule` usa `AcceptLanguageResolver` con `fallbackLanguage: "es"`. La app Android no envía `Accept-Language`, así que siempre recibe español.

### 3.3 `core/database` y `core/cli` — MySQL y migraciones

![Arranque y migraciones](docs/gif/core-database.gif)

| Migración | Qué hace |
| --- | --- |
| `Schema1759303438196` | La misma de la clase 03: crea `entity_passenger` |
| `Schema1759768881969` | Incremental: agrega `INACTIVE_REGISTER` al enum `status` y crea `entity_passenger_otp` (FK con `ON DELETE CASCADE`) |
| `Schema1760371904959` | Incremental (módulo 3 · clase 01): crea `entity_menu` con índices únicos por `key` y por `(application, order)` |
| `Schema1792540800000` | Incremental (módulo 4 · clase 03): crea `entity_passenger_device` con token único y FK a `entity_passenger` (`ON DELETE CASCADE`) |
| `Schema1793664000000` | Incremental (módulo 5 · clase 04): crea `entity_admin`, `entity_driver`, `entity_tariff`, `entity_trip` y `entity_trip_offer` (una oferta por conductor y viaje) |

Una BD nueva ejecuta las cinco; una BD que viene de la clase anterior solo ejecuta la última y conserva sus datos. Para actualizar un stack Docker sigue [«Actualizar un stack existente de la clase anterior»](../infra-app-taxi/README.md#5-actualizar-un-stack-existente-de-la-clase-anterior).

| Archivo | Qué hace |
| --- | --- |
| `database/typeorm.config.ts` | `DataSource` de la app (sin `synchronize`) |
| `database/typeorm.migration.ts` | `DataSource` del CLI; historial en `migrations_history` |
| `database/seeders/` | `passengers.json`, `menus.json` y `drivers.json` + seeders idempotentes. `backoffice-seed.ts` crea el admin `admin@apptaxi.pe` (`SEED_ADMIN_PASSWORD`, por defecto `Admin123*`), los conductores `51900000001` y `51900000002` (`SEED_DRIVER_PASSWORD`, por defecto `Driver123*`) y la tarifa inicial (S/ 5.00 + S/ 1.50 por km) |
| `cli/run-migrations.ts`, `cli/run-seeders.ts` | Runners que usa `entrypoint.sh` en Docker |

Librerías: `typeorm` 1.1, `@nestjs/typeorm` 12, `mysql2` 3.24.

### 3.4 `core/cache` — Redis

`CacheService` envuelve el cliente `redis` 6 con `get`, `set` (con TTL en segundos) y `del`, y desde esta clase también hashes, sets y listas (`hSet`, `hGetAll`, `sAdd`, `sMembers`, `rPush`, `lRange`, `expire`) para la presencia de conductores y el recorrido de los viajes. `CacheModule` es global. Usa `disableOfflineQueue` y `connectTimeout` de 5 s: si Redis cae, la petición falla al instante con 500 en lugar de quedar colgada. Las claves de OTP están en [5.4](#54-redis-en-el-tiempo) y la del menú en [6.1](#61-get-menuactiveapplication).

### 3.5 Configuración, logs y documentación

| Pieza | Librería | Detalle |
| --- | --- | --- |
| Variables de entorno | `@nestjs/config` | `ConfigModule.forRoot({isGlobal: true})`; se leen con `ConfigService` al usarlas |
| Logs | `nestjs-pino` | JSON con `x-request-id`; con `NODE_DEBUG=true`, `pino-pretty` (solo local) |
| Swagger | `@nestjs/swagger` | `/api/docs` (UI) y `/api/docs-json` (OpenAPI) |

---

## 4. Commons

| Archivo | Qué hace |
| --- | --- |
| `utils/UtilDate.ts` | Fecha actual en formato MySQL y timestamp |

Hoy es mínimo a propósito: algo pasa a `commons` recién cuando lo necesitan dos features y no tiene reglas de negocio. Sin librerías propias.

---

## 5. Feature `passengers` — autenticación por OTP y registro

| Capa | Archivos |
| --- | --- |
| Presentación | `controllers/auth.controller.ts`, `controllers/passenger.controller.ts`, `dto/` |
| Aplicación | `services/auth.service.ts`, `services/passenger.service.ts` |
| Datos | `dao/passenger.dao.ts`, `dao/passenger-otp.dao.ts`, `entities/`, `mapper/` |
| Infraestructura | `remote/` (SMS), `CacheService` (core), `JwtService` |
| Módulo | `passenger.module.ts`: `TypeOrmModule.forFeature`, `JwtModule`, proveedores y `smsSenderProvider` |

### 5.1 `POST /auth/otp-generate`

![Flujo de otp-generate](docs/gif/otp-generate.gif)

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Nest | `ValidationPipe`: `phone` de 11 dígitos | **400** |
| 2 | MySQL | Busca el pasajero; si no existe, lo crea con `INACTIVE_REGISTER` | — |
| 3 | Redis | ¿Existe `otp:passenger:{id}:lock`? | **422** `activeCodeExists` |
| 4 | MySQL | Genera 4 dígitos con `crypto.randomInt` y guarda la OTP con `expiresAt` | — |
| 5 | SMS | `SmsSender.sendOtp(phone, code)` | **422** `deliveryFailed` |
| 6 | Redis | Lock por `OTP_RATE_TTL_SEC` (60 s) y borra los intentos | — |
| ✓ | — | **200** `{success, expiresAt, ttlSec, messageId}` | — |

### 5.2 `POST /auth/otp-validate`

![Flujo de otp-validate](docs/gif/otp-validate.gif)

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Nest | `ValidationPipe`: `phone` de 11 dígitos y `code` de 4 a 6 | **400** |
| 2 | MySQL | Busca el pasajero | **422** `passenger.notExists` |
| 3 | Redis | ¿Intentos fallidos ≥ `OTP_MAX_ATTEMPTS`? | **429** `tooManyAttempts` |
| 4 | MySQL | OTP con ese código, `used = false` y no expirada | **422** `invalidOrExpired` y suma un intento |
| 5 | MySQL | `markAsUsed` y `touchLastLoginAt` | **422** `alreadyUsed` |
| 6 | JWT | Firma `accessToken` y `refreshToken` | — |
| 7 | Redis | Borra lock e intentos | — |
| ✓ | — | **200** `{accessToken, refreshToken, user}` | — |

Payload de los JWT: `sub` (id), `typ: "passenger"`, `phone`, `iat`, `exp` (y `rt: true` en el refresh).

### 5.3 Envío de SMS (Brevo o LabsMobile)

![Envío de SMS con patrón Strategy](docs/gif/sms-strategy.gif)

`AuthService` depende del contrato `SmsSender` (patrón **Strategy**). `SMS_PROVIDER` decide al arrancar qué implementación inyecta Nest.

| Pieza | Archivo (`remote/`) | Responsabilidad |
| --- | --- | --- |
| `SmsSender` (clase abstracta) | `sms-sender.ts` | Contrato y lógica común: modo desarrollo, *timeout* de 5 s, log y captura de errores |
| `BrevoSmsService` | `brevo-sms.service.ts` | API de Brevo |
| `LabsMobileSmsService` | `labsmobile-sms.service.ts` | API de LabsMobile |
| `smsSenderProvider` | `sms-sender.provider.ts` | Elige la implementación; un valor desconocido **detiene el arranque** |

| Proveedor | `brevo` (por defecto) | `labsmobile` |
| --- | --- | --- |
| Endpoint | `POST https://api.brevo.com/v3/transactionalSMS/sms` | `POST https://api.labsmobile.com/json/send` |
| Autenticación | Header `api-key: BREVO_API_KEY` | `Authorization: Basic base64(LABSMOBILE_USER:LABSMOBILE_API_KEY)` |
| Body | `{type, sender, recipient: "+51…", content, tag}` | `{message, tpoa, recipient: [{msisdn: "51…"}]}` |
| Éxito | HTTP 2xx con `messageId` | HTTP 2xx con `code: "0"` y `subid` |

| Credenciales del proveedor activo | `NODE_ENV` | Resultado |
| --- | --- | --- |
| Completas | cualquiera | Envía el SMS. Si el proveedor rechaza o vence el *timeout* → **422** `deliveryFailed` |
| Incompletas | ≠ `production` | **Modo desarrollo**: escribe `[DEV] OTP para +51987654321: 1234` en el log y responde 200 |
| Incompletas | `production` | No envía → **422** `deliveryFailed` |

Para agregar un proveedor: una clase que extienda `SmsSender`, registrada en `passenger.module.ts` y agregada a `SMS_PROVIDERS` y al `switch` de `sms-sender.provider.ts`. `AuthService` no cambia.

### 5.4 Redis en el tiempo

![Vigencia, lock e intentos](docs/gif/otp-timeline.gif)

| Clave | Valor | TTL | Se crea | Se borra |
| --- | --- | --- | --- | --- |
| `otp:passenger:{id}:lock` | `"1"` | `OTP_RATE_TTL_SEC` (60 s) | Tras enviar el SMS | Al validar con éxito o al vencer |
| `otp:passenger:{id}:attempts` | contador | `OTP_TTL_SEC` (120 s) | Al fallar una validación | Al generar una OTP nueva o validar con éxito |

La vigencia de la OTP (`OTP_TTL_SEC`) vive en MySQL (`expires_at`), no en Redis.

### 5.5 `PUT /passenger/signup` — completar el registro

Un pasajero nuevo queda en `INACTIVE_REGISTER` después de validar su OTP. Este endpoint guarda su perfil y lo activa. Está protegido con `AccessTokenGuard` (ver [6](#6-feature-menu--opciones-de-menú-por-aplicación)).

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Guard | Valida el `accessToken`; el id del pasajero sale de `sub` (nunca del body) | **401** `auth.unauthorized` |
| 2 | Nest | `ValidationPipe`: nombres y apellidos de 2 a 60 caracteres, correo válido (se guarda en minúsculas) | **400** `signup.validation.*` |
| 3 | MySQL | Busca al pasajero del token | **401** `auth.unauthorized` |
| 4 | MySQL | ¿Otro pasajero usa ese correo? | **422** `signup.emailAlreadyUsed` |
| 5 | MySQL | Guarda `given_name`, `family_name`, `email` y `status = ACTIVE` | **422** si el índice único detecta un duplicado en paralelo |
| ✓ | — | **200** con el pasajero actualizado (`PassengerDto`) | — |

Los tokens no se reemiten: su payload (`sub`, `typ`, `phone`) no cambia con el registro.

### 5.6 `POST /auth/refresh` — renovar la sesión

![Flujo de refresh](docs/gif/auth-refresh.gif)

El access token dura poco (`JWT_ACCESS_TTL_SEC`, 15 min). Para no pedir una OTP cada vez, la app cambia su refresh token (30 días) por una sesión nueva. Es un endpoint público: no lleva `Authorization`.

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Nest | `ValidationPipe`: `refreshToken` obligatorio | **400** `auth.refreshTokenRequired` |
| 2 | JWT | Verifica firma y expiración con `JWT_REFRESH_SECRET` | **401** `auth.invalidRefreshToken` |
| 3 | JWT | El payload debe traer `rt: true` y `typ: "passenger"` (un access token no sirve) | **401** `auth.invalidRefreshToken` |
| 4 | MySQL | El pasajero de `sub` debe existir | **401** `auth.invalidRefreshToken` |
| 5 | JWT | Firma un access token y un refresh token nuevos (rotación) | — |
| ✓ | — | **200** `{accessToken, refreshToken, user}` | — |

`otp-validate` y `refresh` comparten la emisión de tokens (`AuthService.issueSession`). El refresh token anterior sigue siendo válido hasta que expira: no hay lista de revocación.

### 5.7 Foto de perfil con Firebase Storage

![Elegir, autorizar, subir y guardar la foto](../android-app-taxi/docs/gif/storage-photo-flow.gif)

La app **sube el archivo directo a Firebase Storage**; el backend no recibe bytes. Su trabajo es autorizar y guardar la URL final.

| Endpoint | Qué hace | Respuestas |
| --- | --- | --- |
| `GET /passenger/me` | Perfil del pasajero del token | **200** `PassengerDto` · **401** |
| `POST /passenger/storage-token` | `FirebaseAdminService.createCustomToken(passengerId, {role: "passenger"})` y la ruta `passengers/{id}/avatar.jpg` | **200** `{token, bucket, photoPath}` · **401** · **503** `photo.notConfigured` sin `FIREBASE_*` |
| `PUT /passenger/photo` | Valida que `photoUrl` sea `https://firebasestorage.googleapis.com/…/o/passengers%2F{id}%2Favatar.jpg` del **mismo** pasajero y la guarda en `photo_url` | **200** `PassengerDto` · **400** `photo.validation.url` · **422** `photo.invalidUrl` |

**¿Por qué un custom token?** La app inicia sesión con nuestro OTP + JWT, no con Firebase Auth. Las reglas de Storage solo conocen `request.auth` de Firebase, así que el backend firma un token cuyo `uid` es el id del pasajero y la app lo canjea con `signInWithCustomToken`. Las reglas viven versionadas en [`firebase/storage.rules`](firebase/storage.rules):

```
match /passengers/{passengerId}/avatar.jpg {
  allow read: if request.auth != null;
  allow write: if request.auth != null
               && request.auth.uid == passengerId
               && request.resource.size < 5 * 1024 * 1024
               && request.resource.contentType.matches('image/.*');
}
```

Publícalas en *Firebase Console → Storage → Reglas* (pegando el archivo) o con `firebase deploy --only storage`. La migración no cambia: `photo_url` ya existía en `entity_passenger`.

### Contrato de la API con la app

Fuente de verdad del contrato con `../android-app-taxi`: cada respuesta indica el DTO de Retrofit que la recibe y, para los errores, la `DomainException` de la app y el texto que ve el usuario.

| Aspecto | Backend | App |
| --- | --- | --- |
| Base URL | `http://<host>:${APPLICATION_PORT}/` (3001) | `BuildConfig.API_BASE_URL`, termina en `/` (emulador: `http://10.0.2.2:3001/`) |
| Formato | JSON, `Content-Type: application/json` | Retrofit 3 + `converter-gson` |
| Teléfono | E.164 sin `+`, 11 caracteres (`51` + 9 dígitos) | El usuario escribe 9 dígitos; los casos de uso anteponen `51` |
| Código OTP | 4 dígitos (el DTO acepta 4 a 6) | `OtpCodeInput` de 4 casillas |
| Fechas | ISO-8601 en UTC | `Instant.parse(expiresAt)` |
| Idioma | `Accept-Language` (`es`/`en`), fallback `es` | `HeadersInterceptor` envía `Accept-Language: es` |
| `Authorization` | No se requiere en `/auth`; obligatorio en `/menu` y en todo `/passenger/*` | `AuthInterceptor` lo agrega si hay sesión, salvo en `/auth/*` |
| Sesión | Access token de 15 min y refresh token de 30 días | Ante un 401, `TokenAuthenticator` llama a `auth/refresh` y repite la petición |
| Trazas | `x-request-id` identifica cada petición en los logs | `HeadersInterceptor` envía `X-Request-Id` (UUID); el reintento tras el refresh conserva el mismo id |
| Tiempos | Respuesta típica < 100 ms; SMS corta a los 5 s | OkHttp: 10 s. Si se superan → `SocketTimeoutException` |

**Respuestas exitosas**

| Endpoint | DTO en la app | Cuerpo |
| --- | --- | --- |
| `otp-generate` | `AuthOtpGenerateResponse` | `{"success":true,"expiresAt":"2026-09-28T18:47:00.255Z","ttlSec":120,"messageId":"dev-log"}` |
| `otp-validate` | `AuthOtpValidateResponse` | `{"accessToken":"…","refreshToken":"…","user":{"id","phoneNumber","givenName","familyName","email","photoUrl","status"}}` |
| `auth/refresh` | `RefreshResponse` (solo lee los tokens) | `{"accessToken":"…","refreshToken":"…","user":{…}}` |
| `menu/active/PASSENGER` | `List<MenuDto>` | `[{"key":"passenger_home","text":"Pedir taxi","icon":"home","deeplink":"app-taxi://passenger/home","order":1}, …]` |
| `passenger/signup` | `PassengerDto` | `{"id":"…","phoneNumber":"51987654321","givenName":"Ana","familyName":"Perez","email":"ana@example.com","photoUrl":null,"status":"ACTIVE"}` |
| `passenger/device` | `Unit` (`DeviceApi.register`) | **204** sin cuerpo |
| `passenger/me`, `passenger/photo` | `PassengerDto` | Igual que `passenger/signup` (con `photoUrl`) |
| `passenger/storage-token` | `StorageTokenResponse` | `{"token":"eyJ…","bucket":"app-taxi-xxxxx.firebasestorage.app","photoPath":"passengers/<id>/avatar.jpg"}` |
| Push (FCM, solo datos) | `RemoteMessage.data` → `PushMessage` | `{"title":"Tu taxi está llegando","body":"Carlos llega en 3 min…","deeplink":"app-taxi://passenger/menu"}` |

`user.status` decide la navegación de la app: `ACTIVE` → Home, `INACTIVE_REGISTER` → registro. Los campos de nombre, `email` y `photoUrl` pueden ser `null` (pasajero nuevo).

**Errores** (todos con el formato de [3.1](#31-corehttp--errores-con-un-solo-formato)). `ErrorMapper` de la app muestra el texto del servidor en los 4xx (`errors[0].message` o `message`) y un texto propio en los 5xx:

| HTTP | Endpoint | Clave i18n / origen | `DomainException` | Texto que ve el usuario |
| --- | --- | --- | --- | --- |
| 400 | ambos | `passenger.validation.*`, `otp.validation.*` | `ValidationException` | Primer `errors[].message` |
| 400 | ambos | Campo no permitido / JSON mal formado | `ValidationException` | «property x should not exist» / «Bad Request» |
| 401 | menu, signup | `auth.unauthorized` | — (la app renueva la sesión y reintenta); si persiste, `UnauthorizedException` | Normalmente nada; «Tu sesión expiró. Inicia sesión nuevamente.» si persiste |
| 401 | refresh | `auth.invalidRefreshToken` | La sesión expira: vuelve a «Generar OTP» | Toast «Tu sesión expiró. Inicia sesión nuevamente» |
| 400 | refresh | `auth.refreshTokenRequired` | La sesión expira | Igual que el anterior |
| 404 | — | Ruta inexistente | `ClientException` | «Not Found» |
| 400 | signup | `signup.validation.*` | `ValidationException` | «Tus nombres deben tener entre 2 y 60 caracteres.» · «El correo electrónico no es válido.» |
| 400 | device | `device.validation.*` | `ValidationException` | «El token del dispositivo es obligatorio…» (la app no lo muestra: registrar el token es silencioso) |
| 400 | photo | `photo.validation.url` | `ValidationException` | «La URL de la foto debe ser https…» |
| 422 | photo | `photo.invalidUrl` | `ValidationException` | «La foto no pertenece a tu perfil. Vuelve a subirla.» |
| 503 | storage-token | `photo.notConfigured` | `ServerException` | «La subida de fotos no está disponible en este momento.» |
| 422 | signup | `signup.emailAlreadyUsed` | `ValidationException` | «El correo ya está registrado por otro pasajero.» |
| 422 | generate | `auth.activeCodeExists` | `ValidationException` | «Ya existe un código activo. Espera 1 minuto…» |
| 422 | generate | `auth.deliveryFailed` | `ValidationException` | «No se pudo enviar el código OTP por SMS…» |
| 422 | validate | `auth.invalidOrExpired` | `ValidationException` | «El código OTP es inválido o ha expirado.» |
| 422 | validate | `auth.alreadyUsed` | `ValidationException` | «El código OTP ya fue utilizado.» |
| 422 | validate | `auth.passenger.notExists` | `ValidationException` | «No existe un pasajero registrado con este número…» |
| 429 | validate | `auth.tooManyAttempts` | `ClientException` | «Superaste el número de intentos…» |
| 500 | ambos | `common.internalError` | `ServerException` | «El servidor no está disponible. Inténtalo más tarde» |
| — | ambos | Sin conexión | `NetworkException` | «No se pudo conectar con el servidor…» |
| — | ambos | Más de 10 s sin respuesta | `NetworkException` | «El servidor tardó demasiado en responder» |

Reglas que mantienen el contrato: todo texto de un 4xx va en i18n porque la app lo muestra tal cual; los 5xx no llevan detalles internos; y todo falla antes de 10 s (Redis y SMS cortan a los 5 s) para que la app reciba un código y no un timeout.

---

## 6. Feature `menu` — opciones de menú por aplicación

Primer endpoint **protegido** de la API: la app envía el `accessToken` que recibió en `otp-validate`.

| Capa | Archivos |
| --- | --- |
| Presentación | `controllers/menu.controller.ts`, `dto/menu.dto.ts`, `AccessTokenGuard` (core) |
| Aplicación | `services/menu.service.ts` |
| Datos | `dao/menu.dao.ts`, `entities/menu.entity.ts`, `mapper/menu.mapper.ts`, `enum/` |
| Infraestructura | `CacheService` (core), `JwtService` |
| Módulo | `menu.module.ts`: `TypeOrmModule.forFeature([MenuEntity])`, `JwtModule` y el guard |

### 6.1 `GET /menu/active/:application`

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Guard | `AccessTokenGuard`: header `Authorization: Bearer`, firma y expiración con `JWT_ACCESS_SECRET`; rechaza refresh tokens | **401** `auth.unauthorized` |
| 2 | Nest | `ParseEnumPipe`: `application` debe ser `PASSENGER`, `DRIVER` o `BACKOFFICE` | **400** |
| 3 | Redis | ¿Existe `menu:{application}:active`? Si existe, responde con eso | — |
| 4 | MySQL | `entity_menu` con `status = ACTIVE`, ordenado por `order` | — |
| 5 | Redis | Guarda la lista por `MENU_CACHE_TTL_SEC` (60 s) | — |
| ✓ | — | **200** `[{key, text, icon, deeplink, order}]` | — |

| Columna de `entity_menu` | Uso |
| --- | --- |
| `key` (único) | Identificador estable. La app lo usa como clave primaria en Room |
| `text` | Texto que se muestra |
| `icon` | Nombre lógico (`home`, `profile`, `history`, `support`). La app lo traduce a un ícono local |
| `deeplink` | Destino de la opción (`app-taxi://passenger/...`) |
| `order` (único por aplicación) | Orden ascendente |
| `application`, `status` | Filtros: solo se listan las `ACTIVE` de la aplicación pedida |

Las opciones iniciales salen de `core/database/seeders/data/menus.json` (seeder idempotente por `key`). Si editas una fila en MySQL, el cambio tarda como máximo `MENU_CACHE_TTL_SEC` en llegar a la app.

---

## 7. Feature `notifications` — tokens de FCM y push

![Del backend al teléfono](../android-app-taxi/docs/gif/push-fcm-flow.gif)

| Capa | Archivos |
| --- | --- |
| Presentación | `controllers/device.controller.ts`, `dto/passenger-device-request.dto.ts`, `AccessTokenGuard` (core) |
| Aplicación | `services/notification.service.ts` |
| Datos | `dao/passenger-device.dao.ts`, `entities/passenger-device.entity.ts`, `enum/device-platform.enum.ts` |
| Infraestructura | `remote/push-sender.ts` (contrato) y `remote/fcm-push.service.ts` (`firebase-admin`) |
| CLI | `cli/send-push.ts` |

### 7.1 `PUT /passenger/device`

| Paso | Dónde | Qué hace | Si falla |
| --- | --- | --- | --- |
| 1 | Guard | `AccessTokenGuard`: el pasajero sale del `sub` del token | **401** |
| 2 | DTO | `token` de 1 a 255 caracteres; `platform` `ANDROID` (por defecto) o `IOS` | **400** `device.validation.*` |
| 3 | DAO | Si el token ya existe, lo reasigna al pasajero; si no, lo inserta. Repetir la llamada no duplica filas | — |
| ✓ | — | **204** sin cuerpo | — |

La app lo llama al entrar a Home y en `onNewToken`. Un pasajero puede tener varios teléfonos; un token pertenece a un solo pasajero (índice único).

### 7.2 Enviar un push

`NotificationService.sendToPassenger(passengerId, { title, body, deeplink })` busca los tokens del pasajero, llama a `PushSender.send` y **borra los tokens que FCM rechaza** (`registration-token-not-registered`, `invalid-registration-token`), por ejemplo los de una app desinstalada o de una sesión cerrada.

`FcmPushService` envía un **mensaje de solo datos** con prioridad alta:

```ts
getMessaging(app).sendEachForMulticast({
    tokens,
    data: { title, body, deeplink },
    android: { priority: "high" },
});
```

| Sin credenciales de Firebase | Comportamiento |
| --- | --- |
| `NODE_ENV` distinto de `production` | Modo desarrollo: escribe `[DEV] Push a N dispositivo(s): …` en el log y cuenta los envíos como hechos |
| `NODE_ENV=production` | Registra el error y no envía nada |

Para probarlo sin código de negocio, el CLI envía al pasajero por su teléfono:

```bash
pnpm push:send 51987654321                              # local (ts-node)
pnpm push:send 51987654321 app-taxi://passenger/menu    # abre el Menú al tocarla
docker exec http-app-taxi node dist/features/notifications/cli/send-push.js 51987654321   # en Docker
```

Salida: `[push] enviados: 1 · tokens inválidos borrados: 0`. Si el teléfono no existe, termina con código 1.

### 7.3 Credenciales de Firebase

1. Firebase Console → *Configuración del proyecto* → *Cuentas de servicio* → **Generar nueva clave privada** (descarga un JSON).
2. Copia tres campos del JSON al `.env` (nunca subas el JSON ni el `.env` a git):

```bash
FIREBASE_PROJECT_ID=app-taxi-xxxxx
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@app-taxi-xxxxx.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIE...\n-----END PRIVATE KEY-----\n"
```

`FIREBASE_PRIVATE_KEY` va en una sola línea con `\n`; `FirebaseAdminService` los convierte en saltos de línea. `FIREBASE_STORAGE_BUCKET` es opcional (por defecto `<project-id>.firebasestorage.app`). La misma cuenta de servicio sirve para el push y para firmar los custom tokens de la foto.

---

## 8. Feature `trips` — viajes y ofertas

![Ofertas de viaje en tiempo real con Socket.IO](../android-app-taxi/docs/gif/realtime-offers.gif)

Regla de diseño: **los comandos van por REST y los eventos por Socket.IO**. Solicitar, ofertar, aceptar o cambiar de estado son peticiones HTTP con su validación y su error; después, el servicio avisa por el socket a quien le interesa. La teoría está en el [README de la clase](../README.md).

```
REQUESTED ──accept──▶ ACCEPTED ──▶ ARRIVED ──▶ IN_PROGRESS ──▶ COMPLETED
    │                    │            │
    └───── cancel (pasajero) ─────────┴──────▶ CANCELLED
```

| Método | Ruta | Quién | Respuesta y efecto |
| --- | --- | --- | --- |
| POST | `/trips/quote` | pasajero | `QuoteDto` (ruta OSRM y `estimatedFare`). 503 si OSRM no responde |
| POST | `/trips` | pasajero | `TripDto` en `REQUESTED`; emite `trip:requested` a los conductores cercanos. 422 si ya tiene un viaje activo |
| GET | `/trips/current` | pasajero | `{ trip, offers }`: viaje activo (con sus ofertas pendientes si está en `REQUESTED`) o el último completado sin calificar (1 h) |
| POST | `/trips/:id/offers/:offerId/accept` | pasajero | `TripDto` en `ACCEPTED` con `finalFare` = precio de la oferta; emite `trip:offer-accepted`, `trip:unavailable` (`TAKEN`) y `trip:status` |
| POST | `/trips/:id/cancel` | pasajero | `TripDto` en `CANCELLED` (desde `REQUESTED`, `ACCEPTED` o `ARRIVED`); libera al conductor |
| POST | `/trips/:id/rating` | pasajero | Califica al conductor (1–5) una sola vez y actualiza su promedio |
| GET | `/driver/trips/requests` | conductor | Solicitudes `REQUESTED` de los últimos `TRIP_REQUEST_TTL_SEC`, a menos de `DRIVER_SEARCH_RADIUS_KM` y sin oferta suya |
| GET | `/driver/trips/current` | conductor | `{ trip }`: viaje activo o completado sin calificar por él |
| POST | `/driver/trips/:id/offers` | conductor | `OfferDto` con ETA al punto de recojo (OSRM, o línea recta a 25 km/h si falla); emite `trip:offer` al pasajero. Exige estar en labores, libre y sin oferta previa |
| PUT | `/driver/trips/:id/status` | conductor | Solo el siguiente estado: `ARRIVED` → `IN_PROGRESS` → `COMPLETED`; emite `trip:status`. Al completar queda libre |
| POST | `/driver/trips/:id/rating` | conductor | Califica al pasajero (1–5) una sola vez |

Las transiciones usan un `UPDATE ... WHERE status IN (...)` condicional (`TripDao.transition`): si dos peticiones compiten, solo una cambia la fila y la otra recibe 422.

**Tarifa:** `estimatedFare = round2(baseFare + pricePerKm × distanceMeters / 1000)` con la tarifa de `entity_tariff` (`calculateFare` en `tariff.service.ts`).

**Rutas con OSRM:** `OsrmRoutePlanner` pide `GET {OSRM_URL}/route/v1/driving/{lng},{lat};{lng},{lat}?overview=full&geometries=polyline` y devuelve distancia, duración y la polyline (precisión 5) que dibujan las apps y el backoffice. El servidor público `router.project-osrm.org` sirve para clase; en producción conviene uno propio ([infra](../infra-app-taxi/README.md#osrm-propio-opcional)).

| Capa | Archivos |
| --- | --- |
| Presentación | `controllers/passenger-trip.controller.ts`, `driver-trip.controller.ts`, `admin-trip.controller.ts`, `dto/place.dto.ts`, `dto/trip-request.dto.ts`, `dto/trip.dto.ts` |
| Aplicación | `services/passenger-trip.service.ts`, `driver-trip.service.ts`, `driver-location.service.ts`, `admin-trip.service.ts` |
| Datos | `dao/trip.dao.ts`, `dao/trip-offer.dao.ts`, `entities/trip.entity.ts`, `entities/trip-offer.entity.ts`, `mapper/trip.mapper.ts` |
| Infraestructura | `services/trip-notifier.service.ts` (eventos), `services/trip-track.service.ts` (recorrido en Redis), `../routing/osrm-route-planner.ts` |

---

## 9. Gateway Socket.IO — `/realtime`

| Paso | Qué ocurre |
| --- | --- |
| Conexión | `io("{API}/realtime", { auth: { token } })`, también acepta `Authorization: Bearer`. El middleware de `afterInit` valida el access token (no acepta refresh tokens). Si falla, el cliente recibe `connect_error` con `message: "unauthorized"` |
| Sala | Según `typ` del JWT: `passenger:{id}`, `driver:{id}` o `admins`. El servidor siempre emite a salas, nunca a un `socket.id` |
| Bienvenida | `session:ready { userType, userId }` |

| Evento | Dirección | A quién | Payload |
| --- | --- | --- | --- |
| `driver:location` | conductor → servidor (con ack `{ ok }`) | — | `{ lat, lng, bearing?, speed?, accuracy? }` cada ~3 s |
| `trip:requested` | servidor → cliente | conductores en labores, libres y a menos de `DRIVER_SEARCH_RADIUS_KM` del origen | `TripDto` |
| `trip:offer` | servidor → cliente | pasajero del viaje | `OfferDto` |
| `trip:offer-accepted` | servidor → cliente | conductor ganador | `TripDto` |
| `trip:unavailable` | servidor → cliente | conductores que recibieron la solicitud y no ganaron | `{ tripId, reason: "TAKEN" \| "CANCELLED" }` |
| `trip:status` | servidor → cliente | pasajero y conductor del viaje | `TripDto` |
| `driver:location` | servidor → cliente | pasajero del viaje activo del conductor y `admins` | `{ driverId, tripId, lat, lng, bearing, speed, at }` |
| `driver:availability` | servidor → cliente | `admins` | `{ driverId, online }` |
| `trip:updated` | servidor → cliente | `admins` | `TripDto` en cada cambio |

| Clave de Redis | Tipo | Contenido |
| --- | --- | --- |
| `drivers:online` | set | Conductores en labores |
| `drivers:locations` | hash | Última posición de cada conductor (`{ lat, lng, bearing, speed, accuracy, at }`); se descarta si tiene más de `DRIVER_LOCATION_STALE_SEC` |
| `driver:{id}:trip` | string | `{ tripId, passengerId }` del viaje activo: decide a qué pasajero reenviar su ubicación |
| `trip:{id}:notified` | set (1 h) | Conductores que recibieron la solicitud, para avisarles `trip:unavailable` |
| `trip:{id}:track` | lista (7 días) | Recorrido real del conductor durante el viaje (`GET /admin/trips/:id`) |

`RealtimeEmitter` (en `core/realtime`) guarda el namespace y emite a salas; `TripNotifier` traduce cada hecho del dominio a su evento. Así los servicios se prueban sin servidor de sockets.

---

## 10. Feature `drivers` — conductores

| Método | Ruta | Detalle |
| --- | --- | --- |
| POST | `/driver/auth/login` | `{ phone, password }` → `{ accessToken, refreshToken, driver }`. Contraseña verificada con `argon2`; un conductor `INACTIVE` recibe 403 |
| POST | `/driver/auth/refresh` | Igual que el del pasajero, con `typ: "driver"` |
| GET | `/driver/me` | `DriverDto` con vehículo y rating |
| PUT | `/driver/availability` | `{ online, lat?, lng? }`: iniciar o terminar labores. No se puede terminar con un viaje activo (422). Emite `driver:availability` a `admins` |

Los endpoints de conductor usan `DriverAccessGuard`: un token de pasajero o de admin recibe 403.

---

## 11. Backoffice: `admins`, `tariff` y `/admin/*`

Todos usan `AdminAccessGuard`. El token de admin dura `ADMIN_JWT_TTL_SEC` (8 h) y no tiene refresh.

| Método | Ruta | Detalle |
| --- | --- | --- |
| POST | `/admin/auth/login` | `{ email, password }` → `{ accessToken, admin }` |
| GET | `/admin/me` | Perfil del admin |
| GET · POST · PUT · DELETE | `/admin/admins[/:id]` | CRUD; la contraseña es opcional al editar; un admin no puede eliminarse ni desactivarse a sí mismo |
| GET | `/admin/passengers?search=&page=&size=` | Pasajeros paginados (búsqueda por nombre, teléfono o email) |
| GET · POST · PUT · DELETE | `/admin/drivers[/:id]` | CRUD con vehículo; teléfono, email y placa únicos; el listado agrega `online`. Eliminar es borrado lógico y no se permite con un viaje activo |
| GET | `/admin/drivers/locations` | Última posición de cada conductor con `online` y `tripId` |
| GET | `/admin/trips?status=&page=&size=` · `/admin/trips/active` · `/admin/trips/:id` | Historial, viajes activos y detalle con `offers` y `track` |
| GET · PUT | `/admin/tariff` | `{ baseFare, pricePerKm }` |

El listado de pasajeros vive en `features/passengers/controllers/admin-passenger.controller.ts`; cada feature expone sus propios endpoints de admin.

---

## 12. Ejecutar, probar y mantener

### Requisitos e instalación

| Herramienta | Versión | Notas |
| --- | --- | --- |
| Node.js | 24 LTS | Con Node 22.14 `pnpm build` falla con `ERR_REQUIRE_CYCLE_MODULE` |
| pnpm | 12.5.1 | `corepack enable` descarga la versión de `packageManager` |
| MySQL / Redis | 9.7 LTS / 8.10 | Se levantan con [`../infra-app-taxi`](../infra-app-taxi/README.md) |
| Proveedor SMS | Brevo o LabsMobile | Opcional en desarrollo |

```bash
corepack enable
pnpm install
touch .env          # ver variables abajo; .env está en .gitignore
```

`pnpm-workspace.yaml` declara `allowBuilds` (desde pnpm 11 la instalación falla con `ERR_PNPM_IGNORED_BUILDS` si una dependencia trae script de build sin declarar).

### Configuración (`.env`)

```bash
# App
APPLICATION_PORT=3001
NODE_ENV=development              # "production" desactiva el modo SMS de desarrollo
NODE_DEBUG=false                  # true = logs con pino-pretty (solo local)

# MySQL (valores de ../infra-app-taxi/.env)
DB_HOST=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=change-me
DB_NAME=db_app_taxi
DB_POOL=10

# Redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379

# JWT
JWT_ACCESS_SECRET=change-this-access
JWT_REFRESH_SECRET=change-this-refresh
JWT_ACCESS_TTL_SEC=900            # 15 min (usa 20 para ver el refresh en clase)
JWT_REFRESH_TTL_SEC=2592000       # 30 días

# OTP (opcionales; valores por defecto)
OTP_TTL_SEC=120
OTP_RATE_TTL_SEC=60
OTP_MAX_ATTEMPTS=5

# Menú (opcional)
MENU_CACHE_TTL_SEC=60             # segundos en Redis

# SMS: "brevo" (por defecto) o "labsmobile"
SMS_PROVIDER=brevo
BREVO_API_KEY=                    # vacío = modo desarrollo
BREVO_TEXT_SMS="Tu código de App Taxi es:"
BREVO_SENDER=AppTaxi
LABSMOBILE_USER=
LABSMOBILE_API_KEY=
LABSMOBILE_TEXT_SMS="Tu código de App Taxi es:"
LABSMOBILE_SENDER=AppTaxi

# Push (opcional): cuenta de servicio de Firebase. Vacío = modo desarrollo
FIREBASE_PROJECT_ID=
FIREBASE_CLIENT_EMAIL=
FIREBASE_PRIVATE_KEY=
FIREBASE_STORAGE_BUCKET=          # opcional

# Viajes y tiempo real (opcionales; valores por defecto)
OSRM_URL=https://router.project-osrm.org
OSRM_TIMEOUT_MS=8000
DRIVER_SEARCH_RADIUS_KM=10        # radio para avisar la solicitud a conductores
DRIVER_LOCATION_STALE_SEC=120     # una ubicación más vieja no cuenta como cercana
TRIP_REQUEST_TTL_SEC=600          # ventana de GET /driver/trips/requests
ADMIN_JWT_TTL_SEC=28800           # 8 h
CORS_ORIGINS=                     # vacío = cualquier origen; o "http://localhost:5173,http://localhost:8080"
SEED_ADMIN_EMAIL=admin@apptaxi.pe
SEED_ADMIN_PASSWORD=Admin123*
SEED_DRIVER_PASSWORD=Driver123*
```

### Ejecución, migraciones y pruebas

```bash
pnpm migration:run                 # aplicar migraciones pendientes
pnpm migration:revert              # revertir la última
pnpm migration:generate src/core/database/migrations/<nombre>   # sin "--"
pnpm exec ts-node -r tsconfig-paths/register src/core/cli/run-seeders.ts

pnpm start:dev                     # desarrollo · Swagger en /api/docs
pnpm build && pnpm start:prod

pnpm push:send 51987654321 [deeplink]   # push de prueba a un pasajero

pnpm test                          # 82 pruebas: las 58 anteriores + PassengerTripService (8), DriverTripService (7), DriverLocationService (3), OsrmRoutePlanner (3), calculateFare (3)
pnpm lint                          # ESLint con --fix
```

- NestJS 12 se publica solo como ESM: el script `test` corre Jest con `--experimental-vm-modules`, y `jest.moduleNameMapper` resuelve los imports `src/...`.

### Docker

`Dockerfile` multi-stage sobre `node:24-alpine` con pnpm 12.5.1. `entrypoint.sh` espera a MySQL, migra, siembra y arranca `node dist/main.js` (ver [3.3](#33-coredatabase-y-corecli--mysql-y-migraciones)). Para levantar o actualizar el stack sigue [`../infra-app-taxi/README.md`](../infra-app-taxi/README.md). Con `NODE_DEBUG=true` la imagen no arranca (`pino-pretty` es dependencia de desarrollo).

### Seguridad

| Control | Estado |
| --- | --- |
| Generación de la OTP | `crypto.randomInt` (CSPRNG) |
| Fuerza bruta | Máximo `OTP_MAX_ATTEMPTS` fallos → **429** |
| Reenvío abusivo de SMS | Lock por pasajero en Redis (`OTP_RATE_TTL_SEC`) |
| Reutilización del código | `markAsUsed` + `findValidOtp` filtra `used = false` y `expires_at > NOW()` |
| Almacenamiento de la OTP | En claro en `entity_passenger_otp.code`. **Pendiente**: guardar un hash (p. ej. HMAC-SHA256) |
| Enumeración de teléfonos | `otp-generate` responde igual exista o no el pasajero |
| Rutas protegidas | `AccessTokenGuard` en `/menu` y en todo `/passenger/*`; el id del pasajero se toma del token, no del body. Un refresh token no sirve como access token |
| JWT | Secretos fuertes; payload sin datos sensibles. El access token dura 15 min y se renueva con `POST /auth/refresh`, que rota el refresh token. **Pendiente**: revocar el refresh token anterior y los de un pasajero que cierra sesión |
| Secretos | `.env` fuera de git; credenciales SMS y la cuenta de servicio de Firebase nunca en el repositorio ni en el log |
| Viajes | Cada ruta exige el tipo de usuario correcto (`PassengerAccessGuard`, `DriverAccessGuard`, `AdminAccessGuard`); el pasajero y el conductor solo ven y cambian sus propios viajes (404 si no son suyos); el socket solo acepta access tokens y cada usuario recibe únicamente los eventos de su sala |
| Contraseñas | `argon2id` para conductores y administradores; la columna `password_hash` no se selecciona por defecto. Las contraseñas del seed son solo para desarrollo |
| Archivos | La app sube directo a Storage con un custom token de su propio `uid`; `storage.rules` limita a su ruta, imágenes y < 5 MB; `PUT /passenger/photo` rechaza URLs de otra ruta |

### Troubleshooting

| Síntoma | Causa / solución |
| --- | --- |
| `ERR_REQUIRE_CYCLE_MODULE` en `pnpm build` | Node demasiado antiguo: usa Node 24 LTS |
| `Must use import to load ES Module` en Jest | Jest sin `--experimental-vm-modules`: usa `pnpm test` |
| `Cannot find module 'src/…'` en Jest | Falta `moduleNameMapper` (`^src/(.*)$`) en `package.json` |
| `Redis Client Error … ECONNREFUSED` | Redis no está arriba o `REDIS_HOST`/`REDIS_PORT` no coinciden |
| **500** «Ocurrió un error inesperado…» | MySQL o Redis caídos, o un bug: el stack está en el log |
| **422** `deliveryFailed` | El proveedor rechazó el envío (log `Brevo respondió …` / `LabsMobile respondió …`) o producción sin credenciales |
| No arranca: `SMS_PROVIDER inválido` | Solo acepta `brevo` o `labsmobile` |
| No llega el SMS en local | Sin credenciales no se envía: busca `[DEV] OTP para` en el log |
| **401** «Tu sesión expiró…» en `/menu` | Falta el header `Authorization`, el access token venció (15 min) o se envió el refresh token: la app lo resuelve sola llamando a `/auth/refresh` |
| **401** «Tu sesión ya no es válida…» en `/auth/refresh` | El refresh token venció, se firmó con otro `JWT_REFRESH_SECRET`, es un access token o el pasajero ya no existe |
| El menú no refleja un cambio hecho en MySQL | Caché de Redis: espera `MENU_CACHE_TTL_SEC` o borra la clave `menu:PASSENGER:active` |
| **422** `activeCodeExists` / **429** `tooManyAttempts` | Lock de 60 s activo / 5 intentos fallidos: pide un código nuevo |
| i18n muestra la clave (`otp.validation.pattern`) | Falta el archivo o la clave en `core/i18n/<lang>/` |
| La app muestra «Not Found» | `API_BASE_URL` apunta a otro servicio o le falta la `/` final |
| **503** en `POST /passenger/storage-token` | Faltan `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL` o `FIREBASE_PRIVATE_KEY` |
| La app sube pero Storage responde *permission denied* | No publicaste `firebase/storage.rules`, o el bucket de la app no es el de `FIREBASE_STORAGE_BUCKET` |

---

## Licencia

Uso académico / educativo.
