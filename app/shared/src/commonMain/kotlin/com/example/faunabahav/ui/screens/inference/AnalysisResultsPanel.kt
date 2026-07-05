package com.example.faunabahav.ui.screens.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.InferenceResult
import com.example.faunabahav.model.Outcome
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.ObservationThumbnail
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.deterrenceExplanation
import com.example.faunabahav.ui.util.deterrenceLabel
import com.example.faunabahav.ui.util.actionStatusExplanation
import com.example.faunabahav.ui.util.actionStatusLabel
import com.example.faunabahav.ui.util.formatTimestamp
import com.example.faunabahav.ui.util.outcomeExplanation
import com.example.faunabahav.ui.util.outcomeLabel
import com.example.faunabahav.ui.util.riskExplanation
import com.example.faunabahav.ui.util.toFrameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

@Composable
fun AnalysisResultsPanel(
    submitState: SubmitState<InferenceResult>,
    stages: List<String>,
    stageIndex: Int,
    baseUrl: String,
    device: Device?,
    onViewAllObservations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        Column(Modifier.padding(20.dp)) {
            when (submitState) {
                is SubmitState.Idle -> IdleState()
                is SubmitState.Submitting -> LoadingState(stages, stageIndex)
                is SubmitState.Error -> ErrorState(submitState.message)
                is SubmitState.Success -> when (val result = submitState.data) {
                    is InferenceResult.NoSpeciesDetected -> NoSpeciesState()
                    is InferenceResult.Detected -> DetectedState(result, baseUrl, device, onViewAllObservations)
                }
            }
        }
    }
}

