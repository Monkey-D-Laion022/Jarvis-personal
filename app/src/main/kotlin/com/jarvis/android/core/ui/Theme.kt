package com.jarvis.android.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColors = darkColorScheme(
    primary = Color(0xFF4DD0E1),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF0A0F1A),
    onBackground = Color(0xFFE3F2F5),
    surface = Color(0xFF111A2B),
    onSurface = Color(0xFFE3F2F5),
    surfaceVariant = Color(0xFF1B2740),
    onSurfaceVariant = Color(0xFFC5D5E0),
    error = Color(0xFFFF8A80),
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JarvisColors, content = content)
}
