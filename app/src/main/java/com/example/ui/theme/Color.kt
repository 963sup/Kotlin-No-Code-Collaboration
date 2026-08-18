package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light-first enterprise collaboration palette. Legacy token names are retained to
// keep the current component surface stable while the design system moves to M3 roles.
val SophisticatedBg = Color(0xFFF6F8FC)
val SophisticatedSurface = Color(0xFFFFFFFF)
val SophisticatedSurfaceDark = Color(0xFFF9FBFF)
val SophisticatedContainer = Color(0xFFEAF1FF)
val SophisticatedBorder = Color(0xFFD8E0EC)
val SophisticatedBorderSubtle = Color(0x4D0B63F6)

// Primary product identity: high-contrast blue on a white operational canvas.
val LavenderPrimary = Color(0xFF0B63F6)
val LavenderOnPrimary = Color(0xFFFFFFFF)
val LavenderContainer = Color(0xFFDDE9FF)
val LavenderGlow = Color(0xFF2459A9)
val LavenderSubtle = Color(0xFF35598A)
val PinkAccent = Color(0xFF9C2F6D)
val WhiteM3 = Color(0xFFFFFFFF)

// Typography hierarchy for field readability.
val TextHighEmphasis = Color(0xFF14213D)
val TextMediumEmphasis = Color(0xFF5C667A)
val TextLowEmphasis = Color(0xFF8992A6)
val PureWhite = Color(0xFFFFFFFF)

// Operational states calibrated for a light background.
val EmeraldSuccess = Color(0xFF137A43)
val EmeraldDark = Color(0xFFE4F6EC)
val AmberWarning = Color(0xFF8A4E00)
val AmberGlow = Color(0xFFFFE0A3)
val RoseError = Color(0xFFB3261E)
val RoseDark = Color(0xFFFCE8E6)
val CyanAccent = Color(0xFF005B8F)
val CyanGlow = Color(0xFFDDF2FF)
val PurpleTech = Color(0xFF5B42B2)
val PurpleGlow = Color(0xFFECE7FF)

// Compatibility aliases used by existing components.
val SlateDark950 = SophisticatedBg
val SlateDark900 = SophisticatedSurfaceDark
val SlateDark800 = SophisticatedSurface
val SlateDark700 = SophisticatedContainer
val SlateDark600 = SophisticatedBorder
val IndigoPrimary = LavenderPrimary
val IndigoLight = LavenderPrimary
val IndigoDark = Color(0xFF002F66)
val CardSurfaceDark = SophisticatedSurface
val CardBorderDark = SophisticatedBorder
val TopBarSurfaceDark = SophisticatedSurfaceDark

fun parseHexColor(hex: String?, fallback: Color = LavenderPrimary): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        fallback
    }
}
