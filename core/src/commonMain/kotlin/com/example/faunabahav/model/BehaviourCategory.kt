package com.example.faunabahav.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BehaviourCategory {
    @SerialName("feeding_foraging") FEEDING_FORAGING,
    @SerialName("locomotion") LOCOMOTION,
    @SerialName("vigilance_alert") VIGILANCE_ALERT,
    @SerialName("aggressive_destructive") AGGRESSIVE_DESTRUCTIVE,
    @SerialName("resting_passive") RESTING_PASSIVE,
    @SerialName("social_interaction") SOCIAL_INTERACTION,
    @SerialName("unknown_unclear") UNKNOWN_UNCLEAR,
}