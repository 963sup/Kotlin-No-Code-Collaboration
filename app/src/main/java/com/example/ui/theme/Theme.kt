package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00478A),
    onPrimaryContainer = Color(0xFFD7E3FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253141),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = Color(0xFFD5B8F1),
    onTertiary = Color(0xFF3A2750),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF191C20),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474F),
    onSurfaceVariant = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = Color(0xFF002F66),
    secondary = Color(0xFF52657F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F5),
    onSecondaryContainer = Color(0xFF10263F),
    tertiary = PurpleTech,
    onTertiary = Color.White,
    background = SophisticatedBg,
    onBackground = TextHighEmphasis,
    surface = SophisticatedSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = SophisticatedSurfaceDark,
    onSurfaceVariant = TextMediumEmphasis,
    outline = SophisticatedBorder,
    outlineVariant = Color(0xFFE3E8F0),
    error = RoseError,
    onError = Color.White,
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun MyApplicationTheme(darkTheme: Boolean = false, dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
