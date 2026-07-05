package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.LightGreen

private val RiskColors = listOf(DangerRed, AccentOrange, LightGreen)
private val RiskLabels = listOf("High", "Medium", "Low")

@Composable
fun RiskDistributionChart(lowRisk: Int, mediumRisk: Int, highRisk: Int, modifier: Modifier = Modifier) {
    DonutChart(
        values = listOf(highRisk, mediumRisk, lowRisk),
        labels = RiskLabels,
        colors = RiskColors,
        totalLabel = "Total",
        modifier = modifier,
    )
}

@Composable
internal fun Legend(labels: List<String>, colors: List<Color>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        labels.forEachIndexed { index, label ->
            Row {
                Spacer(Modifier.size(10.dp).background(colors[index], CircleShape))
                Spacer(Modifier.size(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
