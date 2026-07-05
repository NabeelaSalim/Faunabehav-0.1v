package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.formatTimeOnly
import com.example.faunabahav.ui.util.statusLabel
import com.example.faunabahav.ui.util.toPercentOrDash

private const val MaxRows = 8

/**
 * Only the latest MaxRows real observations — the full history lives on the standalone
 * Observations screen via the "View Full History" button, keeping the Dashboard focused.
 */
@Composable
fun DetectionHistoryTable(
    observationRepository: ObservationRepository,
    deviceRepository: DeviceRepository,
    modifier: Modifier = Modifier,
    onViewFullHistory: () -> Unit = {},
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
) {
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Text("Detection History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        " (Latest)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onViewFullHistory) { Text("View Full History →") }
            }
            Spacer(Modifier.height(12.dp))

            UiStateContent(
                state = observationsState,
                modifier = Modifier.height(420.dp),
                onRetry = observationsViewModel::load,
            ) { observations ->
                if (observations.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No detections yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val devices = (devicesState as? UiState.Success)?.data ?: emptyList()
                    val deviceNames = devices.associate { it.id to it.name }
                    val latest = observations.sortedByDescending { it.timestamp }.take(MaxRows)

                    // The Dashboard page itself already scrolls vertically — only horizontal
                    // scroll is needed here for the wide row of columns.
                    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        HistoryHeaderRow()
                        HorizontalDivider()
                        latest.forEach { observation ->
                            HistoryRow(observation, deviceNames[observation.deviceId] ?: "Device #${observation.deviceId}")
                        }
                    }
                }
            }
        }
    }
}

private val ColumnWidths = listOf(110.dp, 110.dp, 100.dp, 90.dp, 140.dp, 100.dp, 80.dp, 140.dp, 120.dp)

@Composable
private fun HistoryHeaderRow() {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        HeaderCell("Time", ColumnWidths[0])
        HeaderCell("Camera", ColumnWidths[1])
        HeaderCell("Animal", ColumnWidths[2])
        HeaderCell("Animal Conf.", ColumnWidths[3])
        HeaderCell("Behaviour", ColumnWidths[4])
        HeaderCell("Behaviour Conf.", ColumnWidths[5])
        HeaderCell("Risk", ColumnWidths[6])
        HeaderCell("Deterrence", ColumnWidths[7])
        HeaderCell("Status", ColumnWidths[8])
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HistoryRow(observation: Observation, cameraName: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(formatTimeOnly(observation.timestamp), modifier = Modifier.width(ColumnWidths[0]), style = MaterialTheme.typography.bodySmall)
        Text(cameraName, modifier = Modifier.width(ColumnWidths[1]), style = MaterialTheme.typography.bodySmall)
        Text(observation.species.name, modifier = Modifier.width(ColumnWidths[2]), style = MaterialTheme.typography.bodySmall)
        Text(
            observation.speciesConfidence.toPercentOrDash(),
            modifier = Modifier.width(ColumnWidths[3]),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(observation.behaviourCategory?.name ?: "N/A", modifier = Modifier.width(ColumnWidths[4]), style = MaterialTheme.typography.bodySmall)
        Text(
            observation.confidence.toPercentOrDash(),
            modifier = Modifier.width(ColumnWidths[5]),
            style = MaterialTheme.typography.bodySmall,
        )
        Box(Modifier.width(ColumnWidths[6])) { RiskBadge(observation.riskLevel) }
        Text(observation.deterrenceAction ?: "N/A", modifier = Modifier.width(ColumnWidths[7]), style = MaterialTheme.typography.bodySmall)
        StatusText(observation, modifier = Modifier.width(ColumnWidths[8]))
    }
    HorizontalDivider()
}

@Composable
private fun StatusText(observation: Observation, modifier: Modifier = Modifier) {
    val status = observation.statusLabel()
    val color = when (status) {
        "Triggered" -> DangerRed
        "Monitoring" -> AccentOrange
        else -> PrimaryGreen
    }
    Text(status, modifier = modifier, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
}
