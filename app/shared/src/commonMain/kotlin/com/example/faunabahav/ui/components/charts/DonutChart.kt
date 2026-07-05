package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlin.math.roundToInt

/**
 * A donut chart with the real total in the center and a side legend showing each real count and
 * its percentage of the total — matching the docs/dashboard reference's Risk Distribution and
 * Deterrence Actions Overview cards. All values must be real counts, never fabricated.
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun DonutChart(
    values: List<Int>,
    labels: List<String>,
    colors: List<Color>,
    totalLabel: String = "Total",
    modifier: Modifier = Modifier,
) {
    val total = values.sum()

    if (total == 0) {
        Text("No data yet", style = MaterialTheme.typography.bodySmall, modifier = modifier)
        return
    }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        PieChart(
            values = values.map { it.toFloat() },
            modifier = Modifier.size(150.dp),
            slice = { index -> DefaultSlice(color = colors[index]) },
            holeSize = 0.65f,
            holeContent = {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(total.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(totalLabel, style = MaterialTheme.typography.labelSmall)
                }
            },
        )
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            labels.forEachIndexed { index, label ->
                val value = values[index]
                val pct = (value * 1000.0 / total).roundToInt() / 10.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.size(10.dp).background(colors[index], CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                        Text(
                            "$value ($pct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
