package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.Observation
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.components.RiskFilterRow
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.util.formatTimestamp
import com.example.faunabahav.ui.util.frameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

@Composable
fun ObservationsScreen(
    repository: ObservationRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    viewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(repository) } },
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
            Text("Observations", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        UiStateContent(state = state, modifier = Modifier.weight(1f), onRetry = viewModel::load) { observations ->
            val filtered = selectedRisk?.let { risk -> observations.filter { it.riskLevel == risk } } ?: observations

            Column(Modifier.fillMaxSize()) {
                RiskFilterRow(selected = selectedRisk, onSelect = { selectedRisk = it })
                Spacer(Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    EmptyState(
                        if (observations.isEmpty()) "No observations yet" else "No observations match this filter",
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(240.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(filtered.sortedByDescending { it.timestamp }, key = { it.id }) { observation ->
                            ObservationCard(observation, baseUrl)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(observation: Observation, baseUrl: String) {
    Card(Modifier.fillMaxWidth()) {
        Column {
            ObservationThumbnail(
                url = observation.frameUrl(baseUrl),
                modifier = Modifier.fillMaxWidth().height(140.dp),
                boundingBox = observation.boundingBox,
                frameWidth = observation.frameWidth,
                frameHeight = observation.frameHeight,
            )
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(observation.species.name, fontWeight = FontWeight.Bold)
                    RiskBadge(observation.riskLevel)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Behaviour: ${observation.behaviourCategory?.name ?: "N/A (image)"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Deterrence: ${observation.deterrenceAction ?: "N/A (image)"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Device #${observation.deviceId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        observation.confidence.toPercentOrDash(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    formatTimestamp(observation.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun EmptyState(message: String) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
