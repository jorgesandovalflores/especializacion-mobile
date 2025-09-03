# Sesión 1 - Configuración del entorno y estructura del proyecto (Android + Jetpack Compose)

> Curso: **Especialización en Desarrollo Móvil --- Android/Kotlin**\
> Módulo 1 · Sesión 1

------------------------------------------------------------------------

## Objetivos de aprendizaje

Al finalizar esta sesión, el estudiante será capaz de:

1.  Comprender la evolución de **Android Studio**, el rol del **JDK** y
    los acuerdos históricos entre Sun, Oracle y Google.
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

-   Historia y evolución de Android Studio y el stack Android.
-   Java, el JDK y los conflictos legales entre Sun, Oracle y Google.
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

### 1) Introducción teórica: evolución del entorno Android

#### 1.1 Android Studio en el tiempo

-   **2013**: Google anuncia **Android Studio** en Google I/O como IDE
    oficial, reemplazando a Eclipse ADT.
-   Construido sobre **IntelliJ IDEA** (JetBrains), ofrece mejor
    integración con Gradle y tooling avanzado.
-   Evolución notable: integración de **Layout Editor**, soporte
    completo a **Jetpack Compose**, **Profiler** de rendimiento,
    integración con Firebase.

##### Últimas 5 versiones de Android Studio

1. **Ladybug (2024.2.1)** – Integración avanzada con Gemini AI y mejoras
   en Compose Preview.
2. **Koala (2024.1.1)** – Herramientas para desarrollo en dispositivos
   plegables y mejoras en inspección de Layout.
3. **Iguana (2023.2.1)** – Mejoras en sincronización Gradle y soporte
   para Kotlin multiplatform.
4. **Hedgehog (2023.1.1)** – Nueva interfaz de memoria en Profiler y
   mejoras en el editor de Compose.
5. **Giraffe (2022.3.1)** – Rediseño del editor de dependencias y
   mejoras en la gestión de dispositivos virtuales.

#### 1.2 El JDK y los problemas con Sun/Oracle

-   Inicialmente, Android utilizaba **Apache Harmony**, una
    implementación libre del JDK.
-   Google evitó usar directamente el JDK de Sun para no depender de
    licencias propietarias.
-   En 2010, **Oracle adquiere Sun Microsystems** y demanda a Google por
    uso de APIs de Java en Android.
-   Tras más de una década de juicios (Oracle vs. Google), la **Corte
    Suprema de EE.UU. en 2021 falló a favor de Google**, indicando que
    el uso de APIs de Java en Android constituía *fair use*.
-   Resultado: hoy Android usa su propia implementación, con **OpenJDK**
    como base.

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
de Android. Ej.: API 35 (Android 15). - **System Images**: imágenes del
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
    -   Android 15 → API 35.
    -   Android 14 → API 34.
    -   Android 13 → API 33.
    -   Android 12 → API 31/32.
    -   Android 11 → API 30.
    -   Android 10 → API 29.
-   **Criterios de selección**:
    -   `compileSdk`: usar siempre la última versión estable.
    -   `targetSdk`: definir la versión en la que la app está probada y
        optimizada (mínimo recomendado: N-1).
    -   `minSdk`: definir según la base de usuarios que deseas soportar
        (ej. minSdk 24 = Android 7 Nougat, todavía con usuarios en
        mercado emergente).

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

#### 1.7 Comunicado de Google: restricción al sideloading de APKs

En **agosto 2025**, Google anunció un cambio importante en las políticas
de instalación de aplicaciones en Android:

- A partir de **2026**, solo se podrán instalar aplicaciones en
  dispositivos Android si pertenecen a desarrolladores **verificados por
  Google**.
- Aplica tanto para apps distribuidas en **Play Store**, como en
  **tiendas externas** o instaladas manualmente como **APKs**.
- La verificación solo confirma la **identidad del desarrollador** (no
  revisa el contenido de la app).
- Línea de tiempo:
  - **Octubre 2025**: acceso anticipado para algunos desarrolladores.
  - **Marzo 2026**: verificación abierta a todos.
  - **Septiembre 2026**: obligatoria en Brasil, Indonesia, Singapur y
    Tailandia.
  - **2027 en adelante**: implementación global.

**Motivo principal**: las apps instaladas fuera de la Play Store tienen
**50 veces más probabilidades** de contener malware. Google busca
reducir el fraude y mejorar la seguridad.

**Controversia**:
- Críticos señalan que limita la **apertura de Android**, haciéndolo más
  parecido a iOS.
- Preocupación por el impacto en **desarrolladores pequeños,
  investigadores y la comunidad de software libre**.
  
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

Ejemplo de `app/build.gradle.kts`:

``` kotlin
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.mycomposeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mycomposeapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

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

**MainActivity.kt**:

``` kotlin
package com.example.mycomposeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot() {
    Surface(color = MaterialTheme.colorScheme.background) {
        GreetingCounter("Android")
    }
}

@Composable
fun GreetingCounter(name: String) {
    var count by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Hello, $name!")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Clicks: $count")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { count++ }) {
                Text("Tap me")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGreetingCounter() {
    AppRoot()
}
```

Ejecuta en emulador o dispositivo.

------------------------------------------------------------------------

## Ejercicios prácticos

1.  Investigar y preparar una línea de tiempo con las principales
    versiones de Android Studio y sus novedades.
2.  Diferenciar con ejemplos `.gradle` y `.gradle.kts` en un mismo
    proyecto.
3.  Listar con `sdkmanager --list` los componentes instalados y explicar
    el rol de cada uno.
4.  Modificar el `GreetingCounter` para que el nombre se ingrese en
    tiempo real desde un `TextField`.

------------------------------------------------------------------------

## Resolución de problemas comunes

-   **Gradle Sync lento**: aumentar memoria en `gradle.properties`.
-   **ADB no detecta dispositivo**: cambiar cable, ejecutar
    `adb kill-server && adb start-server`.
-   **Emulador lento**: usar imágenes correctas, asignar más RAM.
-   **Errores de versiones Compose/AGP**: alinear Compose Compiler con
    Kotlin.
