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

### 1) Uso de animate\*AsState y updateTransition

#### 1.1. ¿Qué es `animate*AsState`?

`animate*AsState` es una familia de funciones que convierten un valor de estado “normal” en un valor animado. Cada vez que cambia el `targetValue`, Compose interpola el valor desde el actual al nuevo usando la animación que definamos.

Existen variantes como:

-   `animateFloatAsState`
-   `animateDpAsState`
-   `animateColorAsState`
-   `animateIntAsState`

Se usan cuando:

-   Solo queremos animar **una propiedad**.
-   El cambio se dispara cuando un estado cambia.
-   Necesitamos una sintaxis sencilla.

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

---

#### 1.2. ¿Qué es `updateTransition`?

Se usa cuando:

-   Queremos animar **varias propiedades a la vez**.
-   Necesitamos una transición coordinada.
-   Trabajamos con estados complejos.

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

---

#### 1.3. Comparativo con animaciones en XML (View system clásico)

Antes de Jetpack Compose, muchas animaciones se definían en XML y se aplicaban sobre vistas tradicionales (`View`). Para lograr algo similar a los ejemplos anteriores se usaban:

-   Archivos XML en `res/anim/` o `res/animator/`.
-   Clases como `Animation`, `ObjectAnimator`, `AnimatorSet`.
-   Código imperativo para encontrar la vista y disparar la animación.

---

### 1.3.1. Animar una sola propiedad (ejemplo tipo `FavoriteButton`)

Animación XML:

```xml
<!-- res/anim/scale_up.xml -->
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:fillAfter="true">

    <scale
        android:fromXScale="1.0"
        android:toXScale="1.4"
        android:fromYScale="1.0"
        android:toYScale="1.4"
        android:pivotX="50%"
        android:pivotY="50%"
        android:duration="200" />

</set>
```

En código:

```kotlin
val favoriteView = findViewById<View>(R.id.favoriteButton)
val animation = AnimationUtils.loadAnimation(this, R.anim.scale_up)
favoriteView.startAnimation(animation)
```

Cambio de color por separado:

```kotlin
favoriteView.setBackgroundColor(
    if (isFavorite) Color.RED else Color.GRAY
)
```

---

### 1.3.2. Animar múltiples propiedades coordinadas (tipo `ExpandableCard`)

XML:

```xml
<!-- res/animator/card_expand.xml -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <objectAnimator
        android:propertyName="cardElevation"
        android:valueType="floatType"
        android:valueFrom="2"
        android:valueTo="8"
        android:duration="300" />
    <objectAnimator
        android:propertyName="alpha"
        android:valueType="floatType"
        android:valueFrom="0.8"
        android:valueTo="1.0"
        android:duration="300" />
</set>
```

En código:

```kotlin
val cardView = findViewById<CardView>(R.id.myCardView)
val animator = AnimatorInflater.loadAnimator(this, R.animator.card_expand)
animator.setTarget(cardView)
animator.start()
```

Para altura, color o radio de esquina, había que agregar más animadores o manejar cambios manuales.

---

### 1.3.3. Resumen comparativo

| Aspecto        | XML (View System)                     | Jetpack Compose                         |
| -------------- | ------------------------------------- | --------------------------------------- |
| Declaración    | XML + código                          | Solo Kotlin declarativo                 |
| Estado         | Cambios manuales y animación separada | Estado reactivo que dispara animaciones |
| Complejidad    | Más archivos y coordinación manual    | Menos código, más claro                 |
| Previews       | Requiere dispositivo/emulador         | `@Preview` directo en IDE               |
| Sincronización | Fácil desincronización                | Automática con `targetState`            |

### 2) Animaciones de entrada/salida con AnimatedVisibility

`AnimatedVisibility` permite animar la aparición o desaparición de un composable. Es equivalente a combinar `alpha`, `scale`, `translate`, `height/width` y más, en una sola API declarativa.

Compose maneja automáticamente:

-   Medición
-   Posicionamiento
-   Coordinación de animaciones
-   Eliminación del elemento cuando deja de ser visible

---

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

---

## 2.1. Comparativo: cómo se hacía antes con XML (View System)

Antes de Compose, implementar animaciones de entrada/salida requería una combinación de:

-   Archivos XML en `res/anim/`
-   `AnimationUtils.loadAnimation()`
-   Manejo manual de `visibility` (`View.VISIBLE` / `View.GONE`)
-   Callbacks para sincronizar animación con el estado real

