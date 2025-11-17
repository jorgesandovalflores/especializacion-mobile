package com.example.demoanimation

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class BoxState {
    COLLAPSED,
    EXPANDED
}

@Composable
fun AnimationExample() {
    // Estado para controlar si la caja está expandida o colapsada
    var boxState by remember { mutableStateOf(BoxState.COLLAPSED) }

    // Estado para controlar el color del botón
    var isButtonActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ejemplo 1: Uso de updateTransition para animar múltiples propiedades
        BoxAnimationExample(boxState = boxState)

        Spacer(modifier = Modifier.height(32.dp))

        // Ejemplo 2: Uso individual de animate*AsState
        SinglePropertyAnimationExample(isActive = isButtonActive)

        Spacer(modifier = Modifier.height(32.dp))

        // Botones para controlar las animaciones
        Button(
            onClick = {
                boxState = if (boxState == BoxState.COLLAPSED) BoxState.EXPANDED else BoxState.COLLAPSED
            }
        ) {
            Text(text = if (boxState == BoxState.COLLAPSED) "Expand Box" else "Collapse Box")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isButtonActive = !isButtonActive }
        ) {
            Text(text = if (isButtonActive) "Deactivate" else "Activate")
        }
    }
}

@Composable
fun BoxAnimationExample(boxState: BoxState) {
    // Crear una transición que maneje múltiples animaciones
    val transition = updateTransition(targetState = boxState, label = "Box Transition")

    // Animaciones controladas por la transición
    val boxSize by transition.animateDp(
        transitionSpec = {
            tween(durationMillis = 500) // Duración de 500ms
        }, label = "Size Animation"
    ) { state ->
        when (state) {
            BoxState.COLLAPSED -> 100.dp
            BoxState.EXPANDED -> 200.dp
        }
    }

    val cornerRadius by transition.animateDp(
        transitionSpec = {
            tween(durationMillis = 500)
        }, label = "Corner Animation"
    ) { state ->
        when (state) {
            BoxState.COLLAPSED -> 8.dp
            BoxState.EXPANDED -> 25.dp
        }
    }

    val boxColor by transition.animateColor(
        transitionSpec = {
            tween(durationMillis = 400)
        }, label = "Color Animation"
    ) { state ->
        when (state) {
            BoxState.COLLAPSED -> Color(0xFF2196F3) // Azul
            BoxState.EXPANDED -> Color(0xFF4CAF50)  // Verde
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "updateTransition Example",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(boxSize)
                .background(
                    color = boxColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
        )

        Text(
            text = "Size: ${boxSize.value}dp",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SinglePropertyAnimationExample(isActive: Boolean) {
    // animateColorAsState - Cambia el color del botón
    val buttonColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFF9800) else Color(0xFF9E9E9E),
        animationSpec = tween(durationMillis = 300),
        label = "Button Color"
    )

    // animateDpAsState - Cambia la altura del botón
    val buttonHeight by animateDpAsState(
        targetValue = if (isActive) 60.dp else 48.dp,
        animationSpec = tween(durationMillis = 400),
        label = "Button Height"
    )

    // animateFloatAsState - Rota el ícono (simulado con texto)
    val rotation by animateFloatAsState(
        targetValue = if (isActive) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "Rotation"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "animate*AsState Examples",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón que muestra todas las animaciones individuales
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(buttonHeight)
                .background(
                    color = buttonColor,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Settings",
                modifier = Modifier.rotate(rotation),
                color = Color.White
            )
        }

        Text(
            text = "Status: ${if (isActive) "Active" else "Inactive"}",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnimationExamplePreview() {
    MaterialTheme {
        AnimationExample()
    }
}