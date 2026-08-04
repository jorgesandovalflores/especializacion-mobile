package com.example.demo_mvvm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IconMint,
    onPrimary = Color.White,
    secondary = IconPeach,
    tertiary = IconLavender,
    background = Bg,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
)

/**
 * The Home screen's palette is a fixed set of bespoke pastels (see ui/theme/Color.kt),
 * not a generated Material dynamic scheme, so this theme intentionally always applies
 * [LightColorScheme] regardless of system dark-mode/dynamic-color settings.
 */
@Composable
fun DemomvvmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
