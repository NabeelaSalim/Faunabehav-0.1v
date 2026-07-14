package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.data.remote.parseTimestamp
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.BoundingBox
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertDto(
    @SerialName("alert_id") val alertId: Int,
    val animal: String,
    val behaviour: String,
    @SerialName("risk_level") val riskLevel: String,
    val confidence: Double,
    val location: String,
    val status: String,
    @SerialName("deterrence_action") val deterrenceAction: String,
    val timestamp: String,
    @SerialName("event_id") val eventId: Int? = null,
    @SerialName("device_id") val deviceId: Int? = null,
    @SerialName("species_confidence") val speciesConfidence: Double? = null,
    val camera: String? = null,
    @SerialName("frame_path") val framePath: String? = null,
    @SerialName("bounding_box") val boundingBox: BoundingBox? = null,
    @SerialName("frame_width") val frameWidth: Int? = null,
    @SerialName("frame_height") val frameHeight: Int? = null,
    @SerialName("acknowledged_by") val acknowledgedBy: Int? = null,
)

fun AlertDto.toDomain(): Alert = Alert(
    id = alertId,
    eventId = eventId,
    deviceId = deviceId,
    species = parseSpecies(animal),
    behaviourCategory = parseBehaviourCategory(behaviour),
    riskLevel = parseRiskLevel(riskLevel),
    confidence = confidence,
    speciesConfidence = speciesConfidence,
    camera = camera,
    location = location,
    status = status,
    deterrenceAction = deterrenceAction,
    framePath = framePath,
    boundingBox = boundingBox,
    frameWidth = frameWidth,
    frameHeight = frameHeight,
    acknowledgedBy = acknowledgedBy,
    timestamp = parseTimestamp(timestamp),
)

@Serializable
data class AcknowledgeAlertRequestDto(
    @SerialName("user_id") val userId: Int,
)