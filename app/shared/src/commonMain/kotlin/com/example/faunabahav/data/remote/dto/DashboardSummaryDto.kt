package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.model.DashboardSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryDto(
    @SerialName("total_events") val totalEvents: Int,
    @SerialName("high_risk_events") val highRiskEvents: Int,
    @SerialName("active_devices") val activeDevices: Int,
    @SerialName("deterrence_actions") val deterrenceActions: Int,
)

fun DashboardSummaryDto.toDomain(): DashboardSummary = DashboardSummary(
    totalEvents = totalEvents,
    highRiskEvents = highRiskEvents,
    activeDevices = activeDevices,
    deterrenceActions = deterrenceActions,
)