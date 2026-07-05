package com.example.faunabahav.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaType {
    @SerialName("image") IMAGE,
    @SerialName("video") VIDEO,
}
