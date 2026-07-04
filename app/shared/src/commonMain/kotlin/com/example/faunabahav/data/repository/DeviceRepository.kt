package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.Device

interface DeviceRepository {
    suspend fun getDevices(): ApiResult<List<Device>>
}

class DeviceRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : DeviceRepository {
    override suspend fun getDevices(): ApiResult<List<Device>> = apiCall {
        api.getDevices().map { it.toDomain() }
    }
}