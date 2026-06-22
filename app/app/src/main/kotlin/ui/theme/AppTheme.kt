package com.example.appcantina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D5F),
    onPrimary = Color.White,
    secondary = Color(0xFFF4C95D),
    onSecondary = Color(0xFF332900),
    tertiary = Color(0xFF3D6EA8),
    background = Color(0xFFF7F8F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EEE8),
    onSurface = Color(0xFF1D1F1C),
    onSurfaceVariant = Color(0xFF4A554F),
    error = Color(0xFFB3261E)
)

@Composable
fun AppCantinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
