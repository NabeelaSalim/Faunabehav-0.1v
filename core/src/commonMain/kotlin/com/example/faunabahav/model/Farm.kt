package com.example.faunabahav.model

import kotlinx.serialization.Serializable

@Serializable
data class Farm(
    val id: Int,
    val name: String,
    val location: String,
    val ownerId: Int,
    val deviceCount: Int = 0,
)
