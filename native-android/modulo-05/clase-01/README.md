# Módulo 05 · Sesión 01 — Animaciones con Jetpack Compose

## Objetivos de la sesión

Al finalizar esta sesión el estudiante será capaz de:

1. Comprender el uso de `animate*AsState` y `updateTransition` para animar estados simples y múltiples propiedades.
2. Implementar animaciones de entrada y salida de componentes usando `AnimatedVisibility`.
3. Crear animaciones personalizadas utilizando `Animatable`, `rememberInfiniteTransition` y distintos `AnimationSpec`.
4. Aplicar buenas prácticas de rendimiento al trabajar con animaciones en Compose.

## Contenido

1. Uso de `animate*AsState` y `updateTransition`.
2. Animaciones de entrada/salida con `AnimatedVisibility`.
3. Creación de animaciones personalizadas.
4. Optimización de rendimiento y buenas prácticas.

## Desarrollo de los temas

### 1) Uso de animate*AsState y updateTransition

#### 1.1. ¿Qué es `animate*AsState`?

`animate*AsState` es una familia de funciones que convierten un valor de estado “normal” en un valor animado. Cada vez que cambia el `targetValue`, Compose interpola el valor desde el actual al nuevo usando la animación que definamos.

Existen variantes como:

- `animateFloatAsState`
- `animateDpAsState`
- `animateColorAsState`
- `animateIntAsState`

Se usan cuando:

- Solo queremos animar **una propiedad**.
- El cambio se dispara cuando un estado cambia.
- Necesitamos una sintaxis sencilla.

#### Ejemplo:

```kotlin
@Composable
fun FavoriteButton() {
    var isFavorite by remember { mutableStateOf(false) }

    val size by animateDpAsState(
        targetValue = if (isFavorite) 56.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_size"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFavorite) Color.Red else Color.Gray,
        label = "favorite_color"
    )

    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor, CircleShape)
            .clickable { isFavorite = !isFavorite },
        contentAlignment = Alignment.Center
    ) {
        Text(text = if (isFavorite) "On" else "Off", color = Color.White)
    }
}
```

#### 1.2. ¿Qué es `updateTransition`?

Se usa cuando:

- Queremos animar **varias propiedades a la vez**.
- Necesitamos una transición coordinada.
- Trabajamos con estados complejos.

#### Ejemplo:

```kotlin
enum class CardState { Collapsed, Expanded }

@Composable
fun ExpandableCard() {
    var state by remember { mutableStateOf(CardState.Collapsed) }

    val transition = updateTransition(targetState = state, label = "card_transition")

    val cardHeight by transition.animateDp(label = "card_height") {
        if (it == CardState.Collapsed) 80.dp else 200.dp
    }

    val cardColor by transition.animateColor(label = "card_color") {
        if (it == CardState.Collapsed) Color(0xFFEEEEEE) else Color(0xFFBBDEFB)
    }

    val cornerRadius by transition.animateDp(label = "card_corner") {
        if (it == CardState.Collapsed) 8.dp else 24.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(16.dp)
            .clickable {
                state = if (state == CardState.Collapsed) CardState.Expanded else CardState.Collapsed
            },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = if (state == CardState.Collapsed) "Tap to expand" else "Expanded content")
        }
    }
}
```

### 2) Animaciones de entrada/salida con AnimatedVisibility

#### Ejemplo básico:

```kotlin
@Composable
fun FilterPanel() {
    var showFilters by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Button(onClick = { showFilters = !showFilters }) {
            Text(if (showFilters) "Hide filters" else "Show filters")
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text("Filter by status")
                Text("Filter by date")
                Text("Filter by type")
            }
        }
    }
}
```

### 3) Creación de animaciones personalizadas

#### 3.1. `Animatable`

Para animaciones controladas manualmente desde corrutinas.

```kotlin
@Composable
fun ShakingButton(text: String, onClick: () -> Unit, triggerShake: Boolean) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerShake) {
        if (triggerShake) {
            offsetX.animateTo(20f, tween(50))
            offsetX.animateTo(-20f, tween(50))
            offsetX.animateTo(0f, tween(50))
        }
    }

    Button(onClick = onClick, modifier = Modifier.offset(x = offsetX.value.dp)) {
        Text(text)
    }
}
```

#### 3.2. `rememberInfiniteTransition`

Para animaciones repetitivas como indicadores de carga.

```kotlin
@Composable
fun RecordingIndicator() {
    val infinite = rememberInfiniteTransition(label = "recording")

    val scale by infinite.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = LinearOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        Modifier
            .size(24.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Color.Red, CircleShape)
    )
}
```

### 4) Optimización y buenas prácticas

1. Evitar trabajo pesado en cada frame.
2. Recordar estados (`remember`).
3. Usar `LaunchedEffect` para controlar cuándo se inicia una animación.
4. Animar solo lo necesario.
5. Evitar anidar demasiados `AnimatedVisibility`.
6. Probar en dispositivos reales.
7. Mantener lógica de UI separada del dominio.

## Resumen

- `animate*AsState`: animaciones simples basadas en estado.
- `updateTransition`: animaciones coordinadas con múltiples propiedades.
- `AnimatedVisibility`: animación de entrada y salida de componentes.
- `Animatable` y `rememberInfiniteTransition`: animaciones avanzadas.
- Buenas prácticas para evitar jank y optimizar rendimiento.
