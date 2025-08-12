# auth_sample_flutter — Splash & Login (Flutter)

Proyecto base en **Flutter** con dos pantallas minimalistas (**Splash** y **Login**) y **Material 3**. Mismo look & feel en Android e iOS.

---

## 1) Estructura del proyecto

```
auth_sample_flutter/
├─ lib/
│  ├─ main.dart                   # Punto de entrada: rutas '/' y '/login'
│  ├─ theme/
│  │  └─ app_theme.dart           # Paleta y ThemeData (Material 3)
│  └─ screens/
│     ├─ splash_page.dart         # Splash (Timer 1500ms → Login)
│     └─ login_page.dart          # Login (email, password, acciones)
├─ android/                       # Proyecto Android (Gradle)
├─ ios/                           # Proyecto iOS (Xcode/CocoaPods)
├─ pubspec.yaml                   # Dependencias Flutter
└─ README.md                      # Este archivo
```

### 1.1. Rutas y navegación
- `/` → `SplashPage`
- `/login` → `LoginPage`

### 1.2. Tokens de diseño
- **Colores**: `primary #6750A4`, `onPrimary #FFFFFF`, `background #FFFFFF`, `onBackground #1C1B1F`, `fieldBg #F5F5F7`, `outline #D9D9E3`
- **Tipografía**: título 28 semibold, cuerpo 16 regular
- **Espaciados**: padding 24, radio 12

---

## 2) Requisitos

- **Flutter stable 3.x**
- **Android Studio** con SDK/emulador (o dispositivo Android con depuración USB)
- **Xcode** con simulador iOS (o iPhone físico)
- **CocoaPods** para iOS (`brew install cocoapods` o `sudo gem install cocoapods`)

Verifica tu entorno:
```bash
flutter doctor -v
```

---

## 3) Setup inicial

```bash
flutter clean
flutter pub get
flutter precache --ios
# Si es la primera vez en iOS:
cd ios && pod install && cd -
```

---

## 4) Ejecutar en Android

### 4.1. Emulador
1. Android Studio → Device Manager → crea/inicia AVD.
2. Ver dispositivos:
    ```bash
    flutter devices
    ```
3. Ejecutar:
    ```bash
    flutter run -d android
    ```

### 4.2. Dispositivo físico
1. Activa **Depuración por USB** en el teléfono.
2. Conecta por USB y acepta la huella RSA.
3. Ejecuta:
    ```bash
    flutter run -d android
    ```

---

## 5) Ejecutar en iOS

### 5.1. Simulador iOS
1. Xcode → Settings → **Platforms/Components** → instala un simulador.
2. Abre el simulador:
    ```bash
    open -a Simulator
    flutter devices
    ```
3. Ejecuta:
    ```bash
    flutter run -d ios
    ```

### 5.2. iPhone físico (primera vez por proyecto)
1. Conecta el iPhone por USB y **confía** en el Mac.
2. **Developer Mode** ON: Settings → Privacy & Security → Developer Mode.
3. Abre el proyecto iOS en Xcode:
    ```bash
    open ios/Runner.xcworkspace
    ```
4. Target **Runner** → **Signing & Capabilities**:
   - Elige tu **Team** (Apple ID).
   - Cambia el **Bundle Identifier** a algo único (por ej. `pe.identity.authsample`).
5. Cierra Xcode y corre con el **UDID** del iPhone:
    ```bash
    flutter devices
    flutter run -d <UDID_DEL_IPHONE>
    ```
6. Si usas cuenta gratuita, confía el developer en iPhone:
   Settings → General → VPN & Device Management → **Developer App (Trust)**.

---

## 6) Comandos útiles

```bash
# Formatear código
flutter format .

# Hot restart en sesión de 'flutter run': r / R
# Detener: Ctrl + C

# Builds
flutter build apk            # Android (debug)
flutter build appbundle      # Android (release, Play Store)
flutter build ios            # iOS (debug)
flutter clean                # limpiar
```

---

## 7) Troubleshooting rápido

- **No aparece un dispositivo**:
  - Android: inicia un AVD; revisa `adb devices`.
  - iOS: instala simulador o conecta el iPhone por USB la primera vez.
- **CocoaPods falla**:
  - `sudo gem install cocoapods` o `brew install cocoapods`
  - `cd ios && pod repo update && pod install`
- **Firmas iOS**: configura Team en `ios/Runner.xcworkspace`.
