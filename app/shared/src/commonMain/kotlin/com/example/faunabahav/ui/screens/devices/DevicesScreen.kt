package com.example.faunabahav.ui.screens.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.faunabahav.model.Device
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.observations.EmptyState

@Composable
fun DevicesScreen(
    repository: DeviceRepository,
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Devices", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        UiStateContent(state = state, modifier = Modifier.weight(1f), onRetry = viewModel::load) { devices ->
            if (devices.isEmpty()) {
                EmptyState("No devices registered")
            } else {
                Column(Modifier.fillMaxSize()) {
                    DeviceStatCardRow(devices)
                    Spacer(Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(devices, key = { it.id }) { device ->
                            DeviceCard(device)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceStatCardRow(devices: List<Device>) {
    val online = devices.count { it.status.lowercase() in setOf("online", "active") }
    val offline = devices.size - online

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val cardWidth = Modifier.width(160.dp)
        StatCard("Total devices", devices.size.toString(), Icons.Filled.Devices, cardWidth)
        StatCard("Online", online.toString(), Icons.Filled.CheckCircle, cardWidth)
        StatCard("Offline", offline.toString(), Icons.Filled.Sensors, cardWidth)
    }
}

@Composable
private fun DeviceCard(device: Device) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(device.name, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            device.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            StatusBadge(device.status)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = statusColor(status)
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            status,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun statusColor(status: String): Color = when (status.lowercase()) {
    "online", "active" -> MaterialTheme.colorScheme.primary
    "offline", "inactive" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
