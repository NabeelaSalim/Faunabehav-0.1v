package com.example.faunabahav.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Observation(
    val id: Int,
    val deviceId: Int,
    val species: Species,
    val behaviourCategory: BehaviourCategory,
    val riskLevel: RiskLevel,
    val deterrenceAction: String,
    val confidence: Double,
    val framePath: String,
    val timestamp: Instant,
)