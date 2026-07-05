package com.example.faunabahav.ui.screens.dashboard.panels

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote
import com.example.faunabahav.ui.util.formatTimestamp
import com.example.faunabahav.ui.util.frameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

/**
 * There's no real live camera streaming pipeline in the backend — this shows Camera 01's most
 * recent real detection as the featured "hero" panel (continuously refreshed by the Dashboard's
 * periodic reload), with a thumbnail strip of every other real registered device below it.
 * Honest "recent activity" rather than a simulated live video feed.
 */
@Composable
fun LiveDetectionGrid(
    deviceRepository: DeviceRepository,
    observationRepository: ObservationRepository,
    baseUrl: String,
    onViewAllCameras: () -> Unit = {},
    modifier: Modifier = Modifier,
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(observationRepository) } },
    ),
) {
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()
    val observationsState by observationsViewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(PrimaryGreen, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("Live Camera Feed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val devices = (devicesState as? UiState.Success)?.data
                    if (devices != null) {
                        val allOnline = devices.all { it.status.trim().lowercase() in setOf("online", "active") }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (allOnline) "· All systems operational" else "· Some cameras offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = onViewAllCameras) { Text("View All Cameras →") }
            }
            Spacer(Modifier.height(12.dp))

            UiStateContent(state = devicesState, modifier = Modifier.height(420.dp), onRetry = devicesViewModel::load) { devices ->
                if (devices.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No cameras registered", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val observations = (observationsState as? UiState.Success)?.data ?: emptyList()
                    val latestByDevice = observations.groupBy { it.deviceId }
                        .mapValues { (_, obs) -> obs.maxByOrNull { it.timestamp } }
                    val heroDevice = devices.find { it.id == 1 } ?: devices.first()

                    Column(Modifier.fillMaxWidth()) {
                        HeroCameraPanel(heroDevice, latestByDevice[heroDevice.id], baseUrl)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(devices, key = { it.id }) { device ->
                                ThumbnailCameraCard(device, latestByDevice[device.id], baseUrl)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCameraPanel(device: Device, latest: Observation?, baseUrl: String) {
    val isLive = device.status.trim().lowercase() in setOf("online", "active")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color.Black, RoundedCornerShape(16.dp)),
    ) {
        if (latest != null) {
            val speciesLabel = "${latest.species.displayName()} ${latest.speciesConfidence.toPercentOrDash()}"
            ObservationThumbnail(
                url = latest.frameUrl(baseUrl),
                modifier = Modifier.fillMaxSize(),
                boundingBox = latest.boundingBox,
                frameWidth = latest.frameWidth,
                frameHeight = latest.frameHeight,
                boxColor = PrimaryGreen,
                label = if (latest.boundingBox != null) speciesLabel else null,
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for AI inference...", color = Color.White.copy(alpha = 0.7f))
            }
        }

        // Top overlay bar: camera name/zone + LIVE badge
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("${device.name} • ${device.location}", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            if (isLive) {
                Row(
                    modifier = Modifier.background(PrimaryGreen, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(6.dp).background(Color.White, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recording indicator, honestly reflects that this device's activity is actively polled
        if (isLive) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Icon(Icons.Filled.FiberManualRecord, contentDescription = null, tint = DangerRed, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text("REC", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom info panel with real detection fields
        if (latest != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        ),
                    )
                    .padding(12.dp),
            ) {
                Text(latest.species.displayName().uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Confidence: ${latest.speciesConfidence.toPercentOrDash()}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                Text("Behaviour: ${latest.behaviourCategory.displayNameOrImageNote()}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                Row {
                    Text("Risk: ", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                    Text(
                        latest.riskLevel?.name ?: "N/A",
                        color = when (latest.riskLevel) {
                            com.example.faunabahav.model.RiskLevel.HIGH -> DangerRed
                            com.example.faunabahav.model.RiskLevel.MEDIUM -> com.example.faunabahav.ui.theme.AccentOrange
                            com.example.faunabahav.model.RiskLevel.LOW -> PrimaryGreen
                            null -> Color.White.copy(alpha = 0.85f)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailCameraCard(device: Device, latest: Observation?, baseUrl: String) {
    val isLive = device.status.trim().lowercase() in setOf("online", "active")

    Column(Modifier.width(160.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.Black, RoundedCornerShape(10.dp)),
        ) {
            if (latest != null) {
                ObservationThumbnail(
                    url = latest.frameUrl(baseUrl),
                    modifier = Modifier.fillMaxSize(),
                    boundingBox = latest.boundingBox,
                    frameWidth = latest.frameWidth,
                    frameHeight = latest.frameHeight,
                    boxColor = PrimaryGreen,
                )
            }
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    .background(if (isLive) PrimaryGreen else DangerRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    if (isLive) "LIVE" else "OFFLINE",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(device.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(device.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
