package com.loopline.puzzle.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LoopLineColorScheme = darkColorScheme(
    background = BackgroundBase,
    surface = SurfaceCard,
    primary = Gold,
    secondary = Copper,
    tertiary = RoseGold,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color(0xFF1F160F),
)

@Composable
fun LoopLineTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            activity.window.statusBarColor = BackgroundBase.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LoopLineColorScheme,
        typography = LoopLineTypography,
        content = content
    )
}
