package com.example.faunabahav.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Observations("Observations", Icons.Filled.Visibility),
    Alerts("Alerts", Icons.Filled.Warning),
    Analytics("Analytics", Icons.Filled.BarChart),
    Devices("Devices", Icons.Filled.Devices),
    Feedback("Feedback", Icons.Filled.Feedback),
    Inference("Upload", Icons.Filled.CloudUpload),
    Settings("Settings", Icons.Filled.Settings),
}
