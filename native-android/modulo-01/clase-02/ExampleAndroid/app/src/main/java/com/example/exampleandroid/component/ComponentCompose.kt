package com.example.exampleandroid.component

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import androidx.compose.material3.Text
import android.content.res.Configuration


class ComponentCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppScreen()
            }
        }
    }
}

@Composable
private fun AppScreen() {
    Scaffold(
        topBar = {
            ToolbarCustom(
                title = "Title label",
                elevation = 0.dp,   // sin sombra, como en la web
                barInset = 0.dp     // barra 100% ancho
            )
        }
    ) { inner ->
        // Contenido bajo el toolbar (Scaffold ya maneja el padding top)
        DemoContent(modifier = Modifier.padding(inner))
    }
}

@Composable
private fun DemoContent(modifier: Modifier = Modifier) {

}

@Composable
fun ToolbarCustom(
    title: String,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    barHeight: Dp = 8.dp,
    elevation: Dp = 0.dp,
    barInset: Dp = 0.dp,
    centerTitle: Boolean = true,
    colorStart: Color = Color(0xFFF5B700),
    colorMiddle: Color = Color(0xFF18AEF5),
    colorEnd: Color = Color(0xFF4B007D),
    ratioStart: Float = 0.12f,
    ratioMiddle: Float = 0.10f,
    ratioEnd: Float = 0.78f,
) {

    val total = max(ratioStart + ratioMiddle + ratioEnd, 0.0001f)
    val w1 = ratioStart / total
    val w2 = ratioMiddle / total
    val w3 = ratioEnd / total

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        color = Color.White,
        shadowElevation = elevation,
        shape = RectangleShape
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF222222),
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 24.dp)
                    .then(
                        if (centerTitle) Modifier.fillMaxWidth() else Modifier
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = barInset)
                    .height(barHeight)
            ) {
                Box(Modifier.weight(w1).fillMaxHeight().background(colorStart))
                Box(Modifier.weight(w2).fillMaxHeight().background(colorMiddle))
                Box(Modifier.weight(w3).fillMaxHeight().background(colorEnd))
            }
        }
    }
}

@Preview(name = "Light", showBackground = true, widthDp = 360)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 360)
@Composable
private fun ToolbarCustomPreview() {
    MaterialTheme {
        Column(Modifier.fillMaxWidth()) {
            ToolbarCustom(
                title = "Title label",
                elevation = 0.dp,
                barInset = 0.dp
            )
        }
    }
}