package com.example.faunabahav.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.model.User
import com.example.faunabahav.ui.components.UserProfileCard
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.DarkGreen
import com.example.faunabahav.ui.theme.ForestGreen
import com.example.faunabahav.ui.theme.LightGreen

@Composable
fun AppSidebar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    user: User?,
    deviceRepository: DeviceRepository,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    // Same shared instance the Devices screen/Dashboard use (see RecentAlertsPanel's note) —
    // reads real device status without triggering a duplicate fetch.
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Brush.verticalGradient(listOf(ForestGreen, DarkGreen))),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("FaunaBehav", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Protecting Farms, Respecting Wildlife",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            visibleDestinations(Destination.entries, user).forEach { dest ->
                SidebarItem(
                    destination = dest,
                    isSelected = dest == selected,
                    onClick = { onSelect(dest) },
                )
            }
        }

        UserProfileCard(user = user, onLogout = onLogout)
        SystemStatusCard(devicesViewModel)
    }
}

/** "All systems operational" is only ever shown when every real registered device actually
 *  reports an active status — otherwise it honestly names how many are offline. */
@Composable
private fun SystemStatusCard(devicesViewModel: DevicesViewModel) {
    val state by devicesViewModel.uiState.collectAsStateWithLifecycle()
    val devices = (state as? UiState.Success)?.data

    val (label, isHealthy) = when {
        devices == null -> "Checking status…" to true
        devices.isEmpty() -> "No cameras registered" to false
        else -> {
            val offline = devices.count { it.status.trim().lowercase() !in setOf("online", "active") }
            if (offline == 0) "All systems operational" to true else "$offline of ${devices.size} cameras offline" to false
        }
    }
    val color = if (isHealthy) LightGreen else DangerRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("System Status", color = Color.White, style = MaterialTheme.typography.labelSmall)
            Text(label, color = color, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SidebarItem(destination: Destination, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(destination.icon, contentDescription = destination.label, tint = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(destination.label, color = Color.White)
    }
}
