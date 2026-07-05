package com.example.faunabahav.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Observation(
    val id: Int,
    val deviceId: Int,
    val species: Species,
    // Null for image uploads — a still image only gets YOLOv8 species detection,
    // never an R3D-18 behaviour/risk/deterrence result (see MediaType.IMAGE).
    val behaviourCategory: BehaviourCategory?,
    val riskLevel: RiskLevel?,
    val deterrenceAction: String?,
    val confidence: Double?,
    val speciesConfidence: Double? = null,
    val framePath: String,
    val boundingBox: BoundingBox? = null,
    val frameWidth: Int? = null,
    val frameHeight: Int? = null,
    val outcome: Outcome? = null,
    val timestamp: Instant,
)