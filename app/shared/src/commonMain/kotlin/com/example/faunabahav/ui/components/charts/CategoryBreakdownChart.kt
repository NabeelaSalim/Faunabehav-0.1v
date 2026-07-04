package com.example.faunabahav.ui.components.charts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.DarkGreen
import com.example.faunabahav.ui.theme.ForestGreen
import com.example.faunabahav.ui.theme.LightGreen
import com.example.faunabahav.ui.theme.PrimaryGreen
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

private val Palette = listOf(PrimaryGreen, AccentOrange, LightGreen, DangerRed, ForestGreen, DarkGreen)

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CategoryBreakdownChart(title: String, breakdown: Map<String, Int>, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        if (breakdown.isEmpty()) {
            Text("No data yet", style = MaterialTheme.typography.bodySmall)
            return@Column
        }

        val labels = breakdown.keys.toList()
        val values = breakdown.values.map { it.toFloat() }
        val colors = labels.indices.map { Palette[it % Palette.size] }

        PieChart(
            values = values,
            modifier = Modifier.fillMaxWidth().height(160.dp),
            slice = { index -> DefaultSlice(color = colors[index]) },
        )
        Spacer(Modifier.height(8.dp))
        Legend(labels, colors)
    }
}
