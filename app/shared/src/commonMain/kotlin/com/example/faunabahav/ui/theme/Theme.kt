package com.example.faunabahav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FaunaBahavLightColorScheme = lightColorScheme(
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

private val FaunaBahavDarkColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = Color.Black,
    primaryContainer = DarkGreen,
    onPrimaryContainer = Color.White,
    secondary = AccentOrange,
    onSecondary = Color.Black,
    error = DangerRed,
    onError = Color.Black,
    errorContainer = Color(0xFF4A1414),
    onErrorContainer = Color(0xFFFFB4AB),
    background = BackgroundDark,
    onBackground = PrimaryTextDark,
    surface = CardDark,
    onSurface = PrimaryTextDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = SecondaryTextDark,
    outline = BorderColorDark,
)

@Composable
fun FaunaBahavTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) FaunaBahavDarkColorScheme else FaunaBahavLightColorScheme,
        shapes = FaunaBahavShapes,
        typography = FaunaBahavTypography,
        content = content,
    )
}
