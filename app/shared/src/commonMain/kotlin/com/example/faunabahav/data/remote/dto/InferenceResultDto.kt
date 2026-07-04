package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.model.InferenceResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InferenceResultDto(
    val decision: String? = null,
    @SerialName("video_path") val videoPath: String? = null,
    @SerialName("detected_species") val detectedSpecies: String? = null,
    @SerialName("species_confidence") val speciesConfidence: Double? = null,
    @SerialName("predicted_behaviour") val predictedBehaviour: String? = null,
    @SerialName("behaviour_confidence") val behaviourConfidence: Double? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("alert_type") val alertType: String? = null,
    val actions: List<String>? = null,
    val message: String? = null,
    @SerialName("frame_path") val framePath: String? = null,
)

fun InferenceResultDto.toDomain(): InferenceResult {
    if (decision == "no_target_species_detected" || detectedSpecies == null) {
        return InferenceResult.NoSpeciesDetected(videoPath = videoPath ?: "")
    }
    return InferenceResult.Detected(
        species = parseSpecies(detectedSpecies),
        speciesConfidence = speciesConfidence ?: 0.0,
        behaviourCategory = parseBehaviourCategory(predictedBehaviour ?: ""),
        behaviourConfidence = behaviourConfidence ?: 0.0,
        riskLevel = parseRiskLevel(riskLevel ?: ""),
        alertType = alertType ?: "",
        actions = actions ?: emptyList(),
        message = message ?: "",
        framePath = framePath,
    )
}