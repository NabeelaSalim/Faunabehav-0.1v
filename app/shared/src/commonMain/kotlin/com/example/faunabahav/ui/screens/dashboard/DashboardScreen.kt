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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.Observation
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.components.charts.DeterrenceOverviewChart
import com.example.faunabahav.ui.components.charts.RiskDistributionChart
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.navigation.Destination
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.screens.alerts.AlertsViewModel
import com.example.faunabahav.ui.screens.dashboard.panels.DetectionHistoryTable
import com.example.faunabahav.ui.screens.dashboard.panels.DeviceStatusPanel
import com.example.faunabahav.ui.screens.dashboard.panels.LiveDetectionGrid
import com.example.faunabahav.ui.screens.dashboard.panels.RecentAlertsPanel
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.util.dayOverDayPercentChange
import com.example.faunabahav.ui.util.deterrenceCategoryBreakdown
import com.example.faunabahav.ui.util.lastNDaysCounts
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** How often the Dashboard re-polls the backend so every panel reflects new inference results
 * without a manual refresh — there's no push/websocket channel, so periodic reload is the
 * honest way to approximate "live" on top of the real, single-shot inference endpoint. */
private const val POLL_INTERVAL_MS = 10_000L

@Composable
fun DashboardScreen(
    repository: DashboardRepository,
    alertRepository: AlertRepository,
    deviceRepository: DeviceRepository,
    analyticsRepository: AnalyticsRepository,
    observationRepository: ObservationRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit = {},
    viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { DashboardViewModel(repository) } },
    ),
    alertsViewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(alertRepository) } },
    ),
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()
    val alertsState by alertsViewModel.uiState.collectAsStateWithLifecycle()
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(POLL_INTERVAL_MS)
            isRefreshing = true
            viewModel.load()
            alertsViewModel.load()
            devicesViewModel.load()
            observationsViewModel.load()
            delay(400)
            isRefreshing = false
        }
    }

    UiStateContent(state = state, modifier = modifier, onRetry = viewModel::load) { summary ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            DashboardHeader(isRefreshing)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            StatCardGrid(summary, observationsState, alertsState, devicesState)
            Spacer(Modifier.height(16.dp))

            MainRow(
                deviceRepository = deviceRepository,
                observationRepository = observationRepository,
                alertRepository = alertRepository,
                baseUrl = baseUrl,
                devicesViewModel = devicesViewModel,
                observationsViewModel = observationsViewModel,
                alertsViewModel = alertsViewModel,
                onNavigate = onNavigate,
            )
            Spacer(Modifier.height(16.dp))

            SecondaryRow(
                deviceRepository = deviceRepository,
                observationRepository = observationRepository,
                devicesViewModel = devicesViewModel,
                onNavigate = onNavigate,
            )
            Spacer(Modifier.height(16.dp))

            DetectionHistoryTable(
                observationRepository = observationRepository,
                deviceRepository = deviceRepository,
                observationsViewModel = observationsViewModel,
                devicesViewModel = devicesViewModel,
                modifier = Modifier.fillMaxWidth(),
                onViewFullHistory = { onNavigate(Destination.Observations) },
            )
        }
    }
}

@Composable
private fun DashboardHeader(isRefreshing: Boolean) {
    if (rememberIsWideScreen()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Live Monitoring Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Real-time wildlife monitoring and activity overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DashboardTopBar(isRefreshing)
        }
    } else {
        Column {
            Text("Live Monitoring Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Real-time wildlife monitoring and activity overview",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            DashboardTopBar(isRefreshing)
        }
    }
}

