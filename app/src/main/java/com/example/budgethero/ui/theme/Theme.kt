package com.example.budgethero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Light color scheme using BudgetHero brand green palette.
 * Reference: Material 3 Color System (m3.material.io/styles/color)
 */
private val LightColorScheme = lightColorScheme(
    primary            = BrandGreen,
    onPrimary          = Color.White,
    primaryContainer   = BrandGreenLight,
    onPrimaryContainer = BrandGreenDark,
    background         = BackgroundGray,
    surface            = SurfaceWhite,
    onBackground       = TextPrimary,
    onSurface          = TextPrimary,
)

/**
 * Dark color scheme for dark mode support.
 * Uses darker variants of the brand green palette.
 */
private val DarkColorScheme = darkColorScheme(
    primary            = BrandGreen,
    onPrimary          = Color.White,
    primaryContainer   = BrandGreenDark,
    onPrimaryContainer = BrandGreenLight,
    background         = Color(0xFF121212),
    surface            = Color(0xFF1E1E1E),
    onBackground       = Color.White,
    onSurface          = Color.White,
)

/**
 * CompositionLocal to provide dark mode state throughout the app.
 * Allows any composable to read or toggle dark mode.
 */
val LocalDarkMode = compositionLocalOf { mutableStateOf(false) }

/**
 * Main app theme that supports both light and dark modes.
 * Dark mode can be toggled via LocalDarkMode composition local.
 * @param darkTheme Whether to use dark theme
 * @param content The composable content to theme
 */
@Composable
fun BudgetHeroTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}