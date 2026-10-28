package com.example.sheff.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable



private val LightColors = lightColorScheme(
    primary = Yellow,
    onPrimary = InkBlack,
    secondary = Mint,
    onSecondary = BrownText,
    background = Cream,
    onBackground = BrownText,
    surface = Cream,
    onSurface = BrownText,
    surfaceVariant = CardGray,
    onSurfaceVariant = BrownText
)

@Composable
fun SheffTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = SheffShapes,
        content = content
    )
}
