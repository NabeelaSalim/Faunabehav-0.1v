package com.example.faunabahav.ui.util

import com.example.faunabahav.model.Observation
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DailyEventCount(val date: LocalDate, val count: Int)

/**
 * The `/analytics` endpoint has no time-series field, so "Events Over Time" is derived here from
 * real per-observation timestamps instead. Bucketed in UTC to match `parseTimestamp`'s existing
 * UTC assumption for the naive (no-offset) timestamps this backend sends — otherwise the same
 * data would bucket differently on an Android phone vs. a browser in another timezone.
 */
fun List<Observation>.toDailyEventCounts(): List<DailyEventCount> =
    groupingBy { it.timestamp.toLocalDateTime(TimeZone.UTC).date }
        .eachCount()
        .map { (date, count) -> DailyEventCount(date, count) }
        .sortedBy { it.date }
