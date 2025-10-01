package com.example.android.commons.presentation

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> error("No Activity found")
    }

@Composable
fun NavigationBarStyle(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context.findActivity()
    val window = activity.window
    val controller = WindowInsetsControllerCompat(window, window.decorView)

    DisposableEffect(color, darkIcons) {
        val prevColor = window.navigationBarColor
        val prevLight = controller.isAppearanceLightNavigationBars

        window.navigationBarColor = color.toArgb()
        controller.isAppearanceLightNavigationBars = darkIcons
        window.isNavigationBarContrastEnforced = false

        onDispose {
            window.navigationBarColor = prevColor
            controller.isAppearanceLightNavigationBars = prevLight
        }
    }
}