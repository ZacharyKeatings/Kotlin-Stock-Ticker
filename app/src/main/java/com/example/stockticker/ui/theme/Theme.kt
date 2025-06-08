package com.example.stockticker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Color.Black,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    secondary = Blue500,
    onSecondary = Color.White,
    tertiary = Indigo500,
    onTertiary = Color.White,
    outline = Slate600,
    surfaceVariant = Slate700
)

@Composable
fun StockTickerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
