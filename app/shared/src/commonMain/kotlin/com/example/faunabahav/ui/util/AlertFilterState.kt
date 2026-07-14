package com.example.faunabahav.ui.util

import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.model.Species
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Mirrors the mockup's filter pill row exactly. "Active" means still open (not yet resolved) —
 *  it deliberately includes both the "Active" and "Monitoring" real status values, since both
 *  represent an alert that hasn't been closed out yet. */
enum class AlertStatusTab(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    RESOLVED("Resolved"),
}

enum class AlertSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    HIGHEST_CONFIDENCE("Highest Confidence"),
    HIGHEST_RISK("Highest Risk"),
}

data class AlertFilterState(
    val statusTab: AlertStatusTab = AlertStatusTab.ALL,
    val species: Species? = null,
    val behaviour: BehaviourCategory? = null,
    val deviceId: Int? = null,
    val dateRange: ClosedRange<LocalDate>? = null,
    val searchQuery: String = "",
    val sort: AlertSort = AlertSort.NEWEST,
) {
    val isDefault: Boolean
        get() = statusTab == AlertStatusTab.ALL && species == null && behaviour == null &&
            deviceId == null && dateRange == null && searchQuery.isBlank() && sort == AlertSort.NEWEST
}

/** A real, derived display id (e.g. "ALT-2026-07-05-0001") — not a stored backend field, just a
 *  formatted view of the real alert id + its real trigger date, matching the reference design. */
fun Alert.displayId(): String {
    val date = timestamp.toLocalDateTime(TimeZone.UTC).date
    return "ALT-$date-${id.toString().padStart(4, '0')}"
}

/** Every predicate operates on real fields already present on Alert — nothing here invents a
 *  value, it only narrows down the real fetched list (this app has no server-side query params
 *  for any list endpoint, so filtering/search/sort is done client-side over one real fetch,
 *  same convention ObservationFilters.kt already uses). */
fun List<Alert>.applyFilters(filters: AlertFilterState): List<Alert> {
    var result: List<Alert> = this

    result = when (filters.statusTab) {
        AlertStatusTab.ALL -> result
        AlertStatusTab.ACTIVE -> result.filter { !it.status.equals("resolved", ignoreCase = true) }
        AlertStatusTab.HIGH -> result.filter { it.riskLevel == RiskLevel.HIGH }
        AlertStatusTab.MEDIUM -> result.filter { it.riskLevel == RiskLevel.MEDIUM }
        AlertStatusTab.LOW -> result.filter { it.riskLevel == RiskLevel.LOW }
        AlertStatusTab.RESOLVED -> result.filter { it.status.equals("resolved", ignoreCase = true) }
    }

    filters.species?.let { species -> result = result.filter { it.species == species } }
    filters.behaviour?.let { behaviour -> result = result.filter { it.behaviourCategory == behaviour } }
    filters.deviceId?.let { deviceId -> result = result.filter { it.deviceId == deviceId } }

    filters.dateRange?.let { range ->
        result = result.filter {
            val date = it.timestamp.toLocalDateTime(TimeZone.UTC).date
            date >= range.start && date <= range.endInclusive
        }
    }

    val query = filters.searchQuery.trim().lowercase()
    if (query.isNotEmpty()) {
        result = result.filter { alert ->
            alert.species.name.lowercase().contains(query) ||
                alert.behaviourCategory.name.lowercase().contains(query) ||
                (alert.camera?.lowercase()?.contains(query) == true) ||
                alert.location.lowercase().contains(query) ||
                alert.displayId().lowercase().contains(query)
        }
    }

    result = when (filters.sort) {
        AlertSort.NEWEST -> result.sortedByDescending { it.timestamp }
        AlertSort.OLDEST -> result.sortedBy { it.timestamp }
        AlertSort.HIGHEST_CONFIDENCE -> result.sortedByDescending { it.confidence }
        AlertSort.HIGHEST_RISK -> result.sortedByDescending {
            when (it.riskLevel) {
                RiskLevel.HIGH -> 2
                RiskLevel.MEDIUM -> 1
                RiskLevel.LOW -> 0
            }
        }
    }

    return result
}
