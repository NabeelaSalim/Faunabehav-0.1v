package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.color
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote
import com.example.faunabahav.ui.util.emoji
import com.example.faunabahav.ui.util.formatTimeOnly
import com.example.faunabahav.ui.util.icon
import com.example.faunabahav.ui.util.riskTint
import com.example.faunabahav.ui.util.toActionChips
import com.example.faunabahav.ui.util.toFrameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

@Composable
fun ObservationCard(
    observation: Observation,
    cameraName: String,
    zone: String,
    baseUrl: String,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                ObservationThumbnail(
                    url = observation.framePath.toFrameUrl(baseUrl),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    boundingBox = observation.boundingBox,
                    frameWidth = observation.frameWidth,
                    frameHeight = observation.frameHeight,
                    boxColor = PrimaryGreen,
                )
                observation.riskLevel?.let { risk ->
                    Surface(
                        color = risk.color(),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    ) {
                        Text(
                            risk.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(cameraName, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(zone, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(observation.species.emoji(), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(observation.species.displayName(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val behaviour = observation.behaviourCategory
                    val tint = behaviour?.riskTint()?.color() ?: MaterialTheme.colorScheme.onSurfaceVariant
                    if (behaviour != null) {
                        Icon(behaviour.icon(), contentDescription = null, tint = tint, modifier = Modifier.height(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        behaviour?.displayNameOrImageNote() ?: "N/A (image)",
                        color = tint,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(10.dp))
                ConfidenceRow("Species Confidence", observation.speciesConfidence)
                Spacer(Modifier.height(4.dp))
                ConfidenceRow("Behaviour Confidence", observation.confidence)

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(formatTimeOnly(observation.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(zone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(8.dp))
                Text("System Action", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                observation.deterrenceAction.toActionChips().forEach { chip ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(chip.icon, contentDescription = null, tint = chip.color, modifier = Modifier.height(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(chip.label, color = chip.color, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                TextButton(onClick = onViewDetails, modifier = Modifier.fillMaxWidth()) {
                    Text("View Details >")
                }
            }
        }
    }
}

@Composable
private fun ConfidenceRow(label: String, value: Double?) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toPercentOrDash(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { value?.toFloat() ?: 0f },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = PrimaryGreen,
        )
    }
}
