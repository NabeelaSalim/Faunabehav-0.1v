package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.Observation
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.emoji
import com.example.faunabahav.ui.util.lastNDaysCounts
import com.example.faunabahav.ui.util.mostCommonSpecies
import kotlin.math.roundToInt

/**
 * All five cards are computed from the real observations already fetched for [periodScoped] —
 * the caller passes in whatever subset the top-right period selector (Today/7 Days/30 Days)
 * currently scopes to, so "Total Observations" etc. genuinely reflect that period.
 */
@Composable
fun ObservationStatCards(periodScoped: List<Observation>, periodLabel: String) {
    val total = periodScoped.size
    val high = periodScoped.count { it.riskLevel == RiskLevel.HIGH }
    val medium = periodScoped.count { it.riskLevel == RiskLevel.MEDIUM }
    val low = periodScoped.count { it.riskLevel == RiskLevel.LOW }
    val commonSpecies = periodScoped.mostCommonSpecies()

    fun pct(count: Int): String = if (total == 0) "—" else "${((count * 1000.0 / total).roundToInt() / 10.0)}%"

    val itemsPerRow = if (rememberIsWideScreen()) 5 else 2
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = itemsPerRow,
    ) {
        val cardWidth = Modifier.width(200.dp)
        StatCard(
            title = "Total Observations",
            value = total.toString(),
            icon = Icons.AutoMirrored.Filled.Assignment,
            accentColor = PrimaryGreen,
            subtitle = periodLabel,
            sparklineValues = periodScoped.map { it.timestamp }.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "High Risk",
            value = high.toString(),
            icon = Icons.Filled.Shield,
            accentColor = DangerRed,
            subtitle = pct(high),
            sparklineValues = periodScoped.filter { it.riskLevel == RiskLevel.HIGH }.map { it.timestamp }.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "Medium Risk",
            value = medium.toString(),
            icon = Icons.Filled.Shield,
            accentColor = AccentOrange,
            subtitle = pct(medium),
            sparklineValues = periodScoped.filter { it.riskLevel == RiskLevel.MEDIUM }.map { it.timestamp }.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "Low Risk",
            value = low.toString(),
            icon = Icons.Filled.Shield,
            accentColor = PrimaryGreen,
            subtitle = pct(low),
            sparklineValues = periodScoped.filter { it.riskLevel == RiskLevel.LOW }.map { it.timestamp }.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "Most Common Species",
            value = commonSpecies?.let { (species, _) -> species.emoji() } ?: "—",
            icon = Icons.Filled.Pets,
            accentColor = PrimaryGreen,
            subtitle = commonSpecies?.let { (species, count) -> "${species.displayName()} ($count)" } ?: "No data yet",
            modifier = cardWidth,
        )
    }
}
