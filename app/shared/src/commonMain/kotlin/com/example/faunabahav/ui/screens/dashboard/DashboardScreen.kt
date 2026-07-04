package com.example.faunabahav.ui.screens.dashboard

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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.data.repository.AnalyticsRepository
import com.example.faunabahav.data.repository.DashboardRepository
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.DashboardSummary
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.mock.MockLiveCameraFeed
import com.example.faunabahav.ui.mock.MockWeatherCard
import com.example.faunabahav.ui.screens.dashboard.panels.DashboardAnalyticsPanel
import com.example.faunabahav.ui.screens.dashboard.panels.DeviceStatusPanel
import com.example.faunabahav.ui.screens.dashboard.panels.RecentAlertsPanel
import com.example.faunabahav.ui.screens.dashboard.panels.RecentObservationsPanel

@Composable
fun DashboardScreen(
    repository: DashboardRepository,
    alertRepository: AlertRepository,
    deviceRepository: DeviceRepository,
    analyticsRepository: AnalyticsRepository,
    observationRepository: ObservationRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { DashboardViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    UiStateContent(state = state, modifier = modifier, onRetry = viewModel::load) { summary ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = viewModel::load) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            MockWeatherCard(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            MockLiveCameraFeed(Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            StatCardGrid(summary)
            Spacer(Modifier.height(16.dp))
            PanelsRow(alertRepository, deviceRepository)
            Spacer(Modifier.height(16.dp))
            DashboardAnalyticsPanel(
                analyticsRepository = analyticsRepository,
                observationRepository = observationRepository,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            RecentObservationsPanel(
                repository = observationRepository,
                baseUrl = baseUrl,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PanelsRow(alertRepository: AlertRepository, deviceRepository: DeviceRepository) {
    if (rememberIsWideScreen()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RecentAlertsPanel(alertRepository, modifier = Modifier.weight(1f))
            DeviceStatusPanel(deviceRepository, modifier = Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RecentAlertsPanel(alertRepository, modifier = Modifier.fillMaxWidth())
            DeviceStatusPanel(deviceRepository, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StatCardGrid(summary: DashboardSummary) {
    val itemsPerRow = if (rememberIsWideScreen()) 4 else 2

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = itemsPerRow,
    ) {
        val cardWidth = Modifier.width(180.dp)
        StatCard(
            title = "Total events",
            value = summary.totalEvents.toString(),
            icon = Icons.Filled.BarChart,
            modifier = cardWidth,
        )
        StatCard(
            title = "High risk events",
            value = summary.highRiskEvents.toString(),
            icon = Icons.Filled.Warning,
            modifier = cardWidth,
        )
        StatCard(
            title = "Active devices",
            value = summary.activeDevices.toString(),
            icon = Icons.Filled.Devices,
            modifier = cardWidth,
        )
        StatCard(
            title = "Deterrence actions taken",
            value = summary.deterrenceActions.toString(),
            icon = Icons.Filled.Shield,
            modifier = cardWidth,
        )
    }
}
