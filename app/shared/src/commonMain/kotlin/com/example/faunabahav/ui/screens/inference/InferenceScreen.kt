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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.data.repository.DashboardRepository
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.filepicker.PickedFile
import com.example.faunabahav.filepicker.rememberFilePicker
import com.example.faunabahav.model.Device
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.screens.alerts.AlertsViewModel
import com.example.faunabahav.ui.screens.dashboard.DashboardViewModel
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.ObservationsViewModel
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.toFileSizeLabel
import kotlinx.coroutines.delay

private val IMAGE_STAGES = listOf(
    "Uploading media...",
    "Detecting animal using YOLOv8...",
    "Saving observation...",
)
private val VIDEO_STAGES = listOf(
    "Uploading media...",
    "Detecting animal using YOLOv8...",
    "Classifying behaviour using R3D-18...",
    "Mapping behaviour category...",
    "Assessing risk...",
    "Selecting deterrence action...",
    "Saving observation...",
)

@Composable
fun InferenceScreen(
    repository: ObservationRepository,
    dashboardRepository: DashboardRepository,
    alertRepository: AlertRepository,
    deviceRepository: DeviceRepository,
    baseUrl: String,
    modifier: Modifier = Modifier,
    onViewAllObservations: () -> Unit = {},
    viewModel: InferenceViewModel = viewModel(
        factory = viewModelFactory { initializer { InferenceViewModel(repository) } },
    ),
    // Same shared instances the Dashboard uses (see the note in RecentAlertsPanel) — reloading
    // them here means every Dashboard section reflects a new upload immediately, not after the
    // next poll tick.
    observationsViewModel: ObservationsViewModel = viewModel(
        factory = viewModelFactory { initializer { ObservationsViewModel(repository) } },
    ),
    alertsViewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(alertRepository) } },
    ),
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
    dashboardViewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { DashboardViewModel(dashboardRepository) } },
    ),
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()

    var selectedDeviceId by remember { mutableStateOf<Int?>(null) }
    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
    val filePicker = rememberFilePicker { picked -> pickedFile = picked }

    val isSubmitting = submitState is SubmitState.Submitting
    val canSubmit = selectedDeviceId != null && pickedFile != null && !isSubmitting

    val stages = remember(pickedFile?.contentType) {
        if (pickedFile?.contentType?.startsWith("image/") == true) IMAGE_STAGES else VIDEO_STAGES
    }
    var stageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isSubmitting, stages) {
        if (isSubmitting) {
            stageIndex = 0
            while (stageIndex < stages.lastIndex) {
                delay(700)
                stageIndex++
            }
        }
    }
    val stageLabel = when {
        isSubmitting -> stages.getOrNull(stageIndex)
        submitState is SubmitState.Success -> "Analysis Complete"
        else -> null
    }

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            observationsViewModel.load()
            alertsViewModel.load()
            devicesViewModel.load()
            dashboardViewModel.load()
        }
    }

    val selectedDevice = (devicesState as? UiState.Success)?.data?.find { it.id == selectedDeviceId }

    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Upload Observation", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Upload an image or video and our AI will analyze wildlife activity for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        if (rememberIsWideScreen()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UploadPanel(
                    devicesState = devicesState,
                    selectedDeviceId = selectedDeviceId,
                    onSelectDevice = { selectedDeviceId = it },
                    pickedFile = pickedFile,
                    onPickFile = { filePicker.launch() },
                    onRemoveFile = { pickedFile = null },
                    onSubmit = {
                        val file = pickedFile
                        val deviceId = selectedDeviceId
                        if (file != null && deviceId != null) {
                            viewModel.submitObservation(deviceId, file.bytes, file.fileName, file.contentType)
                        }
                    },
                    canSubmit = canSubmit,
                    isSubmitting = isSubmitting,
                    stageLabel = stageLabel,
                    errorMessage = (submitState as? SubmitState.Error)?.message,
                    modifier = Modifier.width(400.dp),
                )
                AnalysisResultsPanel(
                    submitState = submitState,
                    stages = stages,
                    stageIndex = stageIndex,
                    baseUrl = baseUrl,
                    device = selectedDevice,
                    onViewAllObservations = onViewAllObservations,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UploadPanel(
                    devicesState = devicesState,
                    selectedDeviceId = selectedDeviceId,
                    onSelectDevice = { selectedDeviceId = it },
                    pickedFile = pickedFile,
                    onPickFile = { filePicker.launch() },
                    onRemoveFile = { pickedFile = null },
                    onSubmit = {
                        val file = pickedFile
                        val deviceId = selectedDeviceId
                        if (file != null && deviceId != null) {
                            viewModel.submitObservation(deviceId, file.bytes, file.fileName, file.contentType)
                        }
                    },
                    canSubmit = canSubmit,
                    isSubmitting = isSubmitting,
                    stageLabel = stageLabel,
                    errorMessage = (submitState as? SubmitState.Error)?.message,
                    modifier = Modifier.fillMaxWidth(),
                )
                AnalysisResultsPanel(
                    submitState = submitState,
                    stages = stages,
                    stageIndex = stageIndex,
                    baseUrl = baseUrl,
                    device = selectedDevice,
                    onViewAllObservations = onViewAllObservations,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun UploadPanel(
    devicesState: UiState<List<Device>>,
    selectedDeviceId: Int?,
    onSelectDevice: (Int) -> Unit,
    pickedFile: PickedFile?,
    onPickFile: () -> Unit,
    onRemoveFile: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    stageLabel: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        Column(Modifier.padding(20.dp)) {
            Text("1. Select Device", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Choose the device or camera where this observation was captured.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            DeviceDropdown(devicesState, selectedDeviceId, onSelectDevice, enabled = !isSubmitting)

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            Text("2. Upload Media", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Choose an image or video file for analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            if (pickedFile == null) {
                UploadDropZone(onClick = onPickFile, enabled = !isSubmitting)
            } else {
                PickedFileRow(pickedFile, onRemove = onRemoveFile, enabled = !isSubmitting)
            }

            Spacer(Modifier.height(20.dp))

            PrimaryAnalyzeButton(onClick = onSubmit, enabled = canSubmit, loading = isSubmitting)

            if (isSubmitting && stageLabel != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stageLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Your data is secure and encrypted.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDropdown(
    devicesState: UiState<List<Device>>,
    selectedDeviceId: Int?,
    onSelectDevice: (Int) -> Unit,
    enabled: Boolean,
) {
    val devices = (devicesState as? UiState.Success)?.data ?: emptyList()
    val selected = devices.find { it.id == selectedDeviceId }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selected?.let { "${it.name} - ${it.location}" } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = { Text(if (devices.isEmpty()) "No devices available" else "Select a device") },
            leadingIcon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            devices.forEach { device ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("${device.name} - ${device.location}") },
                    onClick = {
                        onSelectDevice(device.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun UploadDropZone(onClick: () -> Unit, enabled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.CloudUpload,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Drag & drop your file here", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text("or", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        PrimaryAnalyzeButton(text = "Choose File", onClick = onClick, enabled = enabled, loading = false, compact = true)
        Spacer(Modifier.height(12.dp))
        Text(
            "Supported formats: JPG, PNG, MP4, MOV (Max 200MB)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PickedFileRow(pickedFile: PickedFile, onRemove: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = PrimaryGreen)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(pickedFile.fileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${pickedFile.contentType} · ${pickedFile.bytes.size.toFileSizeLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRemove, enabled = enabled) {
            Icon(Icons.Filled.Close, contentDescription = "Remove file")
        }
    }
}

@Composable
private fun PrimaryAnalyzeButton(
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
    text: String = "Analyze",
    compact: Boolean = false,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(if (compact) 40.dp else 52.dp),
        shape = MaterialTheme.shapes.small,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
    ) {
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = androidx.compose.ui.graphics.Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}