`AnimatedVisibility` reemplaza todo eso en **una sola API declarativa**.

---

### 2.1.1. Ejemplo equivalente en XML (fade + expand)

#### Animaciones XML:

**fade_in.xml**

```xml
<alpha xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromAlpha="0"
    android:toAlpha="1"
    android:duration="200"/>
```

**fade_out.xml**

```xml
<alpha xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromAlpha="1"
    android:toAlpha="0"
    android:duration="200"/>
```

**expand_in.xml**

```xml
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromYScale="0"
    android:toYScale="1"
    android:pivotY="0%"
    android:duration="200"/>
```

**shrink_out.xml**

```xml
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromYScale="1"
    android:toYScale="0"
    android:pivotY="0%"
    android:duration="200"/>
```

---

### 2.1.2. Y en código Java/Kotlin:

```kotlin
val panel = findViewById<View>(R.id.filterPanel)

val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
val expand = AnimationUtils.loadAnimation(this, R.anim.expand_in)
val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
val shrink = AnimationUtils.loadAnimation(this, R.anim.shrink_out)

button.setOnClickListener {
    if (panel.visibility == View.GONE) {
        panel.visibility = View.VISIBLE
        panel.startAnimation(fadeIn)
        panel.startAnimation(expand)
    } else {
        val animationSet = AnimationSet(true).apply {
            addAnimation(fadeOut)
            addAnimation(shrink)
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation) {
                    panel.visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationStart(animation: Animation) {}
            })
        }
        panel.startAnimation(animationSet)
    }
}
```

---

### 2.1.3. Resumen comparativo

| Aspecto                        | XML (View System)            | Jetpack Compose             |
| ------------------------------ | ---------------------------- | --------------------------- |
| Archivos necesarios            | 2–4 animaciones XML + código | Solo Kotlin                 |
| Control de visibilidad         | Manual y propenso a errores  | Automático según `visible`  |
| Medición/tamaño                | No cambia sin layout extra   | Natural con `expand/shrink` |
| Sincronización salida/ocultado | Listeners manuales           | Interno y automático        |
| Estilo                         | Imperativo                   | Declarativo                 |
| Complejidad                    | Alta                         | Baja                        |

### 3) Creación de animaciones personalizadas

Las APIs como `animate*AsState` o `AnimatedVisibility` cubren muchos casos comunes, pero a veces necesitamos un **control total** sobre el tiempo, la secuencia y el disparo de la animación. Para eso Compose ofrece herramientas de bajo nivel como `Animatable` y `rememberInfiniteTransition`.

---

#### 3.1. `Animatable`

Para animaciones controladas manualmente desde corrutinas. Permite:

-   Controlar exactamente **cuándo** empieza la animación.
-   Ejecutar varias animaciones en secuencia.
-   Cancelar o reiniciar según lógica propia.

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

En este ejemplo:

-   `offsetX` es el valor animado.
-   Cada `animateTo()` es un segmento de la animación (ida, vuelta, regreso al centro).
-   `LaunchedEffect(triggerShake)` se dispara cuando cambia `triggerShake`.

---

#### 3.2. `rememberInfiniteTransition`

Para animaciones repetitivas como indicadores de carga, respiración, parpadeo, etc. Se mantiene activa mientras el composable está en composición.

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

Aquí:

-   Se crea una transición infinita con `rememberInfiniteTransition`.
-   `animateFloat` oscila entre `0.8f` y `1.2f` de forma indefinida.
-   La escala se aplica a través de `graphicsLayer`.

---

### 3.3. Comparativo con el sistema clásico (XML + ValueAnimator/ObjectAnimator)

Antes de Compose, las animaciones personalizadas se construían con:

-   `ValueAnimator` para animar valores primitivos (float, int).
-   `ObjectAnimator` para propiedades de vistas (`translationX`, `alpha`, `scaleX`, etc.).
-   `AnimatorSet` para componer animaciones en secuencia o en paralelo.
-   XML en `res/animator/` o código directamente.

Esto implicaba:

-   Más código imperativo.
-   Callbacks para aplicar los valores en cada frame.
-   Manejo manual del ciclo de vida del `Animator` (cancel, start, end).

---

### 3.3.1. Equivalente clásico de `ShakingButton` (temblor) con `ValueAnimator`

En el sistema de vistas, un botón que “tiembla” ante un error se podía implementar así:

```kotlin
val button = findViewById<Button>(R.id.button)

fun shakeButton() {
    val animator = ValueAnimator.ofFloat(0f, 20f, -20f, 0f).apply {
        duration = 150L
        addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            button.translationX = value
        }
    }
    animator.start()
}
```

