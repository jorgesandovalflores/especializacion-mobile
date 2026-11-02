package com.example.android.commons.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.core.presentation.theme.AndroidTheme

// Comentarios en español; código en inglés.
// value = solo dígitos (0..9) SIN prefijo. onValueChange devuelve solo dígitos (máx 9).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.onSurface
    )

    TextField(
        value = value,
        onValueChange = { raw ->
            // Acepta solo dígitos; si pegan "+51..." removemos todo lo que no sea dígito
            // y nos quedamos con los últimos 9 dígitos relevantes.
            val digits = raw.filter { it.isDigit() }
            val normalized = when {
                digits.startsWith("51") && digits.length > 9 -> digits.drop(2)
                digits.startsWith("51") -> digits.drop(2)
                else -> digits
            }.take(9)
            onValueChange(normalized)
        },
        placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        colors = tfColors,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),

        // Prefijo visible SOLO cuando hay contenido: "+51 " (con espacio)
        prefix = {
            if (value.isNotEmpty()) {
                Text(text = "+51 ")
            }
        }
    )
}

/* Previews */

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun PhoneInputFieldEmptyPreview() {
    var phone by remember { mutableStateOf("") } // sin prefijo, arranca vacío
    AndroidTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PhoneInputField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Ingresa tu número de teléfono"
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun PhoneInputFieldFilledPreview() {
    var phone by remember { mutableStateOf("987654321") } // 9 dígitos -> mostrará "+51 " como prefijo
    AndroidTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PhoneInputField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Ingresa tu número de teléfono"
            )
        }
    }
}
