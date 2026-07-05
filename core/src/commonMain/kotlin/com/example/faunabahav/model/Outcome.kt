package com.example.faunabahav.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Outcome {
    @SerialName("deterrence_activated") DETERRENCE_ACTIVATED,
    @SerialName("farmer_intervention_required") FARMER_INTERVENTION_REQUIRED,
    @SerialName("monitoring_continues") MONITORING_CONTINUES,
}
