package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseOutcome
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.data.remote.parseTimestamp
import com.example.faunabahav.model.BoundingBox
import com.example.faunabahav.model.Observation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObservationDto(
    @SerialName("event_id") val eventId: Int,
    @SerialName("device_id") val deviceId: Int,
    val animal: String,
    // Null for image uploads — see Observation.behaviourCategory.
    val behaviour: String? = null,
    val confidence: Double? = null,
    @SerialName("species_confidence") val speciesConfidence: Double? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("deterrence_action") val deterrenceAction: String? = null,
    val outcome: String? = null,
    @SerialName("frame_path") val framePath: String,
    @SerialName("bounding_box") val boundingBox: BoundingBox? = null,
    @SerialName("frame_width") val frameWidth: Int? = null,
    @SerialName("frame_height") val frameHeight: Int? = null,
    val timestamp: String,
)

fun ObservationDto.toDomain(): Observation = Observation(
    id = eventId,
    deviceId = deviceId,
    species = parseSpecies(animal),
    behaviourCategory = behaviour?.let { parseBehaviourCategory(it) },
    riskLevel = riskLevel?.let { parseRiskLevel(it) },
    deterrenceAction = deterrenceAction,
    confidence = confidence,
    speciesConfidence = speciesConfidence,
    framePath = framePath,
    boundingBox = boundingBox,
    frameWidth = frameWidth,
    frameHeight = frameHeight,
    outcome = outcome?.let { parseOutcome(it) },
    timestamp = parseTimestamp(timestamp),
)
