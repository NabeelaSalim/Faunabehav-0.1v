package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummary(
    val totalEvents: Int,
    val highRiskEvents: Int,
    val activeDevices: Int,
    val deterrenceActions: Int,
)