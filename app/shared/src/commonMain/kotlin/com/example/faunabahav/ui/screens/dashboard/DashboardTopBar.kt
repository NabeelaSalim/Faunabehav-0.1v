package com.example.faunabahav.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.PrimaryGreen
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DashboardTopBar(isRefreshing: Boolean, modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Clock.System.now()
        }
    }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        InfoChip {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            val dt = now.toLocalDateTime(TimeZone.UTC)
            Column {
                Text(formatFullDate(dt), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${dayOfWeekName(dt.dayOfWeek)}, ${formatClock(dt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Weather has no real backend/API source yet — kept as an explicit placeholder,
        // consistent with this project's established policy for features without backend support.
        InfoChip {
            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("28°C", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Sunny (preview)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        InfoChip {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                tint = if (isRefreshing) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Auto-refresh", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(PrimaryGreen, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("Live", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}

private fun formatFullDate(dt: LocalDateTime): String {
    val month = monthName(dt.month)
    return "${dt.dayOfMonth} $month ${dt.year}"
}

private fun formatClock(dt: LocalDateTime): String {
    val minute = dt.minute.toString().padStart(2, '0')
    val hour12 = when {
        dt.hour == 0 -> 12
        dt.hour > 12 -> dt.hour - 12
        else -> dt.hour
    }
    val amPm = if (dt.hour < 12) "AM" else "PM"
    return "$hour12:$minute $amPm"
}

private fun monthName(month: Month): String = month.name.lowercase().replaceFirstChar { it.uppercase() }
private fun dayOfWeekName(day: DayOfWeek): String = day.name.lowercase().replaceFirstChar { it.uppercase() }
