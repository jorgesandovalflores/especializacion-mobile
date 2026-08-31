package com.example.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.example.calculator.CalcFormatter
import com.example.example.calculator.CalcResult
import com.example.example.calculator.Operation
import com.example.example.calculator.Operator
import com.example.example.calculator.calculate
import com.example.example.calculator.formatted
import com.example.example.calculator.historySummary
import com.example.example.calculator.saveOperationRemotely
import com.example.example.calculator.trimHistory
import com.example.example.ui.theme.ExampleTheme
import kotlinx.coroutines.launch

/**
 * Calculadora funcional que sirve de ejemplo para la clase de Kotlin
 * (native-android/modulo-01/clase-adicional/README.md). La UI es a
 * propósito muy básica: dos campos, botones de operador y un resultado.
 * Los 10 puntos de la clase están implementados en el código —no impresos
 * en pantalla— con un comentario que indica a qué sección corresponden;
 * la lógica vive en el paquete `calculator/` para poder leerla aparte de
 * la UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // el contenido dibuja detrás de las barras del sistema...
        setContent {
            ExampleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // ...por eso safeDrawingPadding() respeta status bar / nav bar / notch.
                    CalculatorScreen(modifier = Modifier.safeDrawingPadding())
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    // Sección 1 del README.md — var: estado mutable de los inputs (String,
    // porque el usuario puede escribir texto inválido; se valida en calculate()).
    var aText by remember { mutableStateOf("") }
    var bText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    // Sección 7 del README.md — lista mutable que respalda el historial.
    val history = remember { mutableStateListOf<Operation>() }

    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var savedCount by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Calculadora Kotlin", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = aText,
            onValueChange = { aText = it },
            label = { Text("Número A") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = bText,
            onValueChange = { bText = it },
            label = { Text("Número B") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        // Sección 4 del README.md — un botón por cada valor del enum Operator.
        Row(modifier = Modifier.fillMaxWidth()) {
            Operator.entries.forEach { operator ->
                Button(
                    onClick = {
                        // Sección 3/4/8 del README.md: ver calculate() en CalculatorLogic.kt
                        result = calculate(aText, bText, operator)
                    },
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text(operator.symbol)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Sección 6 del README.md — smart cast: dentro de cada rama "is",
        // Kotlin ya sabe si "result" es Ok o Error, sin castear a mano.
        when (val r = result) {
            null -> Unit
            is CalcResult.Ok -> {
                Text(
                    "Resultado: ${r.operation.result.formatted()}",
                    style = MaterialTheme.typography.titleMedium,
                )
                // Sección 10 del README.md — CalcFormatter.percent(), objeto con @JvmStatic.
                if (r.operation.a != 0.0) {
                    Text("B es el ${CalcFormatter.percent(r.operation.b, r.operation.a)} de A")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = !saving,
                    onClick = {
                        // Sección 9 del README.md — launch: fire-and-forget, no bloquea la UI
                        // mientras la función suspend saveOperationRemotely() "viaja a la red".
                        scope.launch {
                            saving = true
                            val ok = saveOperationRemotely(r.operation)
                            if (ok) {
                                history.add(r.operation)
                                val trimmed = trimHistory(history.toList())
                                history.clear()
                                history.addAll(trimmed)
                                savedCount++
                            }
                            saving = false
                        }
                    },
                ) {
                    Text(if (saving) "Guardando..." else "Guardar en servidor")
                }
            }
            is CalcResult.Error -> {
                Text(r.message, color = MaterialTheme.colorScheme.error)
            }
        }

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Historial guardado ($savedCount)", style = MaterialTheme.typography.titleMedium)
            Text(historySummary(history)) // Sección 7 del README.md
        }
    }
}
