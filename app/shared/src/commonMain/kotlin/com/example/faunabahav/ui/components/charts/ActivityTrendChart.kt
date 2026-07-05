package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.TrendPeriod
import com.example.faunabahav.ui.util.TrendPoint

@Composable
fun PeriodToggle(selected: TrendPeriod, onSelect: (TrendPeriod) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TrendPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(period) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    period.label,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/** A filled-area trend line with real per-bucket counts and a highlighted latest-point badge. */
@Composable
fun ActivityTrendChart(data: List<TrendPoint>, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (data.isEmpty()) {
            Text("Not enough data yet to chart a trend", style = MaterialTheme.typography.bodySmall)
            return@Column
        }

        val maxCount = data.maxOf { it.count }.coerceAtLeast(1)
        val latest = data.last()

        Box(Modifier.fillMaxWidth().height(180.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 20.dp, end = 36.dp)) {
                val stepX = if (data.size > 1) size.width / (data.size - 1) else 0f
                val points = data.mapIndexed { index, entry ->
                    val x = stepX * index
                    val y = size.height - (entry.count.toFloat() / maxCount) * size.height
                    Offset(x, y)
                }

                // Gridlines
                val gridColor = Color.Black.copy(alpha = 0.06f)
                for (fraction in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
                    val y = size.height * (1 - fraction)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }

                // Filled area under the line
                if (points.size > 1) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(points.first().x, size.height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            listOf(PrimaryGreen.copy(alpha = 0.28f), PrimaryGreen.copy(alpha = 0.02f)),
                        ),
                    )
                }

                for (i in 0 until points.size - 1) {
                    drawLine(color = PrimaryGreen, start = points[i], end = points[i + 1], strokeWidth = 4f)
                }
                // Only the latest point is highlighted, matching the reference's single callout bubble
                drawCircle(color = PrimaryGreen, radius = 6f, center = points.last())
                drawCircle(color = Color.White, radius = 3f, center = points.last())
            }

            Box(Modifier.align(Alignment.TopEnd).padding(top = 0.dp)) {
                LatestPointBadge(latest)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.first().axisLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (data.size > 4) {
                Text(
                    data[data.size / 2].axisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(data.last().axisLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LatestPointBadge(point: TrendPoint) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryGreen)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Column {
            Text(point.count.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(point.axisLabel, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