@Composable
private fun IdleState() {
    Box(Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Pets,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No analysis yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Select a device, upload a file, and press Analyze.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingState(stages: List<String>, stageIndex: Int) {
    Text("Analysis Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(20.dp))
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = PrimaryGreen)
        Spacer(Modifier.height(24.dp))
        stages.forEachIndexed { index, stage ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    index < stageIndex -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    index == stageIndex -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else -> Box(Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (index <= stageIndex) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (index == stageIndex) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Text("Analysis Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NoSpeciesState() {
    Text("Analysis Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("No wildlife detected in this upload.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DetectedState(
    result: InferenceResult.Detected,
    baseUrl: String,
    device: Device?,
    onViewAllObservations: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Analysis Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Surface(color = PrimaryGreen.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen)
                }
            }
        }
        val timestamp = result.timestamp
        if (timestamp != null) {
            Text(
                formatTimestamp(timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    val framePath = result.framePath
    val speciesLabel = "${result.species.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }} " +
        "${(result.speciesConfidence * 100).toInt()}%"

    if (rememberIsWideScreenLocal()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (framePath != null) {
                ObservationThumbnail(
                    url = framePath.toFrameUrl(baseUrl),
                    modifier = Modifier.weight(1.3f).height(280.dp),
                    boundingBox = result.boundingBox,
                    frameWidth = result.frameWidth,
                    frameHeight = result.frameHeight,
                    boxColor = PrimaryGreen,
                    label = if (result.boundingBox != null) speciesLabel else null,
                )
            }
            DetectionInfoCard(result, device, modifier = Modifier.weight(1f))
        }
    } else {
        Column {
            if (framePath != null) {
                ObservationThumbnail(
                    url = framePath.toFrameUrl(baseUrl),
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    boundingBox = result.boundingBox,
                    frameWidth = result.frameWidth,
                    frameHeight = result.frameHeight,
                    boxColor = PrimaryGreen,
                    label = if (result.boundingBox != null) speciesLabel else null,
                )
                Spacer(Modifier.height(16.dp))
            }
            DetectionInfoCard(result, device, modifier = Modifier.fillMaxWidth())
        }
    }

    Spacer(Modifier.height(16.dp))

    val cardRowModifier = Modifier.fillMaxWidth()
    if (rememberIsWideScreenLocal()) {
        Row(cardRowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RiskLevelCard(result, Modifier.weight(1f))
            DeterrenceCard(result, Modifier.weight(1f))
            ActionStatusCard(result, Modifier.weight(1f))
            OutcomeCard(result, Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RiskLevelCard(result, Modifier.fillMaxWidth())
            DeterrenceCard(result, Modifier.fillMaxWidth())
            ActionStatusCard(result, Modifier.fillMaxWidth())
            OutcomeCard(result, Modifier.fillMaxWidth())
        }
    }

    Spacer(Modifier.height(16.dp))
    ConfidenceBreakdownCard(result)

    Spacer(Modifier.height(16.dp))
    HorizontalDividerFull()
    Spacer(Modifier.height(12.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("What happens next?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    "This observation has been saved and will help improve future AI predictions. " +
                        "You will be notified if further action is required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onViewAllObservations) {
            Text("View All Observations")
        }
    }
}

@Composable
private fun HorizontalDividerFull() {
    androidx.compose.material3.HorizontalDivider()
}

@Composable
private fun rememberIsWideScreenLocal(): Boolean = com.example.faunabahav.ui.navigation.rememberIsWideScreen()

@Composable
private fun DetectionInfoCard(result: InferenceResult.Detected, device: Device?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp)) {
            InfoRow("Animal Species", result.species.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() })
            InfoRow("Behaviour", result.behaviourCategory?.name?.lowercase()?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: "N/A (image)")
            InfoRow("Confidence Level", result.speciesConfidence.toPercentOrDash())
            InfoRow("Camera / Device", device?.let { "${it.name} - ${it.location}" } ?: "—")
            InfoRow("Zone", device?.location ?: "—")
            InfoRow("Timestamp", result.timestamp?.let { formatTimestamp(it) } ?: "—")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RiskLevelCard(result: InferenceResult.Detected, modifier: Modifier = Modifier) {
    val color = when (result.riskLevel) {
        RiskLevel.HIGH -> DangerRed
        RiskLevel.MEDIUM -> AccentOrange
        RiskLevel.LOW -> PrimaryGreen
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    MetricCard(
        icon = Icons.Filled.Shield,
        title = "Risk Level",
        modifier = modifier,
    ) {
        Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
            Text(
                result.riskLevel?.name ?: "N/A",
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            riskExplanation(result.riskLevel, result.species, result.behaviourCategory),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeterrenceCard(result: InferenceResult.Detected, modifier: Modifier = Modifier) {
    MetricCard(icon = Icons.Filled.Campaign, title = "Deterrence Action", modifier = modifier) {
        Text(
            deterrenceLabel(result.actions),
            color = AccentOrange,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            deterrenceExplanation(result.actions),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionStatusCard(result: InferenceResult.Detected, modifier: Modifier = Modifier) {
    MetricCard(icon = Icons.Filled.Bolt, title = "Action Status", modifier = modifier) {
        Surface(color = PrimaryGreen.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
            Text(
                actionStatusLabel(result.actions),
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            actionStatusExplanation(result.actions),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OutcomeCard(result: InferenceResult.Detected, modifier: Modifier = Modifier) {
    val color = when (result.outcome) {
        Outcome.DETERRENCE_ACTIVATED -> PrimaryGreen
        Outcome.FARMER_INTERVENTION_REQUIRED -> DangerRed
        Outcome.MONITORING_CONTINUES -> AccentOrange
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    MetricCard(icon = Icons.Filled.CheckCircle, title = "Outcome", modifier = modifier) {
        Text(outcomeLabel(result.outcome), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            outcomeExplanation(result.outcome),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ConfidenceBreakdownCard(result: InferenceResult.Detected) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Confidence Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            val behaviourConfidence = result.behaviourConfidence
            val overall = if (behaviourConfidence != null) {
                (result.speciesConfidence + behaviourConfidence) / 2.0
            } else {
                result.speciesConfidence
            }

            if (rememberIsWideScreenLocal()) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ConfidenceBar("Animal Detection (YOLOv8)", result.speciesConfidence, Modifier.weight(1f))
                    ConfidenceBar("Behaviour Classification (R3D-18)", result.behaviourConfidence, Modifier.weight(1f))
                    ConfidenceBar("Overall Confidence", overall, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ConfidenceBar("Animal Detection (YOLOv8)", result.speciesConfidence, Modifier.fillMaxWidth())
                    ConfidenceBar("Behaviour Classification (R3D-18)", result.behaviourConfidence, Modifier.fillMaxWidth())
                    ConfidenceBar("Overall Confidence", overall, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBar(label: String, value: Double?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value.toPercentOrDash(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { value?.toFloat() ?: 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = PrimaryGreen,
        )
    }
}
