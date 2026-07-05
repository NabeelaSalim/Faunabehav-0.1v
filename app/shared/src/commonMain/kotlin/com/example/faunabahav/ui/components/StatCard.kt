package com.example.faunabahav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.components.charts.Sparkline
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    /** Real day-over-day % change, or null to hide the trend line (see dayOverDayPercentChange —
     *  it's null rather than fabricated when there's no real "yesterday" data to compare against). */
    trendPercent: Int? = null,
    /** Free-text real status line shown instead of a trend (e.g. "100% operational", "Last updated 7:46 AM"). */
    subtitle: String? = null,
    sparklineValues: List<Float>? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier.hoverable(interactionSource),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 6.dp else 2.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (trendPercent != null) {
                Spacer(Modifier.height(4.dp))
                TrendLabel(trendPercent)
            } else if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (sparklineValues != null) {
                Spacer(Modifier.height(8.dp))
                Sparkline(sparklineValues, color = accentColor)
            }
        }
    }
}

@Composable
private fun TrendLabel(trendPercent: Int) {
    val isUp = trendPercent >= 0
    val color = if (isUp) PrimaryGreen else DangerRed
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isUp) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        Text(
            "${if (isUp) "+" else ""}$trendPercent% from yesterday",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