Y en el `onClick` o al detectar error:

```kotlin
button.setOnClickListener {
    // Lógica de click...
    shakeButton()
}
```

Diferencias clave respecto a `Animatable`:

-   Con `ValueAnimator` trabajas con callbacks (`addUpdateListener`) y aplicas tú mismo el valor a la vista.
-   No hay integración directa con corutinas.
-   Debes preocuparte por cancelar animaciones si la vista se destruye.

Con `Animatable`:

-   El valor animado (`offsetX`) está ligado al ciclo de vida del composable.
-   La animación se ejecuta dentro de una corrutina (`LaunchedEffect`).
-   No se necesitan listeners manuales, solo se lee `offsetX.value`.

---

### 3.3.2. Equivalente clásico de `RecordingIndicator` (bombeo infinito)

Antes, un indicador “bombeando” se hacía así:

```kotlin
val circleView = findViewById<View>(R.id.recordingIndicator)

val scaleXAnimator = ObjectAnimator.ofFloat(circleView, "scaleX", 0.8f, 1.2f).apply {
    duration = 800L
    repeatMode = ValueAnimator.REVERSE
    repeatCount = ValueAnimator.INFINITE
}

val scaleYAnimator = ObjectAnimator.ofFloat(circleView, "scaleY", 0.8f, 1.2f).apply {
    duration = 800L
    repeatMode = ValueAnimator.REVERSE
    repeatCount = ValueAnimator.INFINITE
}

val animatorSet = AnimatorSet().apply {
    playTogether(scaleXAnimator, scaleYAnimator)
}

fun startRecordingAnimation() {
    animatorSet.start()
}

fun stopRecordingAnimation() {
    animatorSet.cancel()
}
```

Problemas comunes:

-   Hay que coordinar `start()` y `cancel()` según el ciclo de vida (onResume, onPause, etc.).
-   Si se olvida cancelar, puede haber fugas de memoria.
-   Es más código de configuración para cada animación infinita.

Con `rememberInfiniteTransition`:

-   La animación existe mientras el composable está en el árbol.
-   Cuando se deja de componer, la animación se limpia automáticamente.
-   Solo describes el estado (`initialValue`, `targetValue`, `RepeatMode`) y Compose se encarga del resto.

---

### 3.3.3. Resumen comparativo

| Aspecto               | XML / ValueAnimator / ObjectAnimator      | Jetpack Compose (`Animatable` / `rememberInfiniteTransition`)    |
| --------------------- | ----------------------------------------- | ---------------------------------------------------------------- |
| Tipo de API           | Imperativa (creas y manejas animadores)   | Declarativa (describes estados y transiciones)                   |
| Aplicación de valores | `addUpdateListener` y setters en la vista | Se leen directamente (`offsetX.value`, `scale`) en el composable |
| Ciclo de vida         | Debes cancelar y limpiar manualmente      | Integrado con el ciclo de vida de composición                    |
| Secuencias            | `AnimatorSet` y listeners                 | Secuencia natural con `animateTo` en corutinas                   |
| Animaciones infinitas | `repeatCount = INFINITE` y manejo manual  | `rememberInfiniteTransition` se encarga automáticamente          |
| Legibilidad           | Código más verboso y disgregado           | Código más compacto, junto a la UI que afecta                    |

En resumen, `Animatable` y `rememberInfiniteTransition` permiten construir animaciones personalizadas con **menos código**, más legibles y mejor integradas al modelo declarativo de Compose, reemplazando gran parte de lo que antes hacíamos con `ValueAnimator`, `ObjectAnimator` y `AnimatorSet`.

### 4) Optimización y buenas prácticas

1. Evitar trabajo pesado en cada frame.
2. Recordar estados (`remember`).
3. Usar `LaunchedEffect` para controlar cuándo se inicia una animación.
4. Animar solo lo necesario.
5. Evitar anidar demasiados `AnimatedVisibility`.
6. Probar en dispositivos reales.
7. Mantener lógica de UI separada del dominio.

## Resumen

-   `animate*AsState`: animaciones simples basadas en estado.
-   `updateTransition`: animaciones coordinadas con múltiples propiedades.
-   `AnimatedVisibility`: animación de entrada y salida de componentes.
-   `Animatable` y `rememberInfiniteTransition`: animaciones avanzadas.
-   Buenas prácticas para evitar jank y optimizar rendimiento.
