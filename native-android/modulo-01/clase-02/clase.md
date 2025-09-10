# Sesión 2 - Fundamentos de Jetpack Compose

## Objetivos de aprendizaje

Al finalizar la sesión, el estudiante será capaz de:
1. Declarar UI con funciones `@Composable` y comprender su ciclo de vida. 
2. Diseñar jerarquías de componentes y explicar la **recomposición automática**. 
3. Aplicar **Material Design 3** y usar **Previews** efectivamente. 
4. Diseñar **componentes reutilizables** (API, `Modifier`, state hoisting y
slots). 
5. Comparar Compose con **XML** y con **componentes web**
(Vue/Angular) para tomar decisiones de arquitectura.

## Contenido de la clase

1.  Declaración de UI con `@Composable`.
2.  Jerarquía de componentes y recomposición automática.
3.  Material Design y uso de Previews.
4.  Creación de componentes reutilizables.
5.  Datos adicionales (interop, rendimiento, testing, pitfalls,
    theming).

------------------------------------------------------------------------

## 1) Declaración de UI con funciones `@Composable`

### Teoría

-   **Compose (declarativo):** describes *qué* dibujar para un
    **estado** dado. Cuando el estado cambia, la función se
    **recompone** y actualiza la UI afectada.
-   **XML (imperativo):** inflas layouts (`.xml`) y **mutas** vistas
    (`setText`, `setVisibility`) desde Kotlin/Java.
-   **Analogía web (Vue/Angular):** componentes que reciben
    **props/inputs** y **emiten eventos**, renderizando en función del
    estado reactivo.

### Ejemplos equivalentes

#### A. Vue

``` vue
<template>
    <div class="card">
        <h2>Hello {{ name }}!</h2>
        <button @click="$emit('tap')">Tap me</button>
    </div>
</template>
<script setup lang="ts">
interface Props { name: string }
defineProps<Props>()
defineEmits<{ (e: 'tap'): void }>()
</script>
```

#### B. Android clásico (XML + Kotlin)

``` xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:padding="16dp"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <TextView
        android:id="@+id/tvTitle"
        android:text="Hello World!"
        android:textSize="22sp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
    <Button
        android:id="@+id/btnTap"
        android:text="Tap me"
        android:layout_marginTop="8dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
</LinearLayout>
```

``` kotlin
class GreetingCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_greeting_card)
        val title = findViewById<TextView>(R.id.tvTitle)
        val btn = findViewById<Button>(R.id.btnTap)
        title.text = "Hello Compose!"
        btn.setOnClickListener { Log.d("Session02", "Tap") }
    }
}
```

#### C. Compose

``` kotlin
@Composable
fun GreetingCard(name: String, onTap: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Hello $name!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onTap) { Text("Tap me") }
        }
    }
}
```

------------------------------------------------------------------------

## 2) Jerarquía de componentes y recomposición automática

### Teoría

-   **Jerarquía:** `Column`, `Row`, `Box` componen una UI anidada
    (árbol) como `LinearLayout/ConstraintLayout` en XML.
-   **Recomposición:** cuando cambia un **estado leído** por un
    composable, Compose vuelve a ejecutar **ese** composable y los hijos
    necesarios.

### Ejemplos equivalentes

#### A. Vue --- Jerarquía + estado reactivo

``` vue
<template>
    <div class="panel">
        <p>Count: {{ count }}</p>
        <div class="row">
            <button @click="inc">Increment</button>
            <button @click="reset">Reset</button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
const count = ref(0)
const inc = () => count.value++
const reset = () => (count.value = 0)
</script>
```

#### B. Android clásico --- RecyclerView (esqueleto)

``` kotlin
class UserAdapter : RecyclerView.Adapter<VH>() {
    private val data = mutableListOf<String>()
    fun submit(items: List<String>) {
        data.clear(); data.addAll(items)
        notifyDataSetChanged()
    }
    /* onCreateViewHolder / onBindViewHolder ... */
}
```

#### C. Compose --- Jerarquía + recomposición

``` kotlin
@Composable
fun CounterPanel(modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(0) }
    Column(modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Count: $count", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { count++ }) { Text("Increment") }
            OutlinedButton(onClick = { count = 0 }) { Text("Reset") }
        }
    }
}
```

------------------------------------------------------------------------

## 3) Aplicación de Material Design y uso de Previews

### Teoría

-   **Compose + Material 3:**
    `MaterialTheme(colorScheme, typography, shapes)` + componentes
    (`Card`, `Button`, `TopAppBar`...).
-   **Previews:** `@Preview` para ver composables **sin** ejecutar la
    app.

### Ejemplos equivalentes

#### A. Vue --- Theming simple con CSS variables

``` vue
<template>
    <button class="primary-btn"><slot>Action</slot></button>
</template>
<script setup lang="ts"></script>
<style scoped>
.primary-btn { background: var(--color-primary, #0066cc); color: white; }
</style>
```

#### B. Android clásico --- styles.xml + layout

``` xml
<style name="AppTheme" parent="Theme.MaterialComponents.DayNight">
    <item name="colorPrimary">#0066CC</item>
</style>
```

#### C. Compose --- Material 3 + Previews

``` kotlin
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(primary = Color(0xFF0066CC))
    MaterialTheme(colorScheme = scheme, typography = Typography()) { content() }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun UserCardPreviews() {
    AppTheme { UserCard("Jane Doe", "Android Engineer") }
}
```

------------------------------------------------------------------------

## 4) Creación de componentes reutilizables

### Teoría

-   **API limpia:** parámetros bien nombrados,
    `modifier: Modifier = Modifier`, callbacks (`onClick`).
-   **State hoisting:** el componente recibe `value` y `onValueChange`.

### Ejemplos equivalentes

#### A. Vue --- Botón con slot e icono opcional

``` vue
<template>
    <button class="primary" @click="$emit('click')">
        <span v-if="icon" class="mr-2">{{ icon }}</span>
        <slot>Continue</slot>
    </button>
</template>
<script setup lang="ts">
interface Props { icon?: string }
defineProps<Props>()
defineEmits<{ (e:'click'): void }>()
</script>
```

#### B. Android clásico --- Custom View con atributos

``` xml
<declare-styleable name="PrimaryButtonView">
    <attr name="text" format="string"/>
</declare-styleable>
```

#### C. Compose --- Composable con slots y `Modifier`

``` kotlin
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(48.dp)) {
        if (leading != null) { leading(); Spacer(Modifier.width(8.dp)) }
        content()
    }
}
```

------------------------------------------------------------------------

# Datos adicionales

-   **Interoperabilidad:** `ComposeView` dentro de XML y `AndroidView`
    en Compose.
-   **Rendimiento:** usar `remember`, `rememberSaveable`,
    `derivedStateOf`, claves estables en listas.
-   **Ciclo de efectos:** `LaunchedEffect`, `DisposableEffect`,
    `rememberUpdatedState`.
-   **Testing:** Compose Testing (`createAndroidComposeRule`) vs
    Espresso en XML.
-   **Migración:** empezar por pantallas nuevas o componentes aislados.
