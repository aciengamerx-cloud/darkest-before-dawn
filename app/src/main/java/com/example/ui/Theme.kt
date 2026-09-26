package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE50914), // Midnight Crimson
    secondary = Color(0xFFFF9F0A), // Light Amber
    tertiary = Color(0xFF64748B), // Steel Grey
    background = Color(0xFF030712), // Deep Shadow
    surface = Color(0xFF111827), // Charcoal
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6)
)

private val LightColorScheme = DarkColorScheme // Always maintain dark mood

@Composable
fun DarkestBeforeDawnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
