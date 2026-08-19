# Tutorial paso a paso: Instalación de Android Studio + Guía de la interfaz

> **Última revisión:** 19 de agosto de 2026 · **Sistema operativo:** Windows / macOS / Linux  
> **Fuentes oficiales rápidas:** [Instalar Android Studio](https://developer.android.com/studio/install), [Conoce la UI](https://developer.android.com/studio/intro/user-interface), [Lanzamientos de Android Studio](https://developer.android.com/studio/releases), [Plataformas/API](https://developer.android.com/tools/releases/platforms)

---

## 0) Requisitos previos

- 8 GB de RAM (16 GB recomendado), 10–20 GB de espacio libre.
- Virtualización **habilitada** en BIOS/UEFI (para el emulador).
- Conexión a internet estable.
- **No necesitas instalar un JDK aparte**: Android Studio incluye **JetBrains Runtime (JBR)** basado en OpenJDK. Más adelante puedes cambiar el JDK si lo requieres.
  ![Requisitos del sistema](./img/00-requisitos.png)

---

## 1) Descarga Android Studio (estable)

1. Ve a **Download Android Studio** y descarga el instalador para tu sistema operativo.  
   ![Descargar Android Studio](./img/01-descargar.png)
2. Verifica que sea la **versión estable** más reciente (en agosto 2026, la familia **Quail (2026.1.x)**; la actual es **Quail 3**).  
   ![Página de lanzamientos](./img/01b-releases.png)

[link referencia](https://developer.android.com/studio/releases)

---

## 2) Instalación por sistema operativo

### 2.1 Windows (x64/ARM)

1. Ejecuta el instalador (`.exe`) como administrador.
2. Acepta los componentes por defecto (**Android SDK**, **Android SDK Platform**, **Android Virtual Device**).
3. Al finalizar, deja marcada la opción **Run Android Studio**.
4. En el **Setup Wizard**, elige **Standard** para una instalación guiada y descarga de componentes.
   ![Instalación en Windows](./img/02-windows-setup.png)

### 2.2 macOS (Intel/Apple Silicon)

1. Abre el `.dmg` y arrastra **Android Studio** a **Applications**.
2. Abre la app (puede pedir permiso por ser descargada de internet).
3. Sigue el **Setup Wizard** y confirma la instalación de componentes (incluye JBR).  
   ![Instalación en macOS](./img/02-macos-setup.png)

### 2.3 Linux (deb/rpm/zip)

1. Extrae o instala el paquete según tu distro.
2. Otorga permisos de ejecución al script de inicio si usas el `.zip`:
    ```bash
    chmod +x bin/studio.sh
    ./bin/studio.sh
    ```
3. Sigue el **Setup Wizard**.

> `![Instalación en Linux](./img/02-linux-setup.png)`

> **Rutas típicas del SDK**
>
> - **Windows:** `C:\Users\<usuario>\AppData\Local\Android\Sdk`
> - **macOS:** `/Users/<usuario>/Library/Android/sdk`
> - **Linux:** `/home/<usuario>/Android/Sdk`  
>   ![Ubicación del SDK](./img/02c-sdk-location.png)

---

## 3) Primer arranque y componentes esenciales

Durante el **Setup Wizard**:

- Elige **Standard** (o **Custom** si ajustarás RAM del emulador).
- Selecciona un **tema** (Light/Dark).
- Descarga **Platform Tools** (adb), **Build Tools**, **SDK Platform** más reciente y **Android Virtual Device**.

Luego valida en **File ▸ Settings/Preferences ▸ Appearance & Behavior ▸ System Settings ▸ Android SDK**:

- **SDK Platforms:** marca la plataforma más reciente (**API level actual**).
- **SDK Tools:** deja activado _Android SDK Platform-Tools_, _Android SDK Build-Tools_, _Android Emulator_ y _Android SDK Command-line Tools_.
  ![SDK Manager - Platforms](./img/03-sdk-platforms.png)
  ![SDK Manager - Tools](./img/03-sdk-tools.png)

**Verificación rápida por terminal:**

```bash
adb --version
sdkmanager --list | head -n 30
```

---

## 4) Emulador y aceleración por hardware

1. Abre **Device Manager** ▸ **Create device** y elige un perfil (p. ej., Pixel 8).
2. Descarga una **System Image** (recomendada: la más reciente con Google APIs).
3. Activa aceleración:
    - **Windows:** **WHPX/Hyper-V** (o WSL2) según documentación.
    - **macOS:** **Hypervisor Framework** (Apple Silicon usa este por defecto).
    - **Linux:** KVM.
4. Ajusta **RAM/VRAM** según tu equipo.

> ![Crear AVD](./img/04-avd-create.png)
> ![Aceleración del emulador](./img/04b-accel.png)

---

## 5) Crea tu primer proyecto (Compose recomendado)

1. **New Project** ▸ plantilla **Empty Activity (Compose)**.
2. Define: **Application name**, **Package name** (único), **Save location**, **Language: Kotlin**, **Minimum SDK**.
3. Ejecuta con **Run ▶** en un dispositivo físico (USB) o tu **AVD**.

> ![Proyecto Compose con su preview](./img/05-new-project.png)
> ![App corriendo](./img/05b-running.png)

---

## 6) Secciones importantes de Android Studio (UI)

- **Welcome Screen:** crear/abrir proyectos, recientes, SDK Manager.  
  ![Welcome Screen](./img/06-welcome.png)
- **Barra superior y Run/Debug Configurations:** iniciar, depurar, seleccionar dispositivo.  
  ![Run/Debug](./img/06b-run-configs.png)
- **Tool Windows clave:**
    - **Project** (estructura de módulos).
    - **Gradle** (tareas y sincronización).
    - **Build** (salida de compilación, Build Analyzer).
    - **Logcat** (logs filtrados por app, nivel, proceso).
    - **Device Manager** (AVD).
    - **App Inspection** (DB Inspector, Network Inspector).
    - **Profiler** (CPU, memoria, energía).
    - **Layout Inspector** / **Compose Layout Inspector**.
    - **Device File Explorer**.
    - **Version Control** (Git).
    - **Terminal** integrado.
      ![Tool Windows](./img/06c-tool-windows.png)

- **Editor:** autocompletado, inspecciones, refactors, _intention actions_.

> Documentación: Conoce la interfaz de Android Studio.

---

## 7) Gradle: `.gradle` vs `.gradle.kts` (Kotlin DSL)

- Puedes escribir los scripts con **Groovy** (`build.gradle`) o **Kotlin DSL** (`build.gradle.kts`).
- **Recomendado:** Kotlin DSL por mejor autocompletado, chequeos de tipos y refactors más seguros.
- Si migras, sigue la guía oficial y el _primer_ paso suele ser renombrar archivos a `.gradle.kts` y ajustar el bloque `plugins {}`.

**Recursos:**

- Guía “Migrate your build configuration from Groovy to Kotlin (DSL)”.
- “Gradle build overview | Configuration DSLs”.

---

## 8) Estructura de proyecto (carpetas clave)

- **app/src/main/AndroidManifest.xml** (declaraciones de permisos, actividades, `applicationId` en Gradle).
- **app/build.gradle(.kts)** (configuración Android, dependencias).
- **gradle/libs.versions.toml** (opcional: Version Catalog).
- **src/main/java|kotlin** (código), **src/main/res** (recursos), **src/androidTest** / **test** (tests).

> ![Estructura del proyecto](./img/08-project-structure.png)

---

## 9) Niveles de API (API level) y cómo elegir `minSdk`/`targetSdk`

- **compileSdk**: usa el **API level más reciente** disponible (para acceder a las APIs nuevas).
- **targetSdk**: también el más reciente soportado por Play en el momento (cumplimiento de comportamientos y políticas).
- **minSdk**: decide según tu audiencia, librerías y hardware mínimo. Hoy, muchas librerías exigen **21+**; evalúa métricas de usuarios y dependencias.

### Tabla rápida (actualizada al 19-ago-2026)

| Versión Android           | API level | Estado                | Nota                                                                                                                                                                |
| ------------------------- | --------: | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android 17 "Cinnamon Bun" |    **37** | Estable (16-jun-2026) | Última plataforma estable.                                                                                                                                          |
| Android 16                |    **36** | Estable               | **Play**: nuevas apps y actualizaciones deben **target 36+** desde **31-ago-2026** (extensión posible hasta 1-nov-2026; Wear OS/XR: 34+, TV: 33+, Automotive: 32+). |
| Android 15                |    **35** | Estable               | Apps existentes deben mantener al menos target 35 para seguir visibles a usuarios nuevos en OS más recientes.                                                       |

> ![API levels](./img/09-api-levels.png)

[link referencia (plataformas/API)](https://developer.android.com/tools/releases/platforms) ·
[link referencia (requisito target API de Play)](https://developer.android.com/google/play/requirements/target-sdk)

---

## 10) `applicationId` (package de la app) debe ser **único**

- En Gradle (**defaultConfig ▸ applicationId**) defines el **identificador único** de tu app (p. ej., `com.empresa.app`).
- Este **no puede repetirse en Play** para otra app y es **independiente** del `package` de clases del código.
- **Consejo:** fija el `applicationId` temprano; cambiarlo luego implica migraciones (firmas, enlaces, deep links, etc.).

> ![applicationId único](./img/10-applicationid.png)

**Referencia:** documentación de _applicationId_ y herramientas de firma/empaquetado.

---

## 11) Actualizaciones del IDE y canales

- **Stable** (recomendado), **Beta**, **Canary/Preview**.
- Actualiza desde **Help ▸ Check for Updates** o desde el panel **Updates**.
- Revisa notas de versión por familia (_Quail, Panda, Otter, …_) para cambios de AGP/Gradle.

[link referencia](https://developer.android.com/studio/releases/past-releases)

> ![Updates](./img/11-updates.png)

---

## 12) Recursos oficiales y atajos útiles

- **Página oficial de desarrolladores:** https://developer.android.com (en español disponible).
- **Instalar Android Studio:** guía paso a paso.
- **Conoce la UI de Android Studio:** tour de ventanas y atajos.
- **Command-line tools:** `adb`, `sdkmanager`, `avdmanager`, `apksigner`, etc.
- **SDK Platforms:** notas por versión (API 36/35/34…).
- **Política Play – Target API:** fechas y requisitos vigentes.

> ![Bookmarks de documentación](./img/12-links.png)

---

## 13) Anexos

### A. Detección y cambio de JDK del IDE

- **File ▸ Settings/Preferences ▸ Build, Execution, Deployment ▸ Build Tools ▸ Gradle ▸ Gradle JDK**.
- Puedes usar el JBR incluido o apuntar a otro JDK instalado.

> ![Gradle JDK](./img/13-gradle-jdk.png)

### B. Comandos útiles

```bash
# Listar paquetes instalados y disponibles
sdkmanager --list

# AVDs existentes
avdmanager list avd

# Instalar una plataforma y build-tools específicas
sdkmanager "platforms;android-37" "build-tools;36.0.0"
```

[link referencia](https://developer.android.com/build/releases/gradle-plugin)

### C. Problemas comunes (y pistas)

- **Emulador lento o no arranca** → revisar aceleración (Hyper-V/WHPX/Hypervisor/KVM) y asignación de RAM.
- **Gradle Sync falla** → comprobar versiones de AGP/Gradle, proxy/firewall, JDK seleccionado.
- **No aparece un dispositivo físico** → activar _Developer options_ y **USB debugging**; drivers OEM (Windows).
- **Errores con firmas** → confirmar keystore, contraseñas y `signingConfigs`.
