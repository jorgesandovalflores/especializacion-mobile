# Guía Completa de Implementación de Accesibilidad y TalkBack en Android Compose

## 1. Fundamentos: qué es accesibilidad y TalkBack

### 1.1 ¿Qué es accesibilidad?

Accesibilidad es el conjunto de prácticas que permiten que una aplicación móvil sea usable por personas con distintas capacidades:

-   Personas con discapacidad visual total o parcial.
-   Personas con dificultades motoras (no pueden tocar con precisión).
-   Personas con dificultades cognitivas (necesitan información clara y simple).

Android provee herramientas como:

-   TalkBack (lector de pantalla)
-   Ajustes de tamaño de fuente
-   Alto contraste
-   Animaciones reducidas, entre otras

El objetivo es que **tu UI sea comprensible sin necesidad de verla**.

---

### 1.2 ¿Qué necesita TalkBack de tu app?

TalkBack no “ve” la interfaz, sino que interpreta:

-   **contentDescription**: qué es este elemento.
-   **Rol**: botón, imagen, texto, encabezado.
-   **Estado**: activado, error, seleccionado.
-   **Acciones**: clic, deslizar, expandir.
-   **Orden de navegación**: secuencia en la que se recorren los nodos.

Tus componentes Compose deben exponer esta información correctamente.

---

## 2. Reglas básicas de accesibilidad en Jetpack Compose

Aplica estas reglas en toda tu app:

1. **Imágenes importantes** → deben tener `contentDescription`.
2. **Imágenes decorativas** → usar `contentDescription = null`.
3. **Elementos interactivos** → tamaño mínimo **48dp**.
4. **Botones** → texto claro que describa la acción.
5. **Campos de texto** → etiquetas claras y errores accesibles.
6. **Switches o toggles** → usar `stateDescription`.
7. **Tarjetas o ítems complejos** → usar `semantics` o `clearAndSetSemantics`.
8. **Orden de lectura** → debe seguir el orden visual.
9. **Errores y estados** → usar `error()` o incluirlos en la descripción.
10. **Evitar duplicar información** → no repetir textos en nodos hijos.

---

## 3. Ejemplos paso a paso

A continuación, ejemplos simples y funcionales de accesibilidad.

---

### 3.1 Botón accesible simple

```kotlin
@Composable
fun CloseIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Cerrar pantalla" },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null
        )
    }
}
```

Puntos clave:

-   `contentDescription` claro.
-   Tamaño táctil mínimo.

---

### 3.2 Imagen accesible con descripción

```kotlin
@Composable
fun HeaderIllustration() {
    Image(
        painter = painterResource(id = R.drawable.img_taxi_header),
        contentDescription = "Ilustración de un taxi amarillo en movimiento",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentScale = ContentScale.Crop
    )
}
```

Si la imagen es decorativa:

```kotlin
contentDescription = null
```

---

### 3.3 Tarjeta accesible (ítem de menú)

```kotlin
@Composable
fun MenuCard(title: String, description: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics { contentDescription = "$title. $description" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title)
                Text(description)
            }
        }
    }
}
```

Para exponer solo una semantic node:

```kotlin
.clearAndSetSemantics { contentDescription = "$title. $description" }
```

---

### 3.4 Campo de texto con error accesible

```kotlin
@Composable
fun PhoneFieldWithError(phone: String, onPhoneChange: (String) -> Unit, errorMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Número de teléfono"
                if (errorMessage != null) error(errorMessage)
            }
    ) {
        Text("Número de teléfono")
        PhoneInputField(value = phone, onValueChange = onPhoneChange)
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
```

`error()` permite que TalkBack lea el error automáticamente.

---

### 3.5 Toggle accesible (switch)

```kotlin
@Composable
fun RememberDeviceToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Recordar este dispositivo"
                stateDescription = if (checked) "Activado" else "Desactivado"
            }
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Recordar este dispositivo", modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
```

TalkBack leerá:

> “Recordar este dispositivo, activado”.

---

## 4. Cómo probar accesibilidad y TalkBack

### 4.1 Activar TalkBack

1. Ir a **Ajustes > Accesibilidad > TalkBack**.
2. Activarlo.
3. Navega usando gestos de TalkBack.

---

### 4.2 Qué revisar

✔ ¿El orden de navegación sigue el orden visual?  
✔ ¿Cada elemento se entiende sin verlo?  
✔ ¿Los botones son descriptivos?  
✔ ¿Los switches indican su estado?  
✔ ¿Los campos leen sus errores?  
✔ ¿No se repite información innecesaria?

---

### 4.3 Usar Accessibility Scanner

Instala “Accessibility Scanner” de Google para detectar:

-   Bajo contraste
-   Tamaños táctiles pequeños
-   Etiquetas faltantes
-   Problemas de agrupación

---

## 5. Checklist final para tu app

-   [ ] Todas las imágenes importantes tienen descripción.
-   [ ] Las imágenes decorativas tienen `contentDescription = null`.
-   [ ] Todos los botones y elementos táctiles tienen mínimo **48dp**.
-   [ ] Los textos describen claramente la acción.
-   [ ] Los campos de texto tienen label clara.
-   [ ] Los errores usan `error()` o están en la semantic.
-   [ ] Los toggles usan `stateDescription`.
-   [ ] Las tarjetas complejas usan `semantics` o `clearAndSetSemantics`.
-   [ ] La navegación con TalkBack es coherente.
-   [ ] Probado con Accessibility Scanner.
