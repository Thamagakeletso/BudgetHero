package com.example.budgethero.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetColorScheme = lightColorScheme(
    primary            = BrandGreen,
    onPrimary          = Color.White,
    primaryContainer   = BrandGreenLight,
    onPrimaryContainer = BrandGreenDark,
    background         = BackgroundGray,
    surface            = SurfaceWhite,
    onBackground       = TextPrimary,
    onSurface          = TextPrimary,
)

@Composable
fun BudgetHeroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BudgetColorScheme,
        typography  = Typography,
        content     = content
    )
}