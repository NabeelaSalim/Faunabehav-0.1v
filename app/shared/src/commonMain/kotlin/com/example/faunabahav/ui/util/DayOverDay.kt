package com.example.faunabahav.ui.util

import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Real day-over-day comparison derived from actual timestamps — there is no stored historical
 * snapshot on the backend, so "vs yesterday" is computed client-side from the same real
 * observation/alert records already fetched for other panels. `percentChange` is null when there
 * were zero events yesterday, since a percentage change from zero is undefined — callers must
 * hide the trend text in that case rather than show a fabricated number.
 */
fun List<Instant>.dayOverDayPercentChange(): Int? {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val todayCount = count { it.toLocalDateTime(TimeZone.UTC).date == today }
    val yesterdayCount = count { it.toLocalDateTime(TimeZone.UTC).date == yesterday }
    if (yesterdayCount == 0) return null
    return (((todayCount - yesterdayCount) * 100.0) / yesterdayCount).roundToInt()
}

/** Real daily counts over the last [days] days (oldest first), for a stat card sparkline. */
fun List<Instant>.lastNDaysCounts(days: Int): List<Float> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    val dates = (0 until days).map { today.minus(it, DateTimeUnit.DAY) }.reversed()
    return dates.map { date -> count { it.toLocalDateTime(TimeZone.UTC).date == date }.toFloat() }
}
