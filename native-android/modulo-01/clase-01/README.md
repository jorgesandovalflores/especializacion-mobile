# Sesión 1 - Configuración del entorno y estructura del proyecto (Android + Jetpack Compose)

> Curso: **Especialización en Desarrollo Móvil --- Android/Kotlin**\
> Módulo 1 · Sesión 1

------------------------------------------------------------------------

## Objetivos de aprendizaje

Al finalizar esta sesión, el estudiante será capaz de:

1.  Comprender el **origen de Android**, la evolución de **Android
    Studio**, el rol del **JDK** y los conflictos históricos entre Sun,
    Oracle y Google — incluyendo el paso de **Java a Kotlin** como
    lenguaje oficial.
2.  Explicar la diferencia entre **Gradle Groovy** (`.gradle`) y
    **Gradle Kotlin DSL** (`.gradle.kts`).
3.  Instalar Android Studio y configurar correctamente el **Android
    SDK** y sus componentes.
4.  Comprender la **estructura de un proyecto Android** moderno y los
    archivos de configuración.
5.  Configurar **emuladores (AVD)** y dispositivos físicos para pruebas.
6.  Crear y ejecutar un **proyecto base con Jetpack Compose**.
7.  Reconocer la importancia de las versiones de Android Studio y APIs
    de Android al crear proyectos.
8.  Entender la relevancia de un **package name único** en cada
    aplicación.
9.  Identificar y usar la **página oficial de desarrolladores de
    Android** como referencia principal.
10. Analizar el impacto del **nuevo comunicado de Google sobre el
    sideloading de APKs**.

------------------------------------------------------------------------

## Contenidos

-   Historia de Android: de Android Inc. al primer smartphone comercial.
-   La pelea legal Java: Sun, Oracle y Google (Corte Suprema, 2021).
-   Java vs. Kotlin: por qué Kotlin es hoy el lenguaje oficial de Android.
-   Historia y evolución de Android Studio y el stack Android.
-   Gradle en Android: `.gradle` vs `.gradle.kts`.
-   Android SDK: componentes principales y su rol en el desarrollo.
-   Instalación de Android Studio y configuración del entorno.
-   Estructura del proyecto Android moderno.
-   Configuración de emuladores y dispositivos físicos.
-   Creación de un proyecto inicial con Jetpack Compose.
-   Últimas versiones de Android Studio y sus cambios.
-   APIs de Android: listado y criterios de selección.
-   Package Name como identificador único de la app.
-   Página oficial de desarrolladores de Android.
-   Restricciones recientes de Google sobre instalación de APKs.

------------------------------------------------------------------------

## Desarrollo de la sesión

### 0) Historia: de Android Inc. al Android/Kotlin de hoy

![El robot de Android, mascota del proyecto desde 2007](./img/00a-android-robot-logo.png)

#### 0.1 Los orígenes: Android Inc. (2003-2005)

-   **Android, Inc.** se funda en Palo Alto, California, en **octubre de
    2003**, por **Andy Rubin, Rich Miner, Nick Sears y Chris White**.
-   El plan original **no** era un sistema operativo para celulares:
    apuntaban a **cámaras digitales inteligentes**. Cuando ese mercado se
    desplomó, el equipo giró hacia los teléfonos móviles.
-   Con apenas 8 empleados y fondos casi agotados, **Google adquiere
    Android Inc. en agosto de 2005** por unos **50 millones de
    dólares**. Rubin y su equipo se quedan a cargo del proyecto dentro
    de Google.

