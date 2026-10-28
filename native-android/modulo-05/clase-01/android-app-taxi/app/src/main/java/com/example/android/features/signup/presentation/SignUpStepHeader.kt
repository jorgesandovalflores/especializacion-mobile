package com.example.android.features.signup.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.MOTION_LONG_MS
import com.example.android.core.presentation.theme.ColorPrimary

private const val TOTAL_STEPS = 2

@Composable
fun SignUpStepHeader(
    step: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val inPreview = LocalInspectionMode.current
    val progress = remember { Animatable(if (inPreview) step.toFloat() else step - 1f) }
    LaunchedEffect(step) {
        progress.animateTo(step.toFloat(), tween(MOTION_LONG_MS))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(TOTAL_STEPS) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(ColorPrimary.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                        .drawBehind {
                            val fill = (progress.value - index).coerceIn(0f, 1f)
                            drawRoundRect(
                                color = ColorPrimary,
                                size = Size(size.width * fill, size.height),
                                cornerRadius = CornerRadius(3.dp.toPx())
                            )
                        }
                )
            }
        }
        Text(
            text = "Paso $step de $TOTAL_STEPS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ColorPrimary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F0F0F)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B6B6B)
        )
    }
}
