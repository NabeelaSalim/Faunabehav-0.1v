package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val displayName: String,
)
