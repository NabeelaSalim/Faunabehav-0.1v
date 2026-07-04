package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.model.InferenceResult
import com.example.faunabahav.model.Observation

interface ObservationRepository {
    suspend fun getObservations(): ApiResult<List<Observation>>

    suspend fun submitObservation(
        deviceId: Int,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): ApiResult<InferenceResult>
}

class ObservationRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : ObservationRepository {
    override suspend fun getObservations(): ApiResult<List<Observation>> = apiCall {
        api.getObservations().map { it.toDomain() }
    }

    override suspend fun submitObservation(
        deviceId: Int,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): ApiResult<InferenceResult> = apiCall {
        api.submitInference(deviceId, fileBytes, fileName, contentType).toDomain()
    }
}