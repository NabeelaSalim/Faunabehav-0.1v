package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.devices.DevicesViewModel

/**
 * Reuses the same DevicesViewModel instance the standalone Devices screen uses — see the note
 * in RecentAlertsPanel about why that avoids a duplicate fetch.
 */
@Composable
fun DeviceStatusPanel(
    repository: DeviceRepository,
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Device Status", style = MaterialTheme.typography.titleMedium)
            UiStateContent(state = state, modifier = Modifier.height(180.dp), onRetry = viewModel::load) { devices ->
                if (devices.isEmpty()) {
                    Text("No devices registered")
                } else {
                    Column {
                        devices.forEach { device -> DeviceRow(device) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: Device) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(device.name, fontWeight = FontWeight.Bold)
            Text(device.location, style = MaterialTheme.typography.bodySmall)
        }
        StatusDot(device.status)
    }
}

@Composable
private fun StatusDot(status: String) {
    val color = when (status.lowercase()) {
        "online", "active" -> MaterialTheme.colorScheme.primary
        "offline", "inactive" -> MaterialTheme.colorScheme.error
        else -> Color(0xFFF57C00)
    }
    Text(status, color = color, fontWeight = FontWeight.Bold)
}
