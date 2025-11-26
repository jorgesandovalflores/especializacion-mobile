# Módulo 05 · Sesión 04

## Firma y publicación en Play Store (Actualizado a 2025)

## Objetivos de la sesión

Al finalizar esta sesión, el estudiante será capaz de:

1. Generar un **Android App Bundle (.aab)** firmado correctamente con un **keystore** seguro.
2. Configurar los aspectos básicos de **Google Play Console** para publicar una app (ficha de tienda, testers, track de release).
3. Cumplir el **checklist de políticas y requisitos** vigentes en 2025 (target API, Data Safety, privacidad, contenido).
4. Diseñar una estrategia de **mantenimiento, versionado y releases beta** (closed testing, open testing, producción).

---

## Contenido

1. Generación de archivo `.aab` y firma con Keystore
2. Configuración de Google Play Console
3. Checklist de políticas y requisitos (2025)
4. Mantenimiento, actualizaciones y versiones beta

---

## 1) Generación de archivo .aab y firma con Keystore

### 1.1 ¿Por qué .aab y no .apk?

-   Desde hace años, Google Play exige **.aab (Android App Bundle)** para nuevas apps; el `.apk` queda más para distribución directa o testing local.
    Link de referencia: https://support.google.com/googleplay/android-developer/answer/9842756
    Link de referencia: https://developer.android.com/guide/app-bundle

Beneficios principales:

-   Menor tamaño de descarga gracias a APKs generados por dispositivo (idioma, densidad, ABI).
-   Mejor manejo de firmas con **Play App Signing** (Google guarda la clave de firma principal y genera los APK finales).
    Link de referencia: https://developer.android.com/studio/publish/app-signing

En la práctica, para Play Store en 2025 publicas **.aab** casi siempre; el `.apk` se usa para distribución directa, stores alternativos o pruebas locales específicas.

### 1.2 Keystore, app signing key y upload key (modelo 2025)

En 2025, el flujo estándar recomendado es:
Link de referencia: https://developer.android.com/studio/publish/app-signing
Link de referencia: https://support.google.com/googleplay/android-developer/answer/9842756

-   **App signing key**:

    -   Es la clave “maestra” con la que se firmarán los APK que reciben los usuarios.
    -   Normalmente la gestiona Google mediante **Play App Signing** (obligatorio para nuevas apps).

-   **Upload key**:
    -   Es la clave que usas localmente para firmar el `.aab` que subes a Play.
    -   Si tu upload key se ve comprometida, puedes rotarla sin perder la app signing key.

Esto separa la seguridad de la clave final (controlada por Google) del riesgo operativo de tu entorno local.

En Android Studio, a nivel de proyecto:

1. Generas un keystore con una key que usarás como **upload key**.
2. Puedes configurar `signingConfigs` y `buildTypes` en Gradle (si quieres automatizar) o usar siempre el asistente de firma.

### 1.3 Pasos prácticos para generar el keystore y el .aab (Android Studio)

1. En Android Studio: `Build > Generate Signed App Bundle / APK...`.
2. Selecciona **Android App Bundle (.aab)**.
3. En “Key store path”:
    - Si ya tienes keystore: selecciona el archivo `.jks` o `.keystore` y completa contraseñas.
    - Si no tienes: clic en **Create new...** y rellena:
        - Ruta del archivo (`my-upload-key.jks`, por ejemplo).
        - Contraseña del keystore.
        - Alias de la key.
        - Contraseña de la key.
        - Datos de organización (Name, Org, Country, etc.).
4. Marca **Remember passwords** solo si el entorno es controlado (ej. máquina de build segura).
5. Elige el `Build Type` **release**.
6. Confirma y espera a que finalice la generación del bundle.
7. Android Studio genera el archivo en una ruta similar a: `app/release/app-release.aab`.

Buenas prácticas de seguridad:

