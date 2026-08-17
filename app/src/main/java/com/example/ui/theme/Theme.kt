package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = SophisticatedContainer,
    onPrimaryContainer = LavenderPrimary,
    secondary = LavenderSubtle,
    onSecondary = LavenderOnPrimary,
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = LavenderGlow,
    tertiary = PinkAccent,
    onTertiary = Color(0xFF492532),
    background = SophisticatedBg,
    onBackground = TextHighEmphasis,
    surface = SophisticatedSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = SophisticatedSurfaceDark,
    onSurfaceVariant = TextMediumEmphasis,
    outline = SophisticatedBorder,
    outlineVariant = Color(0xFF79747E),
    error = RoseError,
    onError = Color(0xFF601410)
)

private val LightColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = SophisticatedContainer,
    onPrimaryContainer = LavenderPrimary,
    secondary = LavenderSubtle,
    onSecondary = LavenderOnPrimary,
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = LavenderGlow,
    tertiary = PinkAccent,
    onTertiary = Color(0xFF492532),
    background = SophisticatedBg,
    onBackground = TextHighEmphasis,
    surface = SophisticatedSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = SophisticatedSurfaceDark,
    onSurfaceVariant = TextMediumEmphasis,
    outline = SophisticatedBorder,
    outlineVariant = Color(0xFF79747E),
    error = RoseError,
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek executive dark mode for cyber governance
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
