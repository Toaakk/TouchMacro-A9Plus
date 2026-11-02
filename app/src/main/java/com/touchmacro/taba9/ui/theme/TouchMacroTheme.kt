package com.touchmacro.taba9.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate850 = Color(0xFF172033)
val Slate800 = Color(0xFF1E293B)
val Slate750 = Color(0xFF243048)
val Slate700 = Color(0xFF334155)
val CyanPrimary = Color(0xFF0284C7)
val CyanSecondary = Color(0xFF38BDF8)
val EmeraldSuccess = Color(0xFF10B981)
val AmberWarning = Color(0xFFF59E0B)
val RoseError = Color(0xFFEF4444)

private val DarkColorScheme = darkColorScheme(
    primary = CyanSecondary,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Color.White,
    secondary = EmeraldSuccess,
    background = Slate900,
    surface = Slate800,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RoseError
)

@Composable
fun TouchMacroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
