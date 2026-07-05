package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseMediaType
import com.example.faunabahav.data.remote.parseOutcome
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.data.remote.parseTimestamp
import com.example.faunabahav.model.BoundingBox
import com.example.faunabahav.model.InferenceResult
import com.example.faunabahav.model.MediaType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InferenceResultDto(
    val decision: String? = null,
    @SerialName("video_path") val videoPath: String? = null,
    @SerialName("detected_species") val detectedSpecies: String? = null,
    @SerialName("species_confidence") val speciesConfidence: Double? = null,
    // All null for image uploads — see InferenceResult.Detected.behaviourCategory.
    @SerialName("predicted_behaviour") val predictedBehaviour: String? = null,
    @SerialName("behaviour_confidence") val behaviourConfidence: Double? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("alert_type") val alertType: String? = null,
    val actions: List<String>? = null,
    val message: String? = null,
    val outcome: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("frame_path") val framePath: String? = null,
    @SerialName("bounding_box") val boundingBox: BoundingBox? = null,
    @SerialName("frame_width") val frameWidth: Int? = null,
    @SerialName("frame_height") val frameHeight: Int? = null,
    @SerialName("inference_time_seconds") val inferenceTimeSeconds: Double? = null,
    val timestamp: String? = null,
)

fun InferenceResultDto.toDomain(): InferenceResult {
    if (decision == "no_target_species_detected" || detectedSpecies == null) {
        return InferenceResult.NoSpeciesDetected(videoPath = videoPath ?: "")
    }
    return InferenceResult.Detected(
        species = parseSpecies(detectedSpecies),
        speciesConfidence = speciesConfidence ?: 0.0,
        behaviourCategory = predictedBehaviour?.let { parseBehaviourCategory(it) },
        behaviourConfidence = behaviourConfidence,
        riskLevel = riskLevel?.let { parseRiskLevel(it) },
        alertType = alertType,
        actions = actions ?: emptyList(),
        message = message ?: "",
        outcome = outcome?.let { parseOutcome(it) },
        mediaType = mediaType?.let { parseMediaType(it) } ?: MediaType.VIDEO,
        framePath = framePath,
        boundingBox = boundingBox,
        frameWidth = frameWidth,
        frameHeight = frameHeight,
        inferenceTimeSeconds = inferenceTimeSeconds,
        timestamp = timestamp?.let { parseTimestamp(it) },
    )
}
