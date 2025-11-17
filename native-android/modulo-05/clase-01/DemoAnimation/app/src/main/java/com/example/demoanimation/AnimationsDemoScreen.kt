package com.example.demoanimation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnimationsDemoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Animations Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        // 1) Ejemplo con animate*AsState
        SectionCard(title = "1. FavoriteButton con animate*AsState") {
            FavoriteButtonDemo()
        }

        // 2) Ejemplo con updateTransition
        SectionCard(title = "2. Tarjeta expandible con updateTransition") {
            ExpandableCardDemo()
        }

        // 3) Ejemplo con AnimatedVisibility
        SectionCard(title = "3. Panel de filtros con AnimatedVisibility") {
            FilterPanelDemo()
        }

        // 4) Ejemplo con Animatable e infiniteTransition
        SectionCard(title = "4. Indicador de grabación y botón que tiembla") {
            RecordingAndShakeDemo()
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
fun FavoriteButtonDemo() {
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
        targetValue = if (isFavorite) Color(0xFFFF5252) else Color(0xFFBDBDBD),
        label = "favorite_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(backgroundColor, shape = CircleShape)
                .clickable { isFavorite = !isFavorite },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFavorite) "On" else "Off",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = if (isFavorite) "Este ítem está en favoritos" else "Pulsa para marcar como favorito",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

enum class CardState {
    Collapsed,
    Expanded
}

@Composable
fun ExpandableCardDemo() {
    var state by remember { mutableStateOf(CardState.Collapsed) }

    val transition = updateTransition(
        targetState = state,
        label = "card_transition"
    )

    val cardHeight by transition.animateDp (label = "card_height") { target ->
        when (target) {
            CardState.Collapsed -> 80.dp
            CardState.Expanded -> 200.dp
        }
    }

    val cardColor by transition.animateColor(label = "card_color") { target ->
        when (target) {
            CardState.Collapsed -> Color(0xFFF5F5F5)
            CardState.Expanded -> Color(0xFFE3F2FD)
        }
    }

    val cornerRadius by transition.animateDp(label = "card_corner") { target ->
        when (target) {
            CardState.Collapsed -> 8.dp
            CardState.Expanded -> 24.dp
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable {
                state = if (state == CardState.Collapsed) {
                    CardState.Expanded
                } else {
                    CardState.Collapsed
                }
            },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        if (state == CardState.Collapsed) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Detalle de pedido (toca para ver más)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pedido #12345",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Cliente: Juan Pérez")
                Text(text = "Total: S/ 45.90")
                Text(text = "Estado: En camino")
            }
        }
    }
}

@Composable
fun FilterPanelDemo() {
    var showFilters by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button (
            onClick = { showFilters = !showFilters }
        ) {
            Text(text = if (showFilters) "Ocultar filtros" else "Mostrar filtros")
        }

        AnimatedVisibility (
            visible = showFilters,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically ()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filtros de búsqueda",
                    style = MaterialTheme.typography.titleSmall
                )
                FilterItem(label = "Estado: Activo / Inactivo")
                FilterItem(label = "Fecha: Hoy / Últimos 7 días")
                FilterItem(label = "Tipo: Todos / Favoritos")
            }
        }
    }
}

@Composable
fun FilterItem(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Text(text = "Configurar", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun RecordingAndShakeDemo() {
    var isRecording by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRecording) {
                RecordingIndicator()
            }
            Text(
                text = if (isRecording) "Grabando mensaje de voz" else "Listo para grabar",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    isRecording = !isRecording
                    if (isRecording) {
                        showError = false
                    }
                }
            ) {
                Text(text = if (isRecording) "Detener" else "Iniciar grabación")
            }

            ShakingButton(
                text = "Enviar",
                onClick = {
                    if (!isRecording) {
                        showError = true
                    } else {
                        showError = false
                    }
                },
                triggerShake = showError
            )
        }

        if (showError) {
            Text(
                text = "No puedes enviar sin grabar un mensaje",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_transition")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween (
                durationMillis = 800,
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_alpha"
    )

    Box(
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(Color.Red, shape = CircleShape)
    )
}

@Composable
fun ShakingButton(
    text: String,
    onClick: () -> Unit,
    triggerShake: Boolean
) {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(triggerShake) {
        if (triggerShake) {
            offsetX.animateTo(
                targetValue = 12f,
                animationSpec = tween(durationMillis = 50)
            )
            offsetX.animateTo(
                targetValue = -12f,
                animationSpec = tween(durationMillis = 50)
            )
            offsetX.animateTo(
                targetValue = 8f,
                animationSpec = tween(durationMillis = 50)
            )
            offsetX.animateTo(
                targetValue = -8f,
                animationSpec = tween(durationMillis = 50)
            )
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 50)
            )
        }
    }

    Button(
        onClick = onClick,
        modifier = Modifier.offset(x = offsetX.value.dp)
    ) {
        Text(text = text)
    }
}

/* ===========================
   PREVIEWS
   =========================== */
// Preview de la pantalla completa
@Preview(showBackground = true)
@Composable
fun AnimationsDemoScreenPreview() {
    MaterialTheme {
        Surface {
            AnimationsDemoScreen()
        }
    }
}

// Preview para FavoriteButtonDemo
@Preview(showBackground = true)
@Composable
fun FavoriteButtonDemoPreview() {
    MaterialTheme {
        Surface {
            FavoriteButtonDemo()
        }
    }
}

// Preview para ExpandableCardDemo
@Preview(showBackground = true)
@Composable
fun ExpandableCardDemoPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ExpandableCardDemo()
        }
    }
}

// Preview para FilterPanelDemo
@Preview(showBackground = true)
@Composable
fun FilterPanelDemoPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            FilterPanelDemo()
        }
    }
}

// Preview para RecordingAndShakeDemo
@Preview(showBackground = true)
@Composable
fun RecordingAndShakeDemoPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RecordingAndShakeDemo()
        }
    }
}

// Preview para SectionCard con contenido de ejemplo
@Preview(showBackground = true)
@Composable
fun SectionCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SectionCard(title = "Sección de prueba") {
                Text(text = "Contenido de ejemplo dentro de SectionCard")
            }
        }
    }
}

// Preview para FilterItem
@Preview(showBackground = true)
@Composable
fun FilterItemPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            FilterItem(label = "Estado: Activo / Inactivo")
        }
    }
}

// Preview para RecordingIndicator
@Preview(showBackground = true)
@Composable
fun RecordingIndicatorPreview() {
    MaterialTheme {
        Surface (modifier = Modifier.padding(16.dp)) {
            RecordingIndicator()
        }
    }
}

// Preview para ShakingButton (sin trigger de shake inicial)
@Preview(showBackground = true)
@Composable
fun ShakingButtonPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ShakingButton(
                text = "Enviar",
                onClick = {},
                triggerShake = false
            )
        }
    }
}