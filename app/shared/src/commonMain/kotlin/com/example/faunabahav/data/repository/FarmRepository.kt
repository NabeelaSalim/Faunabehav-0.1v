package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.CreateFarmRequest
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.Farm

interface FarmRepository {
    suspend fun getFarms(): ApiResult<List<Farm>>
    suspend fun createFarm(name: String, location: String): ApiResult<Farm>
    suspend fun deleteFarm(farmId: Int): ApiResult<Unit>
}

class FarmRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : FarmRepository {
    override suspend fun getFarms(): ApiResult<List<Farm>> = apiCall {
        api.getFarms().map { it.toDomain() }
    }

    override suspend fun createFarm(name: String, location: String): ApiResult<Farm> = apiCall {
        api.createFarm(CreateFarmRequest(name, location)).toDomain()
    }

    override suspend fun deleteFarm(farmId: Int): ApiResult<Unit> = apiCall {
        api.deleteFarm(farmId)
    }
}
