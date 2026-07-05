package com.example.faunabahav.ui.util

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatTimestamp(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.UTC)
    val minute = dt.minute.toString().padStart(2, '0')
    return "${dt.date} ${dt.hour}:$minute UTC"
}

/** Short 12-hour clock time only (e.g. "7:46 AM"), for recency-focused lists like Recent Alerts
 *  and Detection History where the full date would be redundant/noisy. */
fun formatTimeOnly(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.UTC)
    val minute = dt.minute.toString().padStart(2, '0')
    val hour12 = when {
        dt.hour == 0 -> 12
        dt.hour > 12 -> dt.hour - 12
        else -> dt.hour
    }
    val amPm = if (dt.hour < 12) "AM" else "PM"
    return "$hour12:$minute $amPm"
}
