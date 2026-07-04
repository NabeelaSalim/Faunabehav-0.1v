package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.data.remote.parseTimestamp
import com.example.faunabahav.model.Observation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObservationDto(
    @SerialName("event_id") val eventId: Int,
    @SerialName("device_id") val deviceId: Int,
    val animal: String,
    val behaviour: String,
    val confidence: Double,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("deterrence_action") val deterrenceAction: String,
    @SerialName("frame_path") val framePath: String,
    val timestamp: String,
)

fun ObservationDto.toDomain(): Observation = Observation(
    id = eventId,
    deviceId = deviceId,
    species = parseSpecies(animal),
    behaviourCategory = parseBehaviourCategory(behaviour),
    riskLevel = parseRiskLevel(riskLevel),
    deterrenceAction = deterrenceAction,
    confidence = confidence,
    framePath = framePath,
    timestamp = parseTimestamp(timestamp),
)