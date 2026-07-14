package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.Alert

interface AlertRepository {
    suspend fun getAlerts(): ApiResult<List<Alert>>
    suspend fun resolveAlert(alertId: Int): ApiResult<Alert>
    suspend fun acknowledgeAlert(alertId: Int, userId: Int): ApiResult<Alert>
}

class AlertRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : AlertRepository {
    override suspend fun getAlerts(): ApiResult<List<Alert>> = apiCall {
        api.getAlerts().map { it.toDomain() }
    }

    override suspend fun resolveAlert(alertId: Int): ApiResult<Alert> = apiCall {
        api.resolveAlert(alertId).toDomain()
    }

    override suspend fun acknowledgeAlert(alertId: Int, userId: Int): ApiResult<Alert> = apiCall {
        api.acknowledgeAlert(alertId, userId).toDomain()
    }
}