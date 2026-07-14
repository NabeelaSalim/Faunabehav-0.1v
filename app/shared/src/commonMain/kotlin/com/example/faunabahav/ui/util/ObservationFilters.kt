package com.example.faunabahav.ui.util

import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.Observation
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.model.Species
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ObservationSort(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    HIGHEST_CONFIDENCE("Highest Confidence"),
    HIGHEST_RISK("Highest Risk"),
}

data class ObservationFilterState(
    val riskLevel: RiskLevel? = null,
    val species: Species? = null,
    val behaviour: BehaviourCategory? = null,
    val deviceId: Int? = null,
    val dateRange: ClosedRange<LocalDate>? = null,
    val searchQuery: String = "",
    val minConfidence: Float = 0f,
    val sort: ObservationSort = ObservationSort.NEWEST_FIRST,
) {
    val isDefault: Boolean
        get() = riskLevel == null && species == null && behaviour == null && deviceId == null &&
            dateRange == null && searchQuery.isBlank() && minConfidence == 0f && sort == ObservationSort.NEWEST_FIRST
}

/** Every predicate operates on real fields already present on Observation — nothing here
 *  invents a value, it only narrows down the real fetched list. */
fun List<Observation>.applyFilters(
    filters: ObservationFilterState,
    deviceNames: Map<Int, String>,
    deviceZones: Map<Int, String>,
): List<Observation> {
    var result: List<Observation> = this

    filters.riskLevel?.let { risk -> result = result.filter { it.riskLevel == risk } }
    filters.species?.let { species -> result = result.filter { it.species == species } }
    filters.behaviour?.let { behaviour -> result = result.filter { it.behaviourCategory == behaviour } }
    filters.deviceId?.let { deviceId -> result = result.filter { it.deviceId == deviceId } }

    filters.dateRange?.let { range ->
        result = result.filter {
            val date = it.timestamp.toLocalDateTime(TimeZone.UTC).date
            date >= range.start && date <= range.endInclusive
        }
    }

    if (filters.minConfidence > 0f) {
        result = result.filter { (it.speciesConfidence ?: 0.0) >= filters.minConfidence }
    }

    val query = filters.searchQuery.trim().lowercase()
    if (query.isNotEmpty()) {
        result = result.filter { observation ->
            val cameraName = deviceNames[observation.deviceId]?.lowercase() ?: ""
            val zone = deviceZones[observation.deviceId]?.lowercase() ?: ""
            observation.species.name.lowercase().contains(query) ||
                (observation.behaviourCategory?.name?.lowercase()?.contains(query) == true) ||
                (observation.deterrenceAction?.lowercase()?.contains(query) == true) ||
                (observation.riskLevel?.name?.lowercase()?.contains(query) == true) ||
                cameraName.contains(query) ||
                zone.contains(query)
        }
    }

    result = when (filters.sort) {
        ObservationSort.NEWEST_FIRST -> result.sortedByDescending { it.timestamp }
        ObservationSort.OLDEST_FIRST -> result.sortedBy { it.timestamp }
        ObservationSort.HIGHEST_CONFIDENCE -> result.sortedByDescending { it.speciesConfidence ?: 0.0 }
        ObservationSort.HIGHEST_RISK -> result.sortedByDescending {
            when (it.riskLevel) {
                RiskLevel.HIGH -> 2
                RiskLevel.MEDIUM -> 1
                RiskLevel.LOW -> 0
                null -> -1
            }
        }
    }

    return result
}

/** The real most-frequent species in the fetched dataset, or null when there's no data yet. */
fun List<Observation>.mostCommonSpecies(): Pair<Species, Int>? =
    groupingBy { it.species }.eachCount().maxByOrNull { it.value }?.toPair()
