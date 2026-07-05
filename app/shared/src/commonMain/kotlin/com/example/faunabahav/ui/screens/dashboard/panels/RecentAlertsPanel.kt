package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.alerts.AlertsViewModel
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.emoji
import com.example.faunabahav.ui.util.formatTimeOnly

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
    onViewAllAlerts: () -> Unit = {},
    viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recent Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onViewAllAlerts) { Text("View All Alerts →") }
            }
            Spacer(Modifier.height(4.dp))
            UiStateContent(state = state, modifier = Modifier.height(380.dp), onRetry = viewModel::load) { alerts ->
                val recent = alerts.sortedByDescending { it.timestamp }.take(5)
                if (recent.isEmpty()) {
                    Text("No active alerts", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column {
                        recent.forEach { alert -> AlertRow(alert) }
                    }
                }
            }
        }
    }
}

private fun riskColor(riskLevel: RiskLevel): Color = when (riskLevel) {
    RiskLevel.HIGH -> DangerRed
    RiskLevel.MEDIUM -> AccentOrange
    RiskLevel.LOW -> PrimaryGreen
}

@Composable
private fun AlertRow(alert: Alert) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            val color = riskColor(alert.riskLevel)
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (alert.riskLevel == RiskLevel.HIGH) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                } else {
                    Text(alert.species.emoji(), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("${alert.species.displayName()} detected", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    alert.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            RiskPill(alert.riskLevel)
            Spacer(Modifier.height(4.dp))
            Text(
                formatTimeOnly(alert.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RiskPill(riskLevel: RiskLevel) {
    val color = riskColor(riskLevel)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
        Text(
            riskLevel.name,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
