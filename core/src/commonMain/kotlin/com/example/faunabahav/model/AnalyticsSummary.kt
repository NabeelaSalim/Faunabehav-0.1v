package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsSummary(
    val totalEvents: Int,
    val lowRisk: Int,
    val mediumRisk: Int,
    val highRisk: Int,
    val animalBreakdown: Map<String, Int>,
    val behaviourBreakdown: Map<String, Int>,
)