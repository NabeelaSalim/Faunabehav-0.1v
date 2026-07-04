package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.Alert

interface AlertRepository {
    suspend fun getAlerts(): ApiResult<List<Alert>>
}

class AlertRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : AlertRepository {
    override suspend fun getAlerts(): ApiResult<List<Alert>> = apiCall {
        api.getAlerts().map { it.toDomain() }
    }
}