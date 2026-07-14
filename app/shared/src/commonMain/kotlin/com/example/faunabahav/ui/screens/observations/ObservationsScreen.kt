package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.util.ObservationFilterState
import com.example.faunabahav.ui.util.TrendPeriod
import com.example.faunabahav.ui.util.applyFilters
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
fun ObservationsScreen(
    repository: ObservationRepository,
    deviceRepository: DeviceRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    viewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(repository) } },
    ),
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()

    var filters by remember { mutableStateOf(ObservationFilterState()) }
    var currentPage by remember { mutableStateOf(0) }
    var itemsPerPage by remember { mutableStateOf(12) }
    var selectedObservation by remember { mutableStateOf<Observation?>(null) }
    var topPeriod by remember { mutableStateOf(TrendPeriod.TODAY) }
    var lastLoadedAt by remember { mutableStateOf(Clock.System.now()) }
    var secondsSinceLoad by remember { mutableStateOf(0L) }

    LaunchedEffect(state) {
        if (state is UiState.Success) lastLoadedAt = Clock.System.now()
    }
    LaunchedEffect(lastLoadedAt) {
        while (true) {
            secondsSinceLoad = (Clock.System.now() - lastLoadedAt).inWholeSeconds
            delay(1000)
        }
    }

    val devices = (devicesState as? UiState.Success)?.data ?: emptyList()
    val deviceNames = devices.associate { it.id to it.name }
    val deviceZones = devices.associate { it.id to it.location }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        ObservationsHeader(
            secondsSinceLoad = secondsSinceLoad,
            period = topPeriod,
            onPeriodChange = { topPeriod = it },
            onRefresh = { viewModel.load(); devicesViewModel.load() },
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        when (state) {
            is UiState.Loading -> ObservationsLoadingSkeleton()
            is UiState.Error -> ObservationsErrorState(
                message = (state as UiState.Error).message,
                onRetry = { viewModel.load() },
            )
            is UiState.Success -> {
                val allObservations = (state as UiState.Success<List<Observation>>).data
                val periodScoped = allObservations.filterByPeriod(topPeriod)

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    ObservationStatCards(periodScoped, periodLabel = topPeriod.label)
                    Spacer(Modifier.height(16.dp))

                    ObservationFilterBar(
                        filters = filters,
                        onFiltersChange = { filters = it; currentPage = 0 },
                        devices = devices,
                    )
                    Spacer(Modifier.height(16.dp))

                    val filtered = allObservations.applyFilters(filters, deviceNames, deviceZones)

                    if (filtered.isEmpty()) {
                        ObservationsEmptyState(
                            hasAnyData = allObservations.isNotEmpty(),
                            onResetFilters = { filters = ObservationFilterState() },
                        )
                    } else {
                        val pageStart = currentPage * itemsPerPage
                        val pageItems = filtered.drop(pageStart).take(itemsPerPage)

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(260.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height((((pageItems.size + 3) / 4) * 420).dp).fillMaxWidth(),
                        ) {
                            items(pageItems, key = { it.id }) { observation ->
                                ObservationCard(
                                    observation = observation,
                                    cameraName = deviceNames[observation.deviceId] ?: "Device #${observation.deviceId}",
                                    zone = deviceZones[observation.deviceId] ?: "—",
                                    baseUrl = baseUrl,
                                    onViewDetails = { selectedObservation = observation },
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        ObservationPaginationBar(
                            currentPage = currentPage,
                            itemsPerPage = itemsPerPage,
                            totalItems = filtered.size,
                            onPageChange = { currentPage = it },
                            onItemsPerPageChange = { itemsPerPage = it; currentPage = 0 },
                        )
                    }
                }
            }
        }
    }

    selectedObservation?.let { observation ->
        ObservationDetailsModal(
            observation = observation,
            cameraName = deviceNames[observation.deviceId] ?: "Device #${observation.deviceId}",
            zone = deviceZones[observation.deviceId] ?: "—",
            baseUrl = baseUrl,
            onDismiss = { selectedObservation = null },
        )
    }
}

@Composable
private fun ObservationsHeader(
    secondsSinceLoad: Long,
    period: TrendPeriod,
    onPeriodChange: (TrendPeriod) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column {
            Text("Observation History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Browse, search and analyze every wildlife observation captured by the system.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Last updated: ${secondsSinceLoad}s ago",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Refresh")
            }
            Spacer(Modifier.width(8.dp))
            com.example.faunabahav.ui.components.charts.PeriodToggle(selected = period, onSelect = onPeriodChange)
        }
    }
}

@Composable
private fun ObservationsLoadingSkeleton() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(80.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading observations…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ObservationsErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(60.dp))
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun ObservationsEmptyState(hasAnyData: Boolean, onResetFilters: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text("No observations found.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            if (hasAnyData) "No observations match your current filters." else "No observations have been captured yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasAnyData) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onResetFilters) { Text("Reset Filters") }
        }
    }
}

/** Reused by AlertsScreen/DevicesScreen/FeedbackScreen for their own empty states — keep this
 *  signature stable since those screens import it from this package. */
@Composable
internal fun EmptyState(message: String) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun List<Observation>.filterByPeriod(period: TrendPeriod): List<Observation> {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.UTC).date
    return when (period) {
        TrendPeriod.TODAY -> filter { it.timestamp.toLocalDateTime(TimeZone.UTC).date == today }
        TrendPeriod.SEVEN_DAYS -> {
            val start = today.minus(6, DateTimeUnit.DAY)
            filter { it.timestamp.toLocalDateTime(TimeZone.UTC).date >= start }
        }
        TrendPeriod.THIRTY_DAYS -> {
            val start = today.minus(29, DateTimeUnit.DAY)
            filter { it.timestamp.toLocalDateTime(TimeZone.UTC).date >= start }
        }
    }
}
