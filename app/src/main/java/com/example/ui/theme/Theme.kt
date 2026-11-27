package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HaniJayaLightColorScheme = lightColorScheme(
    primary = WalletIndigo,
    onPrimary = Color.White,
    primaryContainer = CnfBlueSoft,
    onPrimaryContainer = NavyTopBar,
    secondary = CnfEmeraldStart,
    onSecondary = Color.White,
    secondaryContainer = CnfEmeraldSoft,
    onSecondaryContainer = CnfEmeraldStart,
    tertiary = HeroGradientEnd,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun HaniJayaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HaniJayaLightColorScheme,
        typography = AppTypography,
        content = content
    )
}
