package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.AnalyticsRepository
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.ui.components.charts.EventsOverTimeChart
import com.example.faunabahav.ui.components.charts.RiskDistributionChart
import com.example.faunabahav.ui.screens.analytics.AnalyticsViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.util.toDailyEventCounts

/**
 * Condensed analytics view for the Dashboard, reusing the same AnalyticsViewModel/
 * ObservationsViewModel instances the full Analytics screen uses — see the note in
 * RecentAlertsPanel about why that avoids a duplicate fetch.
 */
@Composable
fun DashboardAnalyticsPanel(
    analyticsRepository: AnalyticsRepository,
    observationRepository: ObservationRepository,
    modifier: Modifier = Modifier,
    analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = viewModelFactory { initializer { AnalyticsViewModel(analyticsRepository) } },
    ),
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
) {
    val analyticsState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Analytics", style = MaterialTheme.typography.titleMedium)

            val summary = analyticsState
            if (summary is UiState.Success) {
                RiskDistributionChart(
                    lowRisk = summary.data.lowRisk,
                    mediumRisk = summary.data.mediumRisk,
                    highRisk = summary.data.highRisk,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val observations = observationsState
            if (observations is UiState.Success) {
                EventsOverTimeChart(
                    data = observations.data.toDailyEventCounts(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
