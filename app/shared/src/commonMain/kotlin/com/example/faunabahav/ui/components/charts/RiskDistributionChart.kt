package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

private val RiskColors = listOf(LightGreen, AccentOrange, DangerRed)
private val RiskLabels = listOf("Low", "Medium", "High")

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun RiskDistributionChart(lowRisk: Int, mediumRisk: Int, highRisk: Int, modifier: Modifier = Modifier) {
    val values = listOf(lowRisk, mediumRisk, highRisk).map { it.toFloat() }

    Column(modifier) {
        if (values.sum() == 0f) {
            Text("No events yet", style = MaterialTheme.typography.bodySmall)
            return@Column
        }

        PieChart(
            values = values,
            modifier = Modifier.fillMaxWidth().height(160.dp),
            slice = { index -> DefaultSlice(color = RiskColors[index]) },
        )
        Spacer(Modifier.height(8.dp))
        Legend(RiskLabels, RiskColors)
    }
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
