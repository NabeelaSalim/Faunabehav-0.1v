package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.model.AnalyticsSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimalBreakdownEntryDto(
    val animal: String,
    val count: Int,
)

@Serializable
data class BehaviourBreakdownEntryDto(
    val behaviour: String,
    val count: Int,
)

@Serializable
data class AnalyticsSummaryDto(
    @SerialName("total_events") val totalEvents: Int,
    @SerialName("low_risk") val lowRisk: Int,
    @SerialName("medium_risk") val mediumRisk: Int,
    @SerialName("high_risk") val highRisk: Int,
    @SerialName("animal_breakdown") val animalBreakdown: List<AnimalBreakdownEntryDto>,
    @SerialName("behaviour_breakdown") val behaviourBreakdown: List<BehaviourBreakdownEntryDto>,
)

fun AnalyticsSummaryDto.toDomain(): AnalyticsSummary = AnalyticsSummary(
    totalEvents = totalEvents,
    lowRisk = lowRisk,
    mediumRisk = mediumRisk,
    highRisk = highRisk,
    animalBreakdown = animalBreakdown.associate { it.animal to it.count },
    behaviourBreakdown = behaviourBreakdown.associate { it.behaviour to it.count },
)