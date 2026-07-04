package com.example.faunabahav.ui.screens.inference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.filepicker.PickedFile
import com.example.faunabahav.filepicker.rememberFilePicker
import com.example.faunabahav.model.InferenceResult
import com.example.faunabahav.ui.components.PrimaryButton
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen

@Composable
fun InferenceScreen(
    repository: ObservationRepository,
    modifier: Modifier = Modifier,
    viewModel: InferenceViewModel = viewModel(
        factory = viewModelFactory { initializer { InferenceViewModel(repository) } },
    ),
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    var deviceIdText by remember { mutableStateOf("") }
    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
    val filePicker = rememberFilePicker { picked -> pickedFile = picked }

    val isSubmitting = submitState is SubmitState.Submitting
    val deviceId = deviceIdText.toIntOrNull()
    val canSubmit = deviceId != null && pickedFile != null && !isSubmitting

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Upload Observation", style = MaterialTheme.typography.headlineSmall)
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Manually submit a photo or video for AI analysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = deviceIdText,
                    onValueChange = { deviceIdText = it.filter(Char::isDigit) },
                    label = { Text("Device ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))

                UploadDropZone(pickedFile = pickedFile, onClick = { filePicker.launch() })
                Spacer(Modifier.height(16.dp))

                if (submitState is SubmitState.Error) {
                    Text(
                        (submitState as SubmitState.Error).message,
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                PrimaryButton(
                    text = "Submit",
                    onClick = {
                        val file = pickedFile
                        if (deviceId != null && file != null) {
                            viewModel.submitObservation(deviceId, file.bytes, file.fileName, file.contentType)
                        }
                    },
                    enabled = canSubmit,
                    loading = isSubmitting,
                )
            }
        }

        val successState = submitState as? SubmitState.Success
        if (successState != null) {
            Spacer(Modifier.height(16.dp))
            ResultCard(successState.data)
        }
    }
}

@Composable
private fun UploadDropZone(pickedFile: PickedFile?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (pickedFile != null) Icons.Filled.InsertDriveFile else Icons.Filled.CloudUpload,
            contentDescription = null,
            tint = PrimaryGreen,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            pickedFile?.fileName ?: "Select image or video",
            color = if (pickedFile != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultCard(result: InferenceResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            when (result) {
                is InferenceResult.Detected -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp)
                                    .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryGreen)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(result.species.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        RiskBadge(result.riskLevel)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Behaviour: ${result.behaviourCategory.name}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Species confidence: ${(result.speciesConfidence * 100).toInt()}% · " +
                            "Behaviour confidence: ${(result.behaviourConfidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(result.message, style = MaterialTheme.typography.bodyMedium)
                    if (result.actions.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Deterrence: ${result.actions.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is InferenceResult.NoSpeciesDetected -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("No wildlife detected in this upload.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