-   Guardar el keystore y sus contraseñas en un almacén seguro (ej. gestor de secretos de tu cloud o password manager corporativo).
-   Nunca subir el keystore al repositorio Git.
-   Documentar claramente:
    -   Dónde está almacenada la clave.
    -   Quién tiene acceso.
    -   Procedimiento de backup y restauración.

---

## 2) Configuración de Google Play Console

### 2.1 Crear cuenta de desarrollador y proyecto

-   Crear la cuenta de desarrollador de Google Play (pago único).
-   En 2025 se exigen más pasos de verificación de identidad (documentos y datos de contacto reales).
-   Si la app es de una empresa, es preferible usar un correo y datos corporativos, no personales.

### 2.2 Crear la aplicación en Play Console

En Google Play Console:

1. Ir a **Todas las aplicaciones > Crear aplicación**.
2. Definir:
    - Idioma principal de la ficha de la tienda.
    - Nombre de la app (puede ser distinto del `applicationId`).
    - Tipo de app: **Aplicación** o **Juego**.
    - Si será **gratuita** o **de pago**.
    - Países o regiones donde estará disponible (esto se puede ajustar luego).
3. Al crearla, Play Console genera la estructura básica de secciones, entre ellas:
    - **App content / Contenido de la app** (clasificación de contenido, anuncios, público objetivo, etc.).
    - **Store listing / Ficha de la tienda**:
        - Nombre, descripción corta y larga.
        - Icono de alta resolución.
        - Capturas de pantalla obligatorias (normalmente teléfono y opcionalmente tablet, TV, etc.).
        - Videos promocionales si aplica.
    - **App integrity** y **Play App Signing**:
        - Aceptar el esquema de firma gestionada.
        - Ver estado de integridad y protección.

Link de referencia: https://support.google.com/googleplay/android-developer/answer/9859152
Link de referencia: https://developer.android.com/distribute/console?hl=es-419

### 2.3 Subir el .aab y preparar un release

Flujo típico recomendado:

1. En Play Console, ir a la sección de **Testing** (por ejemplo, `Internal testing`).
2. Crear un **nuevo release**:
    - Subir el archivo `.aab` generado en Android Studio.
    - Esperar a que Play analice el bundle y muestre advertencias (tamaño, permisos, target API, etc.).
3. Agregar las **notas de la versión** (changelog) explicando cambios y novedades.
4. Seleccionar los testers (lista de correos o grupos de Google) para el track interno.
5. Guardar y enviar a revisión.

Una vez que la versión sea aprobada y validada en pruebas:

-   Puedes promocionar ese mismo release a **closed testing**, **open testing** o directamente a **production**, según tu estrategia.

Link de referencia: https://developer.android.com/studio/publish?hl=es-419

---

## 3) Checklist de políticas y requisitos (2025)

### 3.1 Target API Level (muy importante en 2025)

En 2025, Google endurece los requisitos de `targetSdk`:

-   A partir del **31 de agosto de 2025**, todas las apps nuevas y actualizaciones en Google Play deben:
    -   Targetear **Android 15 (API level 35)** para la mayoría de apps móviles.
    -   Excepciones: TV, Automotive y Wear OS pueden requerir como mínimo **API 34** según el tipo de dispositivo.
-   Apps existentes deben targetear al menos **Android 14 (API 34)** para seguir siendo visibles a nuevos usuarios.
-   Es posible solicitar una **extensión** limitada hasta el **1 de noviembre de 2025**, pero no está garantizada.

Link de referencia: https://developer.android.com/google/play/requirements/target-sdk?hl=es-419
Link de referencia: https://support.google.com/googleplay/android-developer/answer/11926878
Link de referencia: https://developer.android.com/about/versions/15

Checklist técnico mínimo:

-   Asegúrate de que `targetSdkVersion` / `targetSdk` en tu `build.gradle` sea 35 para nuevas apps y actualizaciones estándar.
-   Revisa cambios de comportamiento entre versiones (permisos, restricciones en background, notificaciones, etc.).
-   Valida que tus librerías y SDKs de terceros soporten API 35.

