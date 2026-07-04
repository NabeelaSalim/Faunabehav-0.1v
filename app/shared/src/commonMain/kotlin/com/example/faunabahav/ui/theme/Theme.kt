package com.example.faunabahav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FaunaBahavColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = Color.White,
    secondary = AccentOrange,
    onSecondary = Color.White,
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = DangerRed,
    background = BackgroundLight,
    onBackground = PrimaryText,
    surface = CardWhite,
    onSurface = PrimaryText,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor,
)

@Composable
fun FaunaBahavTheme(content: @Composable () -> Unit) {
    // Dark theme isn't part of this design pass; the light palette is used regardless of
    // system setting so the SaaS-style look stays consistent across Android and Web.
    MaterialTheme(
        colorScheme = FaunaBahavColorScheme,
        shapes = FaunaBahavShapes,
        typography = FaunaBahavTypography,
        content = content,
    )
}
