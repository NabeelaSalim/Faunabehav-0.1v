package com.example.faunabahav.ui.screens.farms

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.faunabahav.data.repository.FarmRepository
import com.example.faunabahav.model.Device
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.observations.EmptyState

@Composable
fun FarmManagementScreen(
    farmRepository: FarmRepository,
    deviceRepository: DeviceRepository,
    modifier: Modifier = Modifier,
    viewModel: FarmManagementViewModel = viewModel(
        factory = viewModelFactory { initializer { FarmManagementViewModel(farmRepository, deviceRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("My Farms", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = viewModel::load) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            UiStateContent(state = state, modifier = Modifier.weight(1f), onRetry = viewModel::load) { farms ->
                if (farms.isEmpty()) {
                    EmptyState("No farms yet. Tap + to add one.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(farms, key = { it.farm.id }) { farmWithDevices ->
                            FarmCard(
                                farmWithDevices = farmWithDevices,
                                onDelete = { viewModel.deleteFarm(farmWithDevices.farm.id) },
                                onToggleDeterrence = { deviceId, isActive ->
                                    viewModel.toggleDeterrence(deviceId, isActive)
                                },
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddFarmDialog(
                onConfirm = { name, location ->
                    viewModel.createFarm(name, location)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false },
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Farm", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun FarmCard(
    farmWithDevices: FarmWithDevices,
    onDelete: () -> Unit,
    onToggleDeterrence: (Int, Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(farmWithDevices.farm.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            farmWithDevices.farm.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete farm", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Devices: ${farmWithDevices.farm.deviceCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (farmWithDevices.devices.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                farmWithDevices.devices.forEach { device ->
                    DeviceRow(device = device, onToggleDeterrence = onToggleDeterrence)
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    onToggleDeterrence: (Int, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Devices,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(device.name, style = MaterialTheme.typography.bodyMedium)
            Text(device.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { onToggleDeterrence(device.id, false) }) {
            Icon(
                Icons.Filled.VolumeUp,
                contentDescription = "Toggle siren",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AddFarmDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Farm") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Farm name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = { onConfirm(name, location) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
