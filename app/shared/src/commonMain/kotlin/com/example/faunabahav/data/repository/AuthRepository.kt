package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.model.Session
import com.example.faunabahav.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): ApiResult<User>

    suspend fun signUp(email: String, password: String, displayName: String, rememberMe: Boolean): ApiResult<User>

    suspend fun logout()

    fun currentSession(): Session?
}
