package com.example.faunabahav.data.remote.dto

import com.example.faunabahav.model.Device
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    @SerialName("device_id") val deviceId: Int,
    @SerialName("device_name") val deviceName: String,
    val location: String,
    val status: String,
)

fun DeviceDto.toDomain(): Device = Device(
    id = deviceId,
    name = deviceName,
    location = location,
    status = status,
)