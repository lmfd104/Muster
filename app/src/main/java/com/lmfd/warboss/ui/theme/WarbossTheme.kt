package com.lmfd.warboss.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarbossColors = darkColorScheme(
    primary = Color(0xFFCF1924),        // Crimson
    onPrimary = Color.White,
    secondary = Color(0xFFFFD700),      // Gold
    onSecondary = Color(0xFF1A1A1A),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFFF5252),
    onError = Color.White,
)

@Composable
fun WarbossTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarbossColors,
        content = content,
    )
}
