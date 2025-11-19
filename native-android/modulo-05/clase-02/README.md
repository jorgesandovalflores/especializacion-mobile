# Módulo 05 · Sesión 02 — Diseño Adaptativo y Accesibilidad en Jetpack Compose

## Objetivos de la sesión

Al finalizar la sesión el estudiante será capaz de:

1. Diseñar interfaces flexibles para pantallas múltiples, plegables y grandes con Jetpack Compose.
2. Crear UI responsivas usando `BoxWithConstraints`, `Modifier` y breakpoints personalizados.
3. Implementar principios de accesibilidad: contraste, etiquetas de contenido, TalkBack, tamaños táctiles y roles semánticos.
4. Integrar modo oscuro, colores dinámicos y soporte para localización en Compose.

---

## Contenido

1. Soporte para pantallas múltiples y plegables
2. UI responsiva con `BoxWithConstraints` y `Modifier`
3. Accesibilidad: contrastes, etiquetas y TalkBack
4. Soporte para modo oscuro y localización

---

# Desarrollo del contenido

---

# 1) Soporte para pantallas múltiples y plegables

## Contexto del caso de uso

Imaginemos una pantalla de **detalle del viaje** en la app de taxi del pasajero:

-   En teléfonos compactos: solo mostramos mapa + datos esenciales.
-   En pantallas grandes: mostramos mapa, datos del viaje, datos del conductor, historial y botones adicionales.
-   En dispositivos plegables: al abrir “tipo libro” se divide en paneles:
    -   Panel 1: mapa
    -   Panel 2: detalles del conductor

Este escenario requiere UI adaptativa.

---

## 1.1 Clasificación de tamaños según Google

-   Teléfonos: < 600dp
-   Teléfonos grandes: 600dp–840dp
-   Tablets: > 840dp

Esto nos permite ofrecer experiencias avanzadas en tablets y plegables.

---

## 1.2 Ejemplo: diseño adaptativo simple

```kotlin
@Composable
fun AdaptiveTravelScreen() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        if (maxWidth < 600.dp) {
            TravelScreenPhone()
        } else if (maxWidth < 840.dp) {
            TravelScreenLargePhone()
        } else {
            TravelScreenTablet()
        }
    }
}
```

---

## 1.3 Dispositivos plegables

```kotlin
@Composable
fun FoldAwareTravelScreen() {
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo(context)

    when {
        windowInfo.isBookPosture -> DualPaneTravelScreen()
        windowInfo.isSeparating -> DualPaneTravelScreen()
        else -> TravelScreenPhone()
    }
}
```

---

# 2) UI responsiva con BoxWithConstraints y Modifier

## Contexto del caso de uso

Tarjeta de información del conductor cuando se asigna un taxi:

-   Pantallas pequeñas: diseño vertical
-   Pantallas grandes: diseño horizontal
-   Tablets: más datos mostrados

---

## 2.1 Ejemplo real

```kotlin
@Composable
fun DriverInfoCardResponsive() {
    BoxWithConstraints(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (maxWidth > 500.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DriverPhoto()
                Spacer(Modifier.width(16.dp))
                DriverDetails()
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DriverPhoto()
                DriverDetails()
            }
        }
    }
}
```

---

## 2.2 Responsividad con Modifier

```kotlin
Modifier.padding(
    horizontal = if (isTablet) 48.dp else 16.dp,
    vertical = 16.dp
)
```

---

# 3) Accesibilidad: contrastes, etiquetas y TalkBack

## Contexto del caso de uso

En una app de taxi es clave:

-   Usuarios con baja visión
-   Lectores de pantalla
-   Botones táctiles grandes
-   Información clara del conductor

---

## 3.1 Botón accesible “Solicitar taxi”

```kotlin
@Composable
fun AccessibleRequestTaxiButton() {
    Button(
        onClick = { },
        modifier = Modifier
            .size(56.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Botón para solicitar un taxi"
            }
    ) {
        Text("Solicitar")
    }
}
```

---

## 3.2 Imagen accesible

```kotlin
Image(
    painter = painterResource(R.drawable.driver_photo),
    contentDescription = "Foto del conductor asignado"
)
```

---

## 3.3 Acción accesible con onClickLabel

```kotlin
Modifier.clickable(
    onClickLabel = "Abrir menú de opciones del viaje",
    onClick = { /* acción */ }
)
```

---

# 4) Soporte para modo oscuro y localización

## Contexto del caso de uso

En una app de taxi:

-   Conductores trabajan de noche → modo oscuro obligatorio
-   Pasajeros pueden ser extranjeros → localización necesaria

---

## 4.1 Modo oscuro dinámico

```kotlin
MaterialTheme(
    colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
)
```

---

## 4.2 Localización con stringResource

```kotlin
Text(text = stringResource(id = R.string.driver_arriving))
```

---

## 4.3 Plurales para ETA

```kotlin
Text(
    text = pluralStringResource(R.plurals.minutes_eta, etaMinutes, etaMinutes)
)
```

---

## 4.4 Soporte RTL

```kotlin
CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl
) {
    TravelScreenPhone()
}
```

---

# Cierre de la sesión

En esta clase aprendimos a:

-   Adaptar UI a múltiples tamaños y dispositivos plegables
-   Construir composables responsivos
-   Implementar accesibilidad con TalkBack
-   Integrar modo oscuro y localización
