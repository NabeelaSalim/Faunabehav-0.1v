package com.example.faunabahav.ui.screens.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.components.StatCard
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.theme.AccentBlue
import com.example.faunabahav.ui.theme.AccentPurple
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * All four numbers are computed client-side from the one real fetched [alerts] list — this app
 * has no backend "alert stats" endpoint, so these mirror how ObservationStatCards already derives
 * real numbers from a real list rather than calling a dedicated summary API.
 */
@Composable
fun AlertStatCards(alerts: List<Alert>) {
    val today = Clock.System.todayIn(TimeZone.UTC)

    val active = alerts.count { !it.status.equals("resolved", ignoreCase = true) }
    val highRisk = alerts.count { it.riskLevel == RiskLevel.HIGH }
    val triggeredToday = alerts.count { it.timestamp.toLocalDateTime(TimeZone.UTC).date == today }
    val resolvedToday = alerts.count {
        it.status.equals("resolved", ignoreCase = true) && it.timestamp.toLocalDateTime(TimeZone.UTC).date == today
    }

    val itemsPerRow = if (rememberIsWideScreen()) 4 else 2
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = itemsPerRow,
    ) {
        val cardWidth = Modifier.width(200.dp)
        StatCard(
            title = "Active Alerts",
            value = active.toString(),
            icon = Icons.Filled.NotificationsActive,
            accentColor = DangerRed,
            subtitle = "Require attention",
            modifier = cardWidth,
        )
        StatCard(
            title = "High Risk",
            value = highRisk.toString(),
            icon = Icons.Filled.Shield,
            accentColor = AccentPurple,
            subtitle = "High priority alerts",
            modifier = cardWidth,
        )
        StatCard(
            title = "Triggered Today",
            value = triggeredToday.toString(),
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            accentColor = AccentBlue,
            subtitle = "Deterrence actions",
            modifier = cardWidth,
        )
        StatCard(
            title = "Resolved Today",
            value = resolvedToday.toString(),
            icon = Icons.Filled.VerifiedUser,
            accentColor = PrimaryGreen,
            subtitle = "Alerts resolved",
            modifier = cardWidth,
        )
    }
}
