package com.example.faunabahav.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

private val PrimaryBottomNavItems = listOf(
    Destination.Dashboard,
    Destination.Observations,
    Destination.Alerts,
    Destination.Inference,
)

@Composable
fun AppBottomNav(
    selected: Destination,
    isMoreSelected: Boolean,
    onSelect: (Destination) -> Unit,
    onMoreClick: () -> Unit,
) {
    NavigationBar {
        PrimaryBottomNavItems.forEach { dest ->
            NavigationBarItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
                alwaysShowLabel = true,
            )
        }
        NavigationBarItem(
            selected = isMoreSelected,
            onClick = onMoreClick,
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "More") },
            label = { Text("More") },
            alwaysShowLabel = true,
        )
    }
}
