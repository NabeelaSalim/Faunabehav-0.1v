package com.example.faunabahav.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.AnalyticsRepository
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.AnalyticsSummary
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.components.charts.CategoryBreakdownChart
import com.example.faunabahav.ui.components.charts.EventsOverTimeChart
import com.example.faunabahav.ui.components.charts.RiskDistributionChart
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.util.toDailyEventCounts

/**
 * Reuses ObservationsViewModel (the same instance the standalone Observations screen and the
 * Dashboard's events-over-time panel use) purely to derive the time series client-side, since
 * `/analytics` has no time-series field.
 */
@Composable
fun AnalyticsScreen(
    repository: AnalyticsRepository,
    observationRepository: ObservationRepository,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(
        factory = viewModelFactory { initializer { AnalyticsViewModel(repository) } },
    ),
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Analytics", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        UiStateContent(state = state, modifier = Modifier.weight(1f), onRetry = viewModel::load) { summary ->
            AnalyticsContent(summary, observationsState)
        }
    }
}

@Composable
private fun AnalyticsContent(summary: AnalyticsSummary, observationsState: UiState<List<Observation>>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StatCardRow(summary)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Events over time", style = MaterialTheme.typography.titleMedium)
                if (observationsState is UiState.Success) {
                    EventsOverTimeChart(
                        data = observationsState.data.toDailyEventCounts(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        val sections: @Composable (Modifier) -> Unit = { itemModifier ->
            ChartSection("Risk distribution", itemModifier) {
                RiskDistributionChart(
                    lowRisk = summary.lowRisk,
                    mediumRisk = summary.mediumRisk,
                    highRisk = summary.highRisk,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ChartCard(itemModifier) {
                CategoryBreakdownChart(
                    title = "By species",
                    breakdown = summary.animalBreakdown,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ChartCard(itemModifier) {
                CategoryBreakdownChart(
                    title = "By behaviour",
                    breakdown = summary.behaviourBreakdown,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (rememberIsWideScreen()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                sections(Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                sections(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun StatCardRow(summary: AnalyticsSummary) {
    val isWide = rememberIsWideScreen()
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = if (isWide) 4 else 2,
    ) {
        val cardWidth = Modifier.width(160.dp)
        StatCard("Total events", summary.totalEvents.toString(), Icons.Filled.BarChart, cardWidth)
        StatCard("Low risk", summary.lowRisk.toString(), Icons.Filled.CheckCircle, cardWidth)
        StatCard("Medium risk", summary.mediumRisk.toString(), Icons.Filled.Shield, cardWidth)
        StatCard("High risk", summary.highRisk.toString(), Icons.Filled.Warning, cardWidth)
    }
}

@Composable
private fun ChartSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ChartCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}
