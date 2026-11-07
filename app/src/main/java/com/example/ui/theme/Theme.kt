package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexaDarkColorScheme = darkColorScheme(
    primary = NexaPurple,
    onPrimary = Color.White,
    primaryContainer = NexaCardHover,
    onPrimaryContainer = Color.White,
    secondary = NexaPink,
    onSecondary = Color.White,
    secondaryContainer = NexaCard,
    onSecondaryContainer = NexaTextPrimary,
    tertiary = NexaGlow,
    onTertiary = Color.White,
    background = NexaBg,
    onBackground = NexaTextPrimary,
    surface = NexaSurface,
    onSurface = NexaTextPrimary,
    surfaceVariant = NexaCard,
    onSurfaceVariant = NexaTextSecondary,
    outline = NexaBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
