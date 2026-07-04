package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val eventId: Int,
    val userId: Int,
    val correctedBehaviour: String,
    val feedbackId: Int? = null,
)