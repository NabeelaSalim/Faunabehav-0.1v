package com.example.faunabahav.ui.util

import com.example.faunabahav.model.Observation
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class TrendPeriod(val label: String) {
    TODAY("Today"),
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
}

data class TrendPoint(val axisLabel: String, val count: Int)

/**
 * Real per-bucket counts derived from observation timestamps — the backend has no time-series
 * endpoint, so this buckets by hour (Today) or by day (7/30 Days) client-side, same reasoning as
 * toDailyEventCounts(). All buckets are shown, including zero-count ones, so the trend line
 * reflects real gaps rather than only points where something happened.
 */
fun List<Observation>.toTrendPoints(period: TrendPeriod): List<TrendPoint> = when (period) {
    TrendPeriod.TODAY -> hourlyBuckets()
    TrendPeriod.SEVEN_DAYS -> dailyBuckets(7)
    TrendPeriod.THIRTY_DAYS -> dailyBuckets(30)
}

/** Only up to the current real hour — future hours today haven't happened yet, so plotting them
 *  as zero would misrepresent "now" as being at the end of the day. */
private fun List<Observation>.hourlyBuckets(): List<TrendPoint> {
    val nowDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val today = nowDateTime.date
    val todays = filter { it.timestamp.toLocalDateTime(TimeZone.UTC).date == today }
    return (0..nowDateTime.hour).map { hour ->
        val count = todays.count { it.timestamp.toLocalDateTime(TimeZone.UTC).hour == hour }
        val label = when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
        TrendPoint(label, count)
    }
}

private fun List<Observation>.dailyBuckets(days: Int): List<TrendPoint> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    val dates = (0 until days).map { today.minus(it, DateTimeUnit.DAY) }.reversed()
    return dates.map { date ->
        val count = count { it.timestamp.toLocalDateTime(TimeZone.UTC).date == date }
        TrendPoint("${date.monthNumber}/${date.dayOfMonth}", count)
    }
}
