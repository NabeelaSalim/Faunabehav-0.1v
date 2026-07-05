package com.example.faunabahav.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.AccentPurple
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen

private val DeterrenceOrder = listOf("Sound", "Light", "Siren", "Other")
private val DeterrenceColors = mapOf(
    "Sound" to PrimaryGreen,
    "Light" to AccentOrange,
    "Siren" to DangerRed,
    "Other" to AccentPurple,
)

/** [breakdown] must be real per-category counts (see List<Observation>.deterrenceCategoryBreakdown()). */
@Composable
fun DeterrenceOverviewChart(breakdown: Map<String, Int>, modifier: Modifier = Modifier) {
    val nonZero = DeterrenceOrder.filter { (breakdown[it] ?: 0) > 0 }
    DonutChart(
        values = nonZero.map { breakdown[it] ?: 0 },
        labels = nonZero,
        colors = nonZero.map { DeterrenceColors[it]!! },
        totalLabel = "Total",
        modifier = modifier,
    )
}
