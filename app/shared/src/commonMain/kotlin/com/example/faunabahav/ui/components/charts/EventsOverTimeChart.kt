package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.DailyEventCount

/**
 * A hand-rolled Canvas line chart rather than koalaplot: koalaplot's line/bar plots need a full
 * XYGraph + AxisModel setup (verified via its actual public API, not assumed) which would take
 * meaningfully more time to get right than this simple trend line needs. koalaplot's standalone
 * PieChart is used instead for RiskDistributionChart/CategoryBreakdownChart, where it's a clean fit.
 */
@Composable
fun EventsOverTimeChart(data: List<DailyEventCount>, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (data.size < 2) {
            Text("Not enough data yet to chart a trend", style = MaterialTheme.typography.bodySmall)
            return@Column
        }

        val maxCount = data.maxOf { it.count }.coerceAtLeast(1)

        Canvas(
            modifier = Modifier.fillMaxWidth().height(140.dp).padding(vertical = 8.dp),
        ) {
            val stepX = if (data.size > 1) size.width / (data.size - 1) else 0f
            val points = data.mapIndexed { index, entry ->
                val x = stepX * index
                val y = size.height - (entry.count.toFloat() / maxCount) * size.height
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = PrimaryGreen, start = points[i], end = points[i + 1], strokeWidth = 4f)
            }
            points.forEach { point -> drawCircle(color = PrimaryGreen, radius = 5f, center = point) }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.first().date.toString(), style = MaterialTheme.typography.labelSmall)
            Text(data.last().date.toString(), style = MaterialTheme.typography.labelSmall)
        }
    }
}
