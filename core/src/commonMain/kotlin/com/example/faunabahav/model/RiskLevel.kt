package com.example.faunabahav.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RiskLevel {
    @SerialName("Low") LOW,
    @SerialName("Medium") MEDIUM,
    @SerialName("High") HIGH,
}