### 3.2 Data Safety Form (Formulario de seguridad de datos)

Google exige completar la sección **Data safety** en Play Console. Debes declarar:

-   Qué datos recopila tu app:
    -   Datos personales (nombre, email, teléfono).
    -   Ubicación.
    -   Identificadores de dispositivo o publicidad.
    -   Información financiera, de salud, etc., si aplica.
-   Si compartes datos con terceros (por ejemplo, proveedores de analítica o anuncios).
-   Para qué usas esos datos:
    -   Analítica, personalización, publicidad, seguridad, prevención de fraude, etc.
-   Qué mecanismos de seguridad aplicas (cifrado en tránsito, en reposo, etc.).

IMPORTANTE:

-   Debes incluir no solo lo que recolectas tú directamente, sino también lo que colectan los **SDKs de terceros** (Analytics, Ads, Crashlytics, etc.).
-   Si la información declarada en Data Safety no coincide con el comportamiento real de la app, Google puede rechazar el release o incluso suspender la app.

Link de referencia: https://support.google.com/googleplay/android-developer/answer/10787469
Link de referencia: https://support.google.com/googleplay/android-developer/answer/10144311
Link de referencia: https://support.google.com/googleplay/android-developer/answer/10356174

### 3.3 Política de Privacidad y otros textos legales

Todas las apps en Play deben tener una **Política de privacidad** adecuada:

-   Debe estar publicada en una URL accesible públicamente (ej. tu web corporativa).
-   Debe estar enlazada en la ficha de la app en Play Store.
-   Idealmente, también debe ser accesible **dentro de la app** (por ejemplo, en un menú de ajustes o sección “Privacidad”).

Contenido mínimo recomendado de la política:

-   Qué datos recolectas.
-   Base legal o motivo (por ejemplo, consentimiento del usuario, ejecución de contrato, interés legítimo, según normativa aplicable).
-   Cómo almacenan y protegen los datos.
-   Por cuánto tiempo los conservan.
-   Con quién se comparten (proveedores, terceros de analítica, etc.).
-   Cómo puede el usuario ejercer sus derechos (eliminar cuenta, eliminar datos, descargar información, etc.).

Link de referencia: https://support.google.com/googleplay/android-developer/answer/113469#privacy
Link de referencia: https://support.google.com/googleplay/android-developer/answer/10787469

### 3.4 Contenido, anuncios y público objetivo

En Play Console deberás declarar:

-   **Target audience**:
    -   Si tu app está dirigida a niños, adultos o ambos.
    -   Esto afecta la **families policy** y restricciones de anuncios.
-   **Ads**:
    -   Si tu app muestra o no anuncios.
    -   Tipo de anuncios (en pantalla completa, recompensados, etc.).
-   **Content rating**:
    -   Cuestionario para obtener la clasificación por edades según región.

También es necesario revisar políticas especiales si tu app:

-   Usa permisos sensibles (ubicación en background, SMS, llamadas, accesibilidad).
-   Tiene funcionalidades de salud, bienestar, finanzas, juegos de azar, contenido generado por usuarios, etc.

Link de referencia: https://support.google.com/googleplay/android-developer/answer/9283445

---

## 4) Mantenimiento, actualizaciones y versiones beta

### 4.1 Estrategia de tracks: internal, closed, open, production

Google Play Console ofrece varios **tracks** para gestionar el ciclo de vida de tus releases:

1. **Internal testing**:

    - Pensado para el equipo interno (hasta ~100 testers por lista de correos).
    - La publicación es muy rápida.
    - Ideal para QA y pruebas previas a cualquier beta pública.

2. **Closed testing**:

    - Grupos más amplios pero aún controlados.
    - Permite probar con usuarios seleccionados (por ejemplo, clientes clave o comunidad cerrada).
    - Útil para una **beta cerrada** con feedback real.

3. **Open testing**:

    - La app se publica en Play con etiqueta de **Beta**.
    - Cualquier usuario del país puede unirse a la prueba.
    - Adecuado cuando quieres feedback masivo antes del lanzamiento general.

