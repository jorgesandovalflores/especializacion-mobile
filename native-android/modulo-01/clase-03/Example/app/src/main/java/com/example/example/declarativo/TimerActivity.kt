package com.example.example.declarativo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class TimerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    TimerScreen()
                }
            }
        }
    }
}

@Composable
fun TimerScreen() {
    var seconds by rememberSaveable { mutableStateOf(0) }

    // Arranca la corrutina cuando el composable entra al árbol
    LaunchedEffect(Unit) {
        Log.d("TimerScreen", "Composición creada → Timer iniciado")
        while (true) {
            delay(1000)
            seconds++
        }
    }

    // Cleanup explícito (se ejecuta cuando este composable sale del árbol)
    DisposableEffect(Unit) {
        onDispose {
            Log.d("TimerScreen", "Composición destruida → Timer cancelado automáticamente")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Seconds: $seconds",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
