package com.example.faunabahav.ui.screens.dashboard.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.util.frameUrl

/**
 * Reuses the same ObservationsViewModel instance the standalone Observations screen (and the
 * Dashboard analytics panel's Events Over Time chart) use — see the note in RecentAlertsPanel
 * about why that avoids a duplicate fetch.
 */
@Composable
fun RecentObservationsPanel(
    repository: ObservationRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    viewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(repository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Recent Observations", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            UiStateContent(state = state, modifier = Modifier.size(160.dp), onRetry = viewModel::load) { observations ->
                val recent = observations.sortedByDescending { it.timestamp }.take(10)
                if (recent.isEmpty()) {
                    Text("No observations yet")
                } else {
                    LazyRow {
                        items(recent, key = { it.id }) { observation ->
                            ObservationThumbnailCard(observation, baseUrl)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationThumbnailCard(observation: Observation, baseUrl: String) {
    Column(Modifier.padding(end = 12.dp).width(120.dp)) {
        ObservationThumbnail(
            url = observation.frameUrl(baseUrl),
            modifier = Modifier.size(120.dp),
        )
        Text(observation.species.name, style = MaterialTheme.typography.labelSmall)
        Row {
            Text(observation.riskLevel.name, style = MaterialTheme.typography.labelSmall)
        }
    }
}
