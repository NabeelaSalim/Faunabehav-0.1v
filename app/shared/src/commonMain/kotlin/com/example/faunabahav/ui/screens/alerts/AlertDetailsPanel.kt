package com.example.faunabahav.ui.screens.alerts

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.faunabahav.model.Alert
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.color
import com.example.faunabahav.ui.util.displayId
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote
import com.example.faunabahav.ui.util.formatTimestamp
import com.example.faunabahav.ui.util.toActionChips
import com.example.faunabahav.ui.util.toFrameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

/**
 * Content-only (no Dialog/Surface chrome) so the caller can either dock it in a persistent
 * side panel on wide screens or wrap it in a `Dialog` full-screen on narrow ones — same
 * responsive split `ObservationDetailsModal` uses via `rememberIsWideScreen()`, just applied at
 * the call site instead of inside this composable.
 */
@Composable
fun AlertDetailsPanel(
    alert: Alert,
    baseUrl: String,
    onClose: () -> Unit,
    onOpenObservation: () -> Unit,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit,
    onSubmitFeedback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isResolved = alert.status.equals("resolved", ignoreCase = true)
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Alert Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
        }

        if (alert.framePath != null) {
            ZoomableFrame(alert, baseUrl)
        }

        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${alert.species.displayName().uppercase()} DETECTED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                RiskBadge(alert.riskLevel)
            }
            Spacer(Modifier.height(12.dp))

            DetailRow("Behaviour", alert.behaviourCategory.displayNameOrImageNote())
            DetailRow("Confidence", alert.confidence.toPercentOrDash())
            if (alert.speciesConfidence != null) {
                DetailRow("Species Confidence", alert.speciesConfidence.toPercentOrDash())
            }
            DetailRow("Camera", alert.camera ?: "Unknown camera")
            DetailRow("Zone", alert.location)
            DetailRow("Time", formatTimestamp(alert.timestamp))
            DetailRow("Risk Level", alert.riskLevel.name)
            DetailRow("Deterrence Action", alert.deterrenceAction.toActionChips().joinToString(", ") { it.label })
            DetailRow("Alert ID", alert.displayId())

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Event Timeline", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            // Only real timestamps we actually have — no fabricated per-stage pipeline steps.
            // Acknowledged/Resolved have no stored timestamp yet (backend doesn't track one),
            // so those two entries intentionally show no time rather than inventing one.
            TimelineEntry(dotColor = alert.riskLevel.color(), label = "Alert triggered", detail = formatTimestamp(alert.timestamp))
            if (alert.acknowledgedBy != null) {
                TimelineEntry(dotColor = MaterialTheme.colorScheme.secondary, label = "Acknowledged", detail = null)
            }
            if (isResolved) {
                TimelineEntry(dotColor = PrimaryGreen, label = "Resolved", detail = null)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Quick Actions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenObservation, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Observation")
            }
            Spacer(Modifier.height(8.dp))
            if (alert.acknowledgedBy == null) {
                OutlinedButton(onClick = onAcknowledge, modifier = Modifier.fillMaxWidth()) {
                    Text("Acknowledge Alert")
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { showFeedbackDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Submit Feedback")
            }
            if (!isResolved) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onResolve,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Mark as Resolved")
                }
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = {
                onSubmitFeedback(it)
                showFeedbackDialog = false
            },
        )
    }
}

@Composable
private fun ZoomableFrame(alert: Alert, baseUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
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
            url = requireNotNull(alert.framePath).toFrameUrl(baseUrl),
            modifier = Modifier.fillMaxWidth().height(240.dp).graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY,
            ),
            boundingBox = alert.boundingBox,
            frameWidth = alert.frameWidth,
            frameHeight = alert.frameHeight,
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

@Composable
private fun TimelineEntry(dotColor: Color, label: String, detail: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        if (detail != null) {
            Spacer(Modifier.width(6.dp))
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeedbackDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Submit Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Correct the detected behaviour if the AI got it wrong — this trains future accuracy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Corrected behaviour") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (text.isNotBlank()) onSubmit(text) }) { Text("Submit") }
                }
            }
        }
    }
}