[link referencia](https://en.wikipedia.org/wiki/Andy_Rubin)

#### 0.2 El primer Android: HTC Dream / T-Mobile G1 (2007-2008)

![HTC Dream / T-Mobile G1 (2008), el primer smartphone Android comercial](./img/00b-htc-dream-g1.jpg)

-   **Noviembre de 2007**: Google anuncia la **Open Handset Alliance**,
    un consorcio de fabricantes, operadoras y desarrolladores para
    construir un estándar de móviles abierto.
-   **Septiembre de 2008**: sale a la venta el **HTC Dream** (conocido
    como **T-Mobile G1** en EE. UU.), el primer teléfono comercial con
    **Android 1.0**.
-   Desde ese momento, cada versión mayor tuvo nombre de postre en orden
    alfabético — Cupcake, Donut, Eclair, Froyo, Gingerbread, Honeycomb,
    Ice Cream Sandwich, Jelly Bean, KitKat... — hasta que en **2019**
    Google simplificó al público a numeración directa (Android 10 en
    adelante), aunque **internamente** el sistema operativo sigue usando
    codinomes en orden alfabético (ej.: Android 16 = *Baklava*). Esto es
    **distinto** de los codinomes de **Android Studio** vistos en la
    sección 1.1 (Ladybug, Meerkat, Narwhal, Otter, Panda, Quail...), que
    versionan el IDE, no el sistema operativo.

[link referencia](https://en.wikipedia.org/wiki/HTC_Dream)

#### 0.3 La pelea por Java: Sun, Oracle y Google (2010-2021)

-   Desde el inicio, Android usa **Java como lenguaje** para sus APIs,
    pero corre sobre una máquina virtual propia (**Dalvik**, luego
    **ART**) y sobre **Apache Harmony** (una implementación libre de las
    APIs de Java) — no sobre la JVM oficial de Sun — para evitar
    depender de licencias propietarias.
-   **2010**: **Oracle compra Sun Microsystems** y hereda los derechos
    de Java. Meses después demanda a Google, alegando que las 37 APIs de
    Java usadas en Android infringen copyright y patentes.
-   El litigio se extiende **más de una década**, con fallos que van y
    vienen entre tribunales: 2012 (jurado no ve infracción de patentes),
    2014 (la Cámara de Apelaciones dice que las APIs sí son
    protegibles por copyright), 2016 (nuevo jurado determina que el uso
    de Google es *fair use*), 2018 (la Cámara de Apelaciones revierte
    otra vez a favor de Oracle).
-   **5 de abril de 2021**: la **Corte Suprema de EE. UU.** falla
    definitivamente a favor de Google — **6 votos contra 2**, caso
    *Google LLC v. Oracle America, Inc.* — determinando que copiar las
    APIs de Java para lograr interoperabilidad constituye **uso justo
    (fair use)**.
-   Resultado: Google gana tras **11 años de litigio**. Hoy Android usa
    su propia implementación basada en **OpenJDK**, ya no en Apache
    Harmony.

![Duke, la mascota de Java (originalmente de Sun Microsystems, hoy de Oracle)](./img/00d-java-duke-mascot.png)
![Corte Suprema de EE. UU., sede del fallo Google LLC v. Oracle America, Inc. (2021)](./img/00e-us-supreme-court.jpg)

[link referencia (caso)](https://en.wikipedia.org/wiki/Google_LLC_v._Oracle_America,_Inc.) ·
[link referencia (resumen oficial del fallo)](https://www.congress.gov/crs-product/LSB10597)

#### 0.4 De Java a Kotlin: el nuevo lenguaje oficial (2011-hoy)

![Logotipo de Kotlin (JetBrains)](./img/00c-kotlin-logo.png)

-   **Kotlin** nace en **JetBrains** (la empresa detrás de IntelliJ
    IDEA, la base de Android Studio): se anuncia en 2011 y llega a su
    versión estable **1.0** en **febrero de 2016**.
-   **Google I/O 2017**: Google declara a Kotlin **lenguaje oficialmente
    soportado** para desarrollo Android, junto a Java. Android Studio
    3.0 lo integra de fábrica, sin plugins adicionales.
-   **Google I/O 2019**: Google va más allá y declara el desarrollo
    Android **"Kotlin-first"** — las nuevas APIs de Jetpack se ofrecen
    primero en Kotlin, y la recomendación oficial para todo proyecto
    nuevo pasa a ser Kotlin, no Java.
-   Ventajas clave frente a Java: **null-safety** en el sistema de
    tipos, sintaxis más concisa, **coroutines** para asincronía,
    interoperabilidad 100% con código Java existente, y es la base de
    **Jetpack Compose** (el toolkit de UI moderno, exclusivo de Kotlin).
-   Hoy, **agosto de 2026**, Kotlin es el lenguaje por defecto de
    cualquier proyecto Android nuevo — este mismo curso lo usa desde
    esta Clase 1 (ver el `MainActivity.kt` real del proyecto en la
    sección 5.2).

[link referencia (anuncio 2017)](https://blog.jetbrains.com/kotlin/2017/05/kotlin-on-android-now-official/) ·
[link referencia (Kotlin-first, 2019)](https://techcrunch.com/2019/05/07/kotlin-is-now-googles-preferred-language-for-android-app-development/)

> Créditos de imágenes: robot de Android y Duke de Java, de dominio
> público / CC BY 3.0 vía Wikimedia Commons; logotipo de Kotlin,
> JetBrains vía Wikimedia Commons; fotos de HTC Dream/G1 y de la Corte
> Suprema de EE. UU., CC vía Wikimedia Commons. Todas enlazadas desde su
> página de origen en los "link referencia" de cada subsección.

------------------------------------------------------------------------

### 1) Introducción teórica: evolución del entorno Android

#### 1.1 Android Studio en el tiempo

-   **2013**: Google anuncia **Android Studio** en Google I/O como IDE
    oficial, reemplazando a Eclipse ADT.
-   Construido sobre **IntelliJ IDEA** (JetBrains), ofrece mejor
    integración con Gradle y tooling avanzado.
-   Evolución notable: integración de **Layout Editor**, soporte
    completo a **Jetpack Compose**, **Profiler** de rendimiento,
    integración con Firebase.

##### Últimas 5 versiones de Android Studio (agosto 2026)

1. **Quail 3 (2026.1.3)** – Versión estable actual.
2. **Quail 2 (2026.1.2)** – julio 2026.
3. **Quail 1 (2026.1.1)** – junio 2026.
4. **Panda 4 (2025.3.1)** – abril 2026.
5. **Otter 3 Feature Drop (2025.2.3)** – enero 2026.

[link referencia](https://developer.android.com/studio/releases/past-releases)

#### 1.2 El JDK y los problemas con Sun/Oracle

-   Resumen rápido: Android evitó depender del JDK propietario de Sun
    usando **Apache Harmony**; cuando Oracle compró Sun en 2010 demandó
    a Google por las APIs de Java, y la **Corte Suprema de EE. UU.
    falló a favor de Google en 2021** (*fair use*). Hoy Android usa
    **OpenJDK** como base.
-   Detalle completo del litigio, fechas y fuentes: ver la
    **sección 0.3, "La pelea por Java: Sun, Oracle y Google"**, más
    arriba en este documento.

#### 1.3 Gradle y su evolución

-   Android migró de **Ant** a **Gradle** como sistema de build
    flexible.
-   **Groovy DSL (`.gradle`)** fue el estándar inicial.
-   Con la popularidad de Kotlin, Google y JetBrains impulsaron **Kotlin
    DSL (`.gradle.kts`)**, que ofrece:
    -   **Tipado estático**.
    -   Autocompletado y chequeo de errores en tiempo de compilación.
    -   Más coherencia en proyectos que ya usan Kotlin.
-   Hoy, la recomendación es iniciar proyectos con **Gradle Kotlin
    DSL**.

#### 1.4 El Android SDK en detalle

El **Android SDK** es un conjunto de herramientas, librerías y APIs que
permiten compilar y ejecutar aplicaciones: - **Platform Tools**: incluye
`adb`, `fastboot`, comandos para depuración y despliegue. - **Build
Tools**: compila código y recursos en APK/Bundle (incluye `aapt2`,
`dx/d8`, `zipalign`). - **SDK Platforms**: define APIs para cada versión
de Android. Ej.: API 37 (Android 17). - **System Images**: imágenes del
sistema usadas en emuladores. - **Command-line Tools**: herramientas
para gestión del SDK sin Android Studio (`sdkmanager`, `avdmanager`). -
**NDK (Native Development Kit)**: opcional, permite usar C/C++ en
Android.

El SDK se actualiza constantemente, y es crítico mantener la versión del
**`compileSdk`** y **Build Tools** alineadas con la versión más reciente
estable.

##### Versiones API de Android y criterio de selección

-   Cada versión de Android está asociada a un número de **API Level**.
-   Ejemplos:
    -   Android 17 → API 37 (estable desde el 16-jun-2026).
    -   Android 16 → API 36.
    -   Android 15 → API 35.
    -   Android 14 → API 34.
    -   Android 13 → API 33.
-   **Criterios de selección**:
    -   `compileSdk`: usar siempre la última versión estable (hoy: 37).
    -   `targetSdk`: Google Play exige **API 36+ (Android 16)** en apps
        y actualizaciones nuevas a partir del **31-ago-2026** (extensión
        posible hasta el 1-nov-2026); las apps existentes deben tener
        como mínimo **API 35**.
    -   `minSdk`: definir según la base de usuarios a soportar. La
        cobertura activa cae con fuerza recién por debajo de API 30
        (~87%), por lo que un piso de **API 24-26** sigue siendo un
        estándar seguro y moderno para la mayoría de apps.

[link referencia (API levels)](https://developer.android.com/tools/releases/platforms) ·
[link referencia (requisito target API de Play)](https://developer.android.com/google/play/requirements/target-sdk) ·
[link referencia (distribución de versiones)](https://apilevels.com/)

#### 1.5 El Package Name como identificador único

-   El **package name** (ej.: `com.example.mycomposeapp`) es el
    **identificador único** de la aplicación en Android.
-   Funciona como **ID en Google Play** y en el sistema operativo.
-   No pueden existir dos apps con el mismo package en un mismo
    dispositivo.
-   Buenas prácticas:
    -   Usar dominio invertido (`com.empresa.proyecto`).
    -   Evitar nombres genéricos (`com.test.app`).
    -   No cambiarlo después de publicar en Google Play (se considera
        una app diferente).

#### 1.6 Página oficial de desarrolladores de Android

-   La fuente principal de información es la página oficial:
    **[developer.android.com](https://developer.android.com/)**.
-   Ofrece:
    -   **Documentación oficial** de todas las librerías y APIs.
    -   **Guías de inicio rápido** y tutoriales.
    -   Descargas de Android Studio y SDK.
    -   Sección de **Best Practices** y ejemplos de código.
    -   Novedades de cada versión de Android y Android Studio.
-   Es fundamental que los estudiantes se acostumbren a usar esta página
    como referencia diaria.

#### 1.7 Verificación de desarrolladores de Google: estado a agosto 2026

Google está implementando un cambio en las políticas de instalación de
aplicaciones en Android. Estado real del cronograma **hoy, 19 de agosto
de 2026**:

- Solo se podrán instalar sin fricción aplicaciones de desarrolladores
  **verificados por Google**; aplica tanto a **Play Store** como a
  **tiendas externas** o **APKs** instalados manualmente.
- La verificación solo confirma la **identidad del desarrollador** (no
  revisa el contenido de la app).
- Línea de tiempo real:
  - **Abril 2026** (cumplido): aparece el servicio en segundo plano
    "Android Developer Verifier" en los ajustes de Google System
    Services de los dispositivos.
  - **Junio 2026** (cumplido): acceso anticipado a cuentas de
    distribución limitada para estudiantes y hobbistas.
  - **Agosto 2026** (en curso): lanzamiento global de las cuentas de
    distribución limitada y del "flujo avanzado" para sideloading de
    apps no verificadas.
  - **30-sep-2026** (próxima, en ~6 semanas): entra en vigor de forma
    obligatoria en **Brasil, Indonesia, Singapur y Tailandia** — instalar
    apps de desarrolladores no verificados exigirá el flujo avanzado o
    ADB.
  - **2027 en adelante**: expansión global, incluyendo EE. UU.

**Motivo principal**: las apps instaladas fuera de la Play Store tienen
mayor probabilidad de contener malware. Google busca reducir el fraude y
mejorar la seguridad, sin eliminar el sideloading.

**Controversia**:
- Críticos señalan que limita la **apertura de Android**, haciéndolo más
  parecido a iOS.
- Preocupación por el impacto en **desarrolladores pequeños,
  investigadores y la comunidad de software libre**.

[link referencia](https://android-developers.googleblog.com/2026/03/android-developer-verification-rolling-out-to-all-developers.html)
  
------------------------------------------------------------------------

### 2) Instalación de Android Studio y configuración del SDK

#### 2.1 Descarga e instalación

-   Descarga Android Studio desde la página oficial de Android
    Developers.
-   Ejecuta el instalador (Windows, macOS o Linux) con opciones por
    defecto.
-   Android Studio incluye su propio **JBR (Java Runtime)**, por lo que
    no es necesario instalar JDK externo.

#### 2.2 Primer arranque y configuración del SDK Manager

1.  Abre **Android Studio**.
2.  Entra a **SDK Manager**:
    -   En macOS: `Android Studio > Preferences` → **Appearance &
        Behavior \> System Settings \> Android SDK**.
    -   En Windows/Linux: `File > Settings` → **Appearance & Behavior \>
        System Settings \> Android SDK**.
3.  Instala:
    -   **Android SDK Platform** (última versión estable).
    -   **Android SDK Platform-Tools**.
    -   **Android SDK Build-Tools**.
    -   **Android Emulator**.
    -   **System Image** adecuada para tu CPU (x86_64 o arm64).
    -   **Command-line Tools**.

#### 2.3 Variables de entorno

-   Configura `ANDROID_SDK_ROOT` y agrega `platform-tools` al `PATH`.

-   Ejemplo en Linux/macOS (`~/.zshrc`):

    ``` bash
    export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
    export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
    ```

-   Ejemplo en Windows (PowerShell):

    ``` powershell
    setx ANDROID_SDK_ROOT "C:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk" /M
    setx PATH "%ANDROID_SDK_ROOT%\\platform-tools;%PATH%" /M
    ```

-   Verifica con:

    ``` bash
    adb --version
    sdkmanager --list
    ```

------------------------------------------------------------------------

### 3) Estructura del proyecto Android moderno

Un proyecto Android se organiza en:

-   **`settings.gradle.kts`**: define módulos incluidos.
-   **`build.gradle.kts` (raíz)**: plugins, repositorios.
-   **`gradle.properties`**: propiedades globales.
-   **`app/build.gradle.kts`**: configuración del módulo `app`.
-   **`src/main`**: código fuente, `AndroidManifest.xml`, recursos
    (`res/*`).

Ejemplo de `app/build.gradle.kts` (versiones vigentes a agosto 2026, con
Version Catalog):

``` kotlin
// AGP 9+ trae soporte a Kotlin integrado: ya no se aplica el plugin
// org.jetbrains.kotlin.android por separado.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mycomposeapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.mycomposeapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

> Notas:
> - Con el plugin `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.0+) ya
>   no se declara `composeOptions { kotlinCompilerExtensionVersion }` —
>   el plugin gestiona la versión del compilador de Compose
>   automáticamente.
> - Con **AGP 9+** tampoco se aplica el plugin
>   `org.jetbrains.kotlin.android` — el soporte a Kotlin ya viene
>   integrado en el propio Android Gradle Plugin.

[link referencia (AGP/Gradle/JDK)](https://developer.android.com/build/releases/gradle-plugin) ·
[link referencia (Kotlin integrado en AGP 9)](https://developer.android.com/build/migrate-to-built-in-kotlin) ·
[link referencia (Compose BOM)](https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html) ·
[link referencia (Kotlin)](https://kotlinlang.org/docs/whatsnew24.html)

------------------------------------------------------------------------

### 4) Configuración de emuladores y dispositivos físicos

#### 4.1 Crear un AVD (emulador)

1.  Abre **Device Manager** en Android Studio.
2.  Clic en **Create Device** → selecciona un modelo (ej. Pixel 7).
3.  Descarga una **System Image** adecuada.
4.  Ajusta RAM y almacenamiento.
5.  Ejecuta el emulador.

#### 4.2 Configurar un dispositivo físico

1.  Activa **Opciones de desarrollador** y **Depuración USB** en el
    móvil.

2.  Conecta por USB y acepta el permiso RSA.

3.  Verifica con:

    ``` bash
    adb devices
    ```

4.  (Opcional) Conexión por WiFi:

    ``` bash
    adb tcpip 5555
    adb connect <ip_dispositivo>:5555
    ```

------------------------------------------------------------------------

### 5) Creación de un proyecto inicial con Jetpack Compose

#### 5.1 Crear el proyecto

1.  `File > New > New Project`.
2.  Selecciona **Empty Compose Activity**.
3.  Define nombre, paquete y SDK mínimo.
4.  Finaliza y espera sincronización.

#### 5.2 Código inicial

**MainActivity.kt** (código real del proyecto `M01Clase01` de este repositorio):

``` kotlin
package com.example.m01clase01

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.m01clase01.ui.theme.M01Clase01Theme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            M01Clase01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello Android!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Log.d(TAG, "Button clicked!")
            }
        ) {
            Text(text = "Tap me")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    M01Clase01Theme {
        HomeScreen()
    }
}
```

Ejecuta en emulador o dispositivo. Este es el `enableEdgeToEdge()` + `Scaffold` recomendado hoy (ver
[modulo-01/clase-01/M01Clase01/app/src/main/java/com/example/m01clase01/MainActivity.kt](./M01Clase01/app/src/main/java/com/example/m01clase01/MainActivity.kt)),
en vez del `Surface` manual usado en versiones previas de esta guía.

------------------------------------------------------------------------

## Ejercicios prácticos

1.  Investigar y preparar una línea de tiempo con las principales
    versiones de Android Studio y sus novedades.
2.  Diferenciar con ejemplos `.gradle` y `.gradle.kts` en un mismo
    proyecto.
3.  Listar con `sdkmanager --list` los componentes instalados y explicar
    el rol de cada uno.
4.  Modificar el `HomeScreen` de `MainActivity.kt` para agregar un
    `TextField` donde se ingrese un nombre, y reemplazar el texto fijo
    `"Hello Android!"` por un saludo (`"Hello, $nombre!"`) que se
    actualice en tiempo real mientras el usuario escribe.

------------------------------------------------------------------------

## Resolución de problemas comunes

-   **Gradle Sync lento**: aumentar memoria en `gradle.properties`.
-   **ADB no detecta dispositivo**: cambiar cable, ejecutar
    `adb kill-server && adb start-server`.
-   **Emulador lento**: usar imágenes correctas, asignar más RAM.
-   **Errores de versiones Compose/AGP**: alinear Compose Compiler con
    Kotlin.
