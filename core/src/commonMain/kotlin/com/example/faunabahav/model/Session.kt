package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val token: String,
    val user: User,
)
