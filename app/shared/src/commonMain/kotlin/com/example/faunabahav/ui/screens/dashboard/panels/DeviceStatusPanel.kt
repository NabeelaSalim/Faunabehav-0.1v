package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.formatTimeOnly

/**
 * Reuses the same DevicesViewModel/ObservationsViewModel instances the standalone Devices/
 * Observations screens use — see the note in RecentAlertsPanel about why that avoids a
 * duplicate fetch. There's no real per-camera signal-strength field in the devices table, so
 * this shows a real status-derived connectivity icon instead of fabricated signal bars.
 */
@Composable
fun DeviceStatusPanel(
    repository: DeviceRepository,
    observationRepository: ObservationRepository,
    modifier: Modifier = Modifier,
    onViewAllDevices: () -> Unit = {},
    viewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(repository) } },
    ),
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Camera Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onViewAllDevices) { Text("View All Devices →") }
            }
            UiStateContent(state = state, modifier = Modifier.height(160.dp), onRetry = viewModel::load) { devices ->
                if (devices.isEmpty()) {
                    Text("No devices registered", style = MaterialTheme.typography.bodySmall)
                } else {
                    val observations = (observationsState as? UiState.Success)?.data ?: emptyList()
                    val lastDetectionByDevice = observations.groupBy { it.deviceId }
                        .mapValues { (_, obs) -> obs.maxByOrNull { it.timestamp } }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        devices.forEach { device ->
                            DeviceCard(device, lastDetectionByDevice[device.id]?.timestamp, modifier = Modifier.width(160.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(device: Device, lastDetection: kotlin.time.Instant?, modifier: Modifier = Modifier) {
    val isLive = device.status.trim().lowercase() in setOf("online", "active")
    val color = if (isLive) PrimaryGreen else DangerRed

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(device.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(device.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (isLive) Icons.Filled.SignalWifi4Bar else Icons.Filled.SignalWifiOff,
                    contentDescription = if (isLive) "Online" else "Offline",
                    tint = color,
                )
            }
            Spacer(Modifier.height(8.dp))
            Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                Text(
                    if (isLive) "LIVE" else "OFFLINE",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Last detection: ${lastDetection?.let { formatTimeOnly(it) } ?: "—"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
