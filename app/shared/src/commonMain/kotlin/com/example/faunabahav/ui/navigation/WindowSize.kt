package com.example.faunabahav.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

private val WideScreenBreakpoint = 840.dp

/**
 * Wide screens (Web/desktop/tablet) get the fixed sidebar; narrow screens (Android phones) get
 * the bottom navigation bar. Threshold matches Material3's "expanded" window size class convention.
 */
@Composable
fun rememberIsWideScreen(): Boolean {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val widthDp = with(density) { containerSize.width.toDp() }
    return widthDp >= WideScreenBreakpoint
}