@Composable
private fun MainRow(
    deviceRepository: DeviceRepository,
    observationRepository: ObservationRepository,
    alertRepository: AlertRepository,
    baseUrl: String,
    devicesViewModel: DevicesViewModel,
    observationsViewModel: ObservationsViewModel,
    alertsViewModel: AlertsViewModel,
    onNavigate: (Destination) -> Unit,
) {
    if (rememberIsWideScreen()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LiveDetectionGrid(
                deviceRepository = deviceRepository,
                observationRepository = observationRepository,
                baseUrl = baseUrl,
                devicesViewModel = devicesViewModel,
                observationsViewModel = observationsViewModel,
                onViewAllCameras = { onNavigate(Destination.Devices) },
                modifier = Modifier.weight(2.2f),
            )
            RecentAlertsPanel(
                alertRepository,
                viewModel = alertsViewModel,
                onViewAllAlerts = { onNavigate(Destination.Alerts) },
                modifier = Modifier.weight(1f),
            )
            RiskDeterrenceColumn(observationsViewModel, modifier = Modifier.weight(1.1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiveDetectionGrid(
                deviceRepository = deviceRepository,
                observationRepository = observationRepository,
                baseUrl = baseUrl,
                devicesViewModel = devicesViewModel,
                observationsViewModel = observationsViewModel,
                onViewAllCameras = { onNavigate(Destination.Devices) },
                modifier = Modifier.fillMaxWidth(),
            )
            RecentAlertsPanel(
                alertRepository,
                viewModel = alertsViewModel,
                onViewAllAlerts = { onNavigate(Destination.Alerts) },
                modifier = Modifier.fillMaxWidth(),
            )
            RiskDeterrenceColumn(observationsViewModel, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RiskDeterrenceColumn(observationsViewModel: ObservationsViewModel, modifier: Modifier = Modifier) {
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()
    val observations = (observationsState as? UiState.Success)?.data ?: emptyList()

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Risk Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                RiskDistributionChart(
                    lowRisk = observations.count { it.riskLevel == RiskLevel.LOW },
                    mediumRisk = observations.count { it.riskLevel == RiskLevel.MEDIUM },
                    highRisk = observations.count { it.riskLevel == RiskLevel.HIGH },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Deterrence Actions Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DeterrenceOverviewChart(observations.deterrenceCategoryBreakdown(), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SecondaryRow(
    deviceRepository: DeviceRepository,
    observationRepository: ObservationRepository,
    devicesViewModel: DevicesViewModel,
    onNavigate: (Destination) -> Unit,
) {
    DeviceStatusPanel(
        deviceRepository,
        observationRepository,
        viewModel = devicesViewModel,
        onViewAllDevices = { onNavigate(Destination.Devices) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StatCardGrid(
    summary: DashboardSummary,
    observationsState: UiState<List<Observation>>,
    alertsState: UiState<List<com.example.faunabahav.model.Alert>>,
    devicesState: UiState<List<Device>>,
) {
    val itemsPerRow = if (rememberIsWideScreen()) 5 else 2
    val observations = (observationsState as? UiState.Success)?.data ?: emptyList()
    val alerts = (alertsState as? UiState.Success)?.data ?: emptyList()
    val devices = (devicesState as? UiState.Success)?.data ?: emptyList()

    val allTimestamps = observations.map { it.timestamp }
    val highRiskTimestamps = observations.filter { it.riskLevel == RiskLevel.HIGH }.map { it.timestamp }
    val alertTimestamps = alerts.map { it.timestamp }
    val todaysDetections = todaysDetectionCount(observations)

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = itemsPerRow,
    ) {
        val cardWidth = Modifier.width(200.dp)
        StatCard(
            title = "Total Observations",
            value = summary.totalEvents.toString(),
            icon = Icons.Filled.BarChart,
            accentColor = com.example.faunabahav.ui.theme.PrimaryGreen,
            trendPercent = allTimestamps.dayOverDayPercentChange(),
            sparklineValues = allTimestamps.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "High Risk Events",
            value = summary.highRiskEvents.toString(),
            icon = Icons.Filled.Warning,
            accentColor = com.example.faunabahav.ui.theme.DangerRed,
            trendPercent = highRiskTimestamps.dayOverDayPercentChange(),
            sparklineValues = highRiskTimestamps.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "Active Cameras",
            value = summary.activeDevices.toString(),
            icon = Icons.Filled.Devices,
            accentColor = com.example.faunabahav.ui.theme.AccentPurple,
            subtitle = devices.takeIf { it.isNotEmpty() }?.let {
                val pct = (summary.activeDevices * 100.0 / it.size).let { p -> (p * 10).toInt() / 10.0 }
                "$pct% operational"
            },
            modifier = cardWidth,
        )
        StatCard(
            title = "Deterrence Actions Triggered",
            value = summary.deterrenceActions.toString(),
            icon = Icons.Filled.Shield,
            accentColor = com.example.faunabahav.ui.theme.AccentOrange,
            trendPercent = alertTimestamps.dayOverDayPercentChange(),
            sparklineValues = alertTimestamps.lastNDaysCounts(7),
            modifier = cardWidth,
        )
        StatCard(
            title = "Today's Detections",
            value = todaysDetections.toString(),
            icon = Icons.Filled.CalendarToday,
            accentColor = com.example.faunabahav.ui.theme.PrimaryGreen,
            subtitle = "Last updated ${com.example.faunabahav.ui.util.formatTimeOnly(Clock.System.now())}",
            modifier = cardWidth,
        )
    }
}

/** Derived client-side from real observation timestamps, since /dashboard/summary has no
 * "today" field — same reasoning already used for the Analytics events-over-time chart. */
private fun todaysDetectionCount(observations: List<Observation>): Int {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    return observations.count { it.timestamp.toLocalDateTime(TimeZone.UTC).date == today }
}
