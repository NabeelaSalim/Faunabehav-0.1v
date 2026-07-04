package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.model.Feedback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackDto(
    @SerialName("event_id") val eventId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("corrected_behaviour") val correctedBehaviour: String,
    @SerialName("feedback_id") val feedbackId: Int? = null,
)

fun FeedbackDto.toDomain(): Feedback = Feedback(
    eventId = eventId,
    userId = userId,
    correctedBehaviour = correctedBehaviour,
    feedbackId = feedbackId,
)

fun Feedback.toDto(): FeedbackDto = FeedbackDto(
    eventId = eventId,
    userId = userId,
    correctedBehaviour = correctedBehaviour,
    feedbackId = feedbackId,
)