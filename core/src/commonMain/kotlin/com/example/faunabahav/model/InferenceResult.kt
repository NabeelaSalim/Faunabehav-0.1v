package com.example.faunabahav.model

sealed interface InferenceResult {
    data class Detected(
        val species: Species,
        val speciesConfidence: Double,
        val behaviourCategory: BehaviourCategory,
        val behaviourConfidence: Double,
        val riskLevel: RiskLevel,
        val alertType: String,
        val actions: List<String>,
        val message: String,
        val framePath: String?,
    ) : InferenceResult

    data class NoSpeciesDetected(
        val videoPath: String,
    ) : InferenceResult
}