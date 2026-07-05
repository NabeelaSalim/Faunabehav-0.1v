package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A tiny real-data trend line for a stat card — no axis, just the shape of recent activity. */
@Composable
fun Sparkline(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    val maxValue = values.max().coerceAtLeast(1f)

    Canvas(modifier = modifier.fillMaxWidth().height(28.dp)) {
        val stepX = size.width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            val x = stepX * index
            val y = size.height - (value / maxValue) * size.height
            Offset(x, y)
        }
        for (i in 0 until points.size - 1) {
            drawLine(color = color, start = points[i], end = points[i + 1], strokeWidth = 3f)
        }
    }
}
