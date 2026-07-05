package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.remote.dto.AuthResponseDto
import com.example.faunabahav.data.remote.dto.LoginRequestDto
import com.example.faunabahav.data.remote.dto.RegisterRequestDto
import com.example.faunabahav.data.session.SessionStorage
import com.example.faunabahav.model.Session
import com.example.faunabahav.model.User
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val api: FaunaBehavApiClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    private var cachedSession: Session? = decodeSession(sessionStorage.readRaw())

    override suspend fun login(email: String, password: String, rememberMe: Boolean): ApiResult<User> =
        apiCall { api.login(LoginRequestDto(email, password)) }.toUserResult(rememberMe)

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        rememberMe: Boolean,
    ): ApiResult<User> =
        apiCall { api.register(RegisterRequestDto(email, password, displayName)) }.toUserResult(rememberMe)

    override suspend fun logout() {
        cachedSession = null
        sessionStorage.clear()
    }

    override fun currentSession(): Session? = cachedSession

    private fun ApiResult<AuthResponseDto>.toUserResult(rememberMe: Boolean): ApiResult<User> = when (this) {
        is ApiResult.Success -> {
            val user = User(email = data.user.email, displayName = data.user.username, role = data.user.role)
            val session = Session(token = data.accessToken, user = user)
            cachedSession = session
            if (rememberMe) {
                sessionStorage.writeRaw(Json.encodeToString(Session.serializer(), session))
            } else {
                sessionStorage.clear()
            }
            ApiResult.Success(user)
        }

        is ApiResult.Failure -> ApiResult.Failure(error)
    }
}

private fun decodeSession(raw: String?): Session? = raw?.let {
    try {
        Json.decodeFromString(Session.serializer(), it)
    } catch (e: Exception) {
        null
    }
}
