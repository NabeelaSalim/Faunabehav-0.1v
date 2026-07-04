package com.example.faunabahav.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Alert(
    val id: Int,
    val species: Species,
    val behaviourCategory: BehaviourCategory,
    val riskLevel: RiskLevel,
    val confidence: Double,
    val location: String,
    val status: String,
    val deterrenceAction: String,
    val timestamp: Instant,
    val eventId: Int? = null,
)