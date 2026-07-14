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
import com.example.faunabahav.model.User

enum class Destination(val label: String, val icon: ImageVector, val adminOnly: Boolean = false) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Observations("Observations", Icons.Filled.Visibility),
    Alerts("Alerts", Icons.Filled.Warning),
    Analytics("Analytics", Icons.Filled.BarChart),
    Devices("Devices", Icons.Filled.Devices, adminOnly = true),
    Feedback("Feedback", Icons.Filled.Feedback, adminOnly = true),
    Inference("Upload", Icons.Filled.CloudUpload),
    Settings("Settings", Icons.Filled.Settings),
}

/** Wire `role` casing isn't guaranteed consistent (see other free-text backend fields). */
val User?.isAdmin: Boolean get() = this?.role?.equals("admin", ignoreCase = true) == true

fun visibleDestinations(entries: List<Destination>, user: User?): List<Destination> =
    if (user.isAdmin) entries else entries.filterNot { it.adminOnly }
