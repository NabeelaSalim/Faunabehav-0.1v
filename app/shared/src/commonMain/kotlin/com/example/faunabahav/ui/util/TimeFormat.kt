package com.example.faunabahav.ui.util

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatTimestamp(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.UTC)
    val minute = dt.minute.toString().padStart(2, '0')
    return "${dt.date} ${dt.hour}:$minute UTC"
}
