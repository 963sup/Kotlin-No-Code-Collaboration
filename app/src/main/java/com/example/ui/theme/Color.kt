package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sophisticated Dark Theme Base Surfaces (from Design Specification)
val SophisticatedBg = Color(0xFF1C1B1F)           // #1C1B1F Dark Base Canvas
val SophisticatedSurface = Color(0xFF25232A)      // #25232A Main Card / Container Surface
val SophisticatedSurfaceDark = Color(0xFF1D1B20)  // #1D1B20 Sub-container / Deep Inset Surface
val SophisticatedContainer = Color(0xFF332D41)    // #332D41 Elevated / Active Pill / Header container
val SophisticatedBorder = Color(0xFF49454F)       // #49454F Structural Outline / Divider
val SophisticatedBorderSubtle = Color(0x4DD0BCFF) // Subtle Lavender border (30% alpha)

// Sophisticated Accents & M3 Tonal Palette
val LavenderPrimary = Color(0xFFD0BCFF)           // #D0BCFF Primary Soft Lavender Highlight
val LavenderOnPrimary = Color(0xFF381E72)         // #381E72 Contrast text/icon on primary
val LavenderContainer = Color(0xFF4F378B)         // #4F378B M3 Lavender container
val LavenderGlow = Color(0xFFE8DEF8)              // #E8DEF8 Bright lavender tint
val LavenderSubtle = Color(0xFFCCC2DC)            // #CCC2DC Secondary soft lilac gray
val PinkAccent = Color(0xFFEFB8C8)                // #EFB8C8 Tertiary soft rose/pink accent
val WhiteM3 = Color(0xFFFFFFFF)

// Typography & Hierarchy
val TextHighEmphasis = Color(0xFFE6E1E5)          // #E6E1E5 Primary readable text
val TextMediumEmphasis = Color(0xFF938F99)        // #938F99 Secondary muted label
val TextLowEmphasis = Color(0xFF79747E)           // #79747E Tertiary subtle outline/text
val PureWhite = Color(0xFFFFFFFF)

// Governance & Status Indicators (Calibrated to Sophisticated Dark Palette)
val EmeraldSuccess = Color(0xFF81C784)            // Soft emerald success
val EmeraldDark = Color(0xFF1B4D3E)               // Dark emerald container
val AmberWarning = Color(0xFFFFD54F)              // Soft amber warning
val AmberGlow = Color(0xFFFFE082)
val RoseError = Color(0xFFF2B8B5)                 // Soft rose error
val RoseDark = Color(0xFF601410)                  // Dark rose container
val CyanAccent = Color(0xFFD0BCFF)                // Lavender accent for high-tech tags
val CyanGlow = Color(0xFFE8DEF8)
val PurpleTech = Color(0xFFD0BCFF)
val PurpleGlow = Color(0xFFE8DEF8)

// Component & Dark Mode Compatibility Aliases
val SlateDark950 = SophisticatedBg
val SlateDark900 = SophisticatedSurfaceDark
val SlateDark800 = SophisticatedSurface
val SlateDark700 = SophisticatedContainer
val SlateDark600 = SophisticatedBorder
val IndigoPrimary = LavenderPrimary
val IndigoLight = LavenderPrimary
val IndigoDark = LavenderOnPrimary
val CardSurfaceDark = SophisticatedSurface
val CardBorderDark = SophisticatedBorder
val TopBarSurfaceDark = SophisticatedSurfaceDark

