package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.data.remote.parseBehaviourCategory
import com.example.faunabahav.data.remote.parseRiskLevel
import com.example.faunabahav.data.remote.parseSpecies
import com.example.faunabahav.data.remote.parseTimestamp
import com.example.faunabahav.model.Alert
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
)

fun AlertDto.toDomain(): Alert = Alert(
    id = alertId,
    eventId = eventId,
    species = parseSpecies(animal),
    behaviourCategory = parseBehaviourCategory(behaviour),
    riskLevel = parseRiskLevel(riskLevel),
    confidence = confidence,
    location = location,
    status = status,
    deterrenceAction = deterrenceAction,
    timestamp = parseTimestamp(timestamp),
)