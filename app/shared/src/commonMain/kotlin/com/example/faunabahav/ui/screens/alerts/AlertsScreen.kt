package com.example.faunabahav.ui.screens.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.components.RiskFilterRow
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.observations.EmptyState
import com.example.faunabahav.ui.util.formatTimestamp

@Composable
fun AlertsScreen(
    repository: AlertRepository,
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedRisk by remember { mutableStateOf<RiskLevel?>(null) }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Alerts", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        UiStateContent(state = state, modifier = Modifier.weight(1f), onRetry = viewModel::load) { alerts ->
            val filtered = selectedRisk?.let { risk -> alerts.filter { it.riskLevel == risk } } ?: alerts

            Column(Modifier.fillMaxSize()) {
                RiskFilterRow(selected = selectedRisk, onSelect = { selectedRisk = it })
                Spacer(Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    EmptyState(
                        if (alerts.isEmpty()) {
                            "No active alerts — the system is handling detections automatically."
                        } else {
                            "No alerts match this filter"
                        },
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered.sortedByDescending { it.timestamp }, key = { it.id }) { alert ->
                            AlertCard(alert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: Alert) {
    val containerColor = if (alert.riskLevel == RiskLevel.HIGH) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        CardDefaults.cardColors().containerColor
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(alert.species.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                RiskBadge(alert.riskLevel)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Behaviour: ${alert.behaviourCategory.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Deterrence: ${alert.deterrenceAction}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        alert.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "${(alert.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(alert.status)
                Text(
                    formatTimestamp(alert.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = if (status.equals("active", ignoreCase = true)) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
        Text(
            status,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
