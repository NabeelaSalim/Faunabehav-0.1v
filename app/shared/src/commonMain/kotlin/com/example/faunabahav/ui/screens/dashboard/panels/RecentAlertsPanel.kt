package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.alerts.AlertsViewModel

/**
 * Reuses the same AlertsViewModel instance the standalone Alerts screen uses — this app has no
 * NavHost, so both call sites share one ViewModelStore under the default key, meaning switching
 * between Dashboard and Alerts doesn't re-fetch. If per-destination navigation is added later,
 * this sharing stops silently.
 */
@Composable
fun RecentAlertsPanel(
    repository: AlertRepository,
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recent Alerts", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {}) { Text("View All") }
            }
            Spacer(Modifier.height(8.dp))
            UiStateContent(state = state, modifier = Modifier.height(220.dp), onRetry = viewModel::load) { alerts ->
                val recent = alerts.sortedByDescending { it.timestamp }.take(5)
                if (recent.isEmpty()) {
                    Text("No active alerts")
                } else {
                    Column {
                        recent.forEach { alert -> AlertRow(alert) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertRow(alert: Alert) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(alert.species.name, fontWeight = FontWeight.Bold)
            Text(alert.location, style = MaterialTheme.typography.bodySmall)
        }
        RiskBadge(alert.riskLevel)
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    val color = when (riskLevel) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
        RiskLevel.LOW -> MaterialTheme.colorScheme.primary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            riskLevel.name,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
