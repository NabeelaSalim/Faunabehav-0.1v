package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int,
    val email: String,
    val displayName: String,
    val role: String,
)
