package com.example.faunabahav.model

import kotlin.time.Instant

sealed interface InferenceResult {
    data class Detected(
        val species: Species,
        val speciesConfidence: Double,
        // Null for image uploads — a still image only gets YOLOv8 species detection,
        // never an R3D-18 behaviour/risk/deterrence/outcome result.
        val behaviourCategory: BehaviourCategory?,
        val behaviourConfidence: Double?,
        val riskLevel: RiskLevel?,
        val alertType: String?,
        val actions: List<String>,
        val message: String,
        val outcome: Outcome? = null,
        val mediaType: MediaType = MediaType.VIDEO,
        val framePath: String?,
        val boundingBox: BoundingBox? = null,
        val frameWidth: Int? = null,
        val frameHeight: Int? = null,
        val inferenceTimeSeconds: Double? = null,
        val timestamp: Instant? = null,
    ) : InferenceResult

    data class NoSpeciesDetected(
        val videoPath: String,
    ) : InferenceResult
}