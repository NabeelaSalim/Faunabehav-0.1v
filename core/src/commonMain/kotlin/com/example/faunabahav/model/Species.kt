package com.example.faunabahav.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Species {
    @SerialName("monkey") MONKEY,
    @SerialName("wild_boar") WILD_BOAR,
    @SerialName("bird") BIRD,
    @SerialName("unknown") UNKNOWN,
}