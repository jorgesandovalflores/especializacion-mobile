package com.example.android.commons.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    imeAction: ImeAction = ImeAction.Next
) {
    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = Color.Black
    )

    TextField(
        value = value,
        onValueChange = { newValue ->
            // Aplicar maxLength si está especificado
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        placeholder = { Text(text = placeholder, color = Color(0xFF9AA0A6)) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = tfColors,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

/* Previews */

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun GenericInputFieldTextPreview() {
    var text by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        GenericInputField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Ingresa tu nombre",
            keyboardType = KeyboardType.Text,
            maxLength = 50
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun GenericInputFieldEmailPreview() {
    var email by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        GenericInputField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Ingresa tu correo",
            keyboardType = KeyboardType.Email
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun GenericInputFieldMaxLengthPreview() {
    var text by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        GenericInputField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Máximo 10 caracteres",
            keyboardType = KeyboardType.Text,
            maxLength = 10
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun GenericInputFieldFilledPreview() {
    var text by remember { mutableStateOf("Texto de ejemplo") }
    Box(modifier = Modifier.padding(16.dp)) {
        GenericInputField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Campo con texto",
            keyboardType = KeyboardType.Text
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun GenericInputFieldDoneActionPreview() {
    var text by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        GenericInputField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Con acción Done",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )
    }
}