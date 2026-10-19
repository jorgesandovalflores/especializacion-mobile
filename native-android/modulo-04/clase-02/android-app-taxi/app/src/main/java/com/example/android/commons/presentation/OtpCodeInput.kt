package com.example.android.commons.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===============================================
// OtpBox: Caja individual para un dígito del OTP
// ===============================================
@Composable
fun OtpBox(
    value: String,
    onChange: (String) -> Unit,
    requester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val borderColor = if (value.isNotEmpty()) Color(0xFF222222) else Color(0xFFCFCFCF)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(color = Color(0xFFF8F8F8)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF0F0F0F)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { /* El cambio de foco se maneja externamente */ }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(requester)
        )
    }
}

// =====================================================
// OtpCodeInput: Arma las 4 cajas y maneja foco/filtrado
// - Pasa el weight desde el Row a cada OtpBox
// - Mueve el foco automáticamente hacia adelante/atrás
// =====================================================
@Composable
fun OtpCodeInput(
    d0: String, onD0: (String) -> Unit,
    d1: String, onD1: (String) -> Unit,
    d2: String, onD2: (String) -> Unit,
    d3: String, onD3: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val requesters = remember { List(4) { FocusRequester() } }

    fun next(index: Int) {
        if (index < 3) requesters[index + 1].requestFocus() else focusManager.clearFocus()
    }

    fun prev(index: Int) {
        if (index > 0) requesters[index - 1].requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OtpBox(
            value = d0,
            onChange = { new ->
                val filtered = new.filter(Char::isDigit).take(1)
                if (filtered != d0) onD0(filtered)
                if (filtered.length == 1) next(0)
            },
            requester = requesters[0],
            modifier = Modifier.weight(1f)
        )
        OtpBox(
            value = d1,
            onChange = { new ->
                val filtered = new.filter(Char::isDigit).take(1)
                if (filtered != d1) onD1(filtered)
                if (filtered.length == 1) next(1) else if (filtered.isEmpty()) prev(1)
            },
            requester = requesters[1],
            modifier = Modifier.weight(1f)
        )
        OtpBox(
            value = d2,
            onChange = { new ->
                val filtered = new.filter(Char::isDigit).take(1)
                if (filtered != d2) onD2(filtered)
                if (filtered.length == 1) next(2) else if (filtered.isEmpty()) prev(2)
            },
            requester = requesters[2],
            modifier = Modifier.weight(1f)
        )
        OtpBox(
            value = d3,
            onChange = { new ->
                val filtered = new.filter(Char::isDigit).take(1)
                if (filtered != d3) onD3(filtered)
                if (filtered.isEmpty()) prev(3)
            },
            requester = requesters[3],
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewOtpCodeInput() {
    var d0 by remember { mutableStateOf("") }
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var d3 by remember { mutableStateOf("") }

    MaterialTheme {
        Surface {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                OtpCodeInput(
                    d0 = d0, onD0 = { d0 = it },
                    d1 = d1, onD1 = { d1 = it },
                    d2 = d2, onD2 = { d2 = it },
                    d3 = d3, onD3 = { d3 = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}