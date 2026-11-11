package com.fundtracker.expenses.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDarkTheme,
    onPrimary = Color.Black,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = GreenPrimaryLight,
    onSecondary = Color.Black,
    background = GreenBackgroundDark,
    onBackground = Color(0xFFE0E0E0),
    surface = GreenSurfaceDark,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF283B2B),
    onSurfaceVariant = Color(0xFFC8E6C9)
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenPrimaryDark,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    background = GreenBackgroundLight,
    onBackground = Color(0xFF1E271F),
    surface = GreenSurfaceLight,
    onSurface = Color(0xFF1E271F),
    surfaceVariant = GreenContainer,
    onSurfaceVariant = GreenPrimaryDark
)

@Composable
fun FundTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GreenPrimaryDark.toArgb()
            window.navigationBarColor = GreenPrimaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
