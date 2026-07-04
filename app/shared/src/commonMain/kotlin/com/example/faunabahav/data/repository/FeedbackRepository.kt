package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.toDomain
import com.example.faunabahav.data.remote.dto.toDto
import com.example.faunabahav.model.Feedback

interface FeedbackRepository {
    suspend fun getFeedback(): ApiResult<List<Feedback>>

    suspend fun submitFeedback(feedback: Feedback): ApiResult<Feedback>
}

class FeedbackRepositoryImpl(
    private val api: FaunaBehavApiClient,
) : FeedbackRepository {
    override suspend fun getFeedback(): ApiResult<List<Feedback>> = apiCall {
        api.getFeedback().map { it.toDomain() }
    }

    override suspend fun submitFeedback(feedback: Feedback): ApiResult<Feedback> = apiCall {
        api.submitFeedback(feedback.toDto()).toDomain()
    }
}