package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.color
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote
import com.example.faunabahav.ui.util.formatTimestamp
import com.example.faunabahav.ui.util.outcomeExplanation
import com.example.faunabahav.ui.util.outcomeLabel
import com.example.faunabahav.ui.util.toActionChips
import com.example.faunabahav.ui.util.toFrameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

@Composable
fun ObservationDetailsModal(
    observation: Observation,
    cameraName: String,
    zone: String,
    baseUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 560.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Observation Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                }

                ZoomableFrame(observation, baseUrl)

                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(observation.species.displayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        observation.riskLevel?.let { risk ->
                            Text(risk.name, color = risk.color(), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    DetailRow("Behaviour", observation.behaviourCategory.displayNameOrImageNote())
                    DetailRow("Species Confidence", observation.speciesConfidence.toPercentOrDash())
                    DetailRow("Behaviour Confidence", observation.confidence.toPercentOrDash())
                    DetailRow("Detection Time", formatTimestamp(observation.timestamp))
                    DetailRow("Camera", cameraName)
                    DetailRow("Zone", zone)

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Text("Deterrence", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    val chips = observation.deterrenceAction.toActionChips()
                    if (chips.isEmpty()) {
                        Text("N/A (image)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        chips.forEach { chip ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(chip.icon, contentDescription = null, tint = chip.color, modifier = Modifier.height(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(chip.label, color = chip.color, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Text("Outcome", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(outcomeLabel(observation.outcome), fontWeight = FontWeight.Bold)
                    Text(
                        outcomeExplanation(observation.outcome),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomableFrame(observation: Observation, baseUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(0.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
    ) {
        ObservationThumbnail(
            url = observation.framePath.toFrameUrl(baseUrl),
            modifier = Modifier.fillMaxWidth().height(320.dp).graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY,
            ),
            boundingBox = observation.boundingBox,
            frameWidth = observation.frameWidth,
            frameHeight = observation.frameHeight,
            boxColor = PrimaryGreen,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