4. **Production**:
    - Versión estable para todos los usuarios de los países habilitados.
    - Debe ser la versión más probada y estable.

Estrategia recomendada de flujo:

-   Rama `develop` o `main` estable → generar build → subir a **Internal testing**.
-   Una vez aprobada por el equipo → mover a **Closed testing**.
-   Si quieres más feedback → promover a **Open testing**.
-   Finalmente, lanzar la versión a **Production** con un **rollout gradual** (porcentaje de usuarios).

### 4.2 Versionado y despliegues controlados

A nivel de proyecto Android (Gradle/manifest):

-   `versionCode`:

    -   Entero que **siempre** debe incrementarse en cada release.
    -   Es el valor que Play usa para saber si un APK/AAB es más nuevo.

-   `versionName`:
    -   Cadena legible para humanos (por ejemplo, `1.3.0`, `2.0.1`).
    -   Generalmente sigue **SemVer**: `major.minor.patch`.

Buenas prácticas:

-   Documentar en un archivo de cambios (CHANGELOG) qué trae cada versión.
-   Relacionar `versionCode` con `versionName` para simplificar debug (por ejemplo, usar esquema 10000 _ major + 100 _ minor + patch).

En Play Console:

-   Usar **staged rollout**:
    -   Comenzar desplegando a un pequeño porcentaje (por ejemplo, 5–10 % de los usuarios).
    -   Monitorear:
        -   Crashes y ANRs (Android Vitals).
        -   Métricas de rendimiento.
        -   Comentarios y valoraciones en la tienda.
-   Si se detectan problemas graves:
    -   Detener el rollout.
    -   Corregir y subir un hotfix con nuevo `versionCode`.

### 4.3 Mantenimiento continuo y cumplimiento de políticas

En 2025, Google es más agresivo en:

-   Hacer cumplir el **target API level** actualizado (API 35).
-   Verificar que la información declarada en **Data Safety** y en la política de privacidad sea coherente con el comportamiento real de la app.

Plan de mantenimiento recomendado:

1. **Revisión periódica (al menos anual o con cada gran release de Android):**

    - Actualizar `targetSdk`.
    - Revisar permisos en el `AndroidManifest`.
    - Validar que las librerías y SDKs de terceros estén actualizados y compatibles.

2. **Revisar de forma continua Play Console:**

    - Sección de **Android Vitals** (crashes, ANRs, consumo de batería).
    - Sección de **Policy status** para ver advertencias o posibles incumplimientos.

3. **Pipeline CI/CD (opcional pero recomendado):**
    - Automatizar la generación de `.aab` firmados con la upload key.
    - Integrar con la API de Google Play Developer para subir releases a `Internal testing` de forma automática.

Link de referencia: https://developer.android.com/google/play/requirements/target-sdk?hl=es-419
Link de referencia: https://support.google.com/googleplay/android-developer/answer/10144311

---

## 5) Resumen para el estudiante

-   Siempre genera **.aab** firmados con una **upload key** y habilita **Play App Signing** para que Google gestione la clave maestra.
    Link de referencia: https://developer.android.com/studio/publish/app-signing
    Link de referencia: https://support.google.com/googleplay/android-developer/answer/9842756

-   Asegúrate de cumplir el **target API level** exigido (en 2025, Android 15 / API 35 para nuevas apps y actualizaciones estándar).
    Link de referencia: https://developer.android.com/google/play/requirements/target-sdk?hl=es-419

-   Completa correctamente el formulario de **Data Safety** y publica una **Política de privacidad** clara y honesta.
    Link de referencia: https://support.google.com/googleplay/android-developer/answer/10787469

-   Usa los distintos tracks de testing (**internal**, **closed**, **open**) antes de lanzar a **production**.
-   Realiza **rollouts graduales**, supervisa métricas y mantén la app actualizada para evitar bloqueos por políticas o mala calidad.
