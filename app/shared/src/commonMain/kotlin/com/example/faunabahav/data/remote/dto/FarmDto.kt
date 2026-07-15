package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.model.Farm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FarmDto(
    @SerialName("farm_id") val farmId: Int,
    @SerialName("farm_name") val farmName: String,
    val location: String,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("device_count") val deviceCount: Int = 0,
)

fun FarmDto.toDomain(): Farm = Farm(
    id = farmId,
    name = farmName,
    location = location,
    ownerId = ownerId,
    deviceCount = deviceCount,
)
