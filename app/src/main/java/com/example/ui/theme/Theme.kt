package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TacticalGreen,
    secondary = TacticalBlue,
    tertiary = TacticalAmber,
    background = TacticalDark,
    surface = TacticalSurface,
    onBackground = TacticalLight,
    onSurface = TacticalLight,
    surfaceVariant = TacticalSurfaceVariant,
    error = TacticalDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the tactical command center feel
    dynamicColor: Boolean = false, // Disable dynamic colors to keep original military visual identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
