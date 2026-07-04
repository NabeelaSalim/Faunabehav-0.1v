package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.DashboardSummary

interface DashboardRepository {
    suspend fun getDashboardSummary(): ApiResult<DashboardSummary>
}

class DashboardRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : DashboardRepository {
    override suspend fun getDashboardSummary(): ApiResult<DashboardSummary> = apiCall {
        api.getDashboardSummary().toDomain()
    }
}