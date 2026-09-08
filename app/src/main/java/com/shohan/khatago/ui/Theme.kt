package com.shohan.khatago.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Emerald = Color(0xFF087F5B)
val EmeraldDark = Color(0xFF064E3B)
val Mint = Color(0xFFDDF8EC)
val MintBright = Color(0xFFB7F2D6)
val Canvas = Color(0xFFF7FAF8)
val Ink = Color(0xFF15201B)
val Muted = Color(0xFF708078)
val Line = Color(0xFFE4ECE7)
val Success = Color(0xFF16845A)
val Warning = Color(0xFFB86B00)
val Danger = Color(0xFFC74747)
val Info = Color(0xFF2774B9)

private val KhataGoColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = EmeraldDark,
    secondary = Color(0xFF4F8371),
    secondaryContainer = Color(0xFFE5F3ED),
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFF5F1),
    onSurfaceVariant = Color(0xFF53635A),
    outline = Line,
    error = Danger
)

private val KhataGoTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 25.sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
        bodyLarge = base.bodyLarge.copy(lineHeight = 25.sp)
    )
}

@Composable
fun KhataGoTheme(content: @Composable () -> Unit) {
    // Deliberately use the light palette regardless of the device setting.
    @Suppress("UNUSED_VARIABLE") val ignored = isSystemInDarkTheme()
    MaterialTheme(colorScheme = KhataGoColors, typography = KhataGoTypography, content = content)
}
