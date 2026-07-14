package com.example.faunabahav.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.faunabahav.model.User

private val MoreDestinations = listOf(
    Destination.Analytics,
    Destination.Devices,
    Destination.Feedback,
    Destination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreDestinationsSheet(
    user: User?,
    onSelect: (Destination) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        visibleDestinations(MoreDestinations, user).forEach { dest ->
            ListItem(
                headlineContent = { Text(dest.label) },
                leadingContent = { Icon(dest.icon, contentDescription = dest.label) },
                modifier = Modifier.fillMaxWidth().clickable { onSelect(dest) },
            )
        }
    }
}
