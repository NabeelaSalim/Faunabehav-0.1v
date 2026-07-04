package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.AnalyticsSummary

interface AnalyticsRepository {
    suspend fun getAnalytics(): ApiResult<AnalyticsSummary>
}

class AnalyticsRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : AnalyticsRepository {
    override suspend fun getAnalytics(): ApiResult<AnalyticsSummary> = apiCall {
        api.getAnalytics().toDomain()
    }
}