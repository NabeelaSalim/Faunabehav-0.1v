package com.example.faunabahav.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.model.User

private val MoreOverflowDestinations = setOf(
    Destination.Analytics,
    Destination.Devices,
    Destination.Feedback,
    Destination.Settings,
)

/**
 * Wide screens (Web/desktop/tablet) get a fixed left sidebar listing every destination.
 * Narrow screens (Android phones) get a 5-item bottom nav; the destinations that don't fit
 * (Analytics, Devices, Feedback, Settings) live behind the 5th "More" item's bottom sheet.
 */
@Composable
fun ResponsiveScaffold(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    user: User?,
    deviceRepository: DeviceRepository,
    onLogout: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (rememberIsWideScreen()) {
        Row(Modifier.fillMaxSize()) {
            AppSidebar(
                selected = selected,
                onSelect = onSelect,
                user = user,
                deviceRepository = deviceRepository,
                onLogout = onLogout,
            )
            Box(Modifier.weight(1f)) { content() }
        }
    } else {
        var showMoreSheet by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                AppBottomNav(
                    selected = selected,
                    isMoreSelected = selected in MoreOverflowDestinations,
                    onSelect = onSelect,
                    onMoreClick = { showMoreSheet = true },
                )
            },
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues).fillMaxSize()) { content() }
        }

        if (showMoreSheet) {
            MoreDestinationsSheet(
                user = user,
                onSelect = { dest ->
                    onSelect(dest)
                    showMoreSheet = false
                },
                onDismiss = { showMoreSheet = false },
            )
        }
    }
}
