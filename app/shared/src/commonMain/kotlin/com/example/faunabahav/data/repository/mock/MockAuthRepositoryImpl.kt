package com.example.faunabahav.data.repository.mock

import com.example.faunabahav.data.remote.ApiError
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.AuthRepository
import com.example.faunabahav.data.session.SessionStorage
import com.example.faunabahav.model.Session
import com.example.faunabahav.model.User
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.random.Random

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/**
 * The real FastAPI backend has no authentication endpoint yet. This mock accepts any
 * syntactically valid email + password (never a hardcoded credential pair) so the
 * Login/Sign Up -> Dashboard flow is fully usable today. Swap for a real ApiResult<User>-returning
 * implementation backed by `apiCall {}` + FaunaBehavApiClient once backend auth exists; nothing
 * above the AuthRepository interface needs to change.
 *
 * Passing the password "networkerror" deliberately returns a Network failure so the error UI
 * path is exercisable without a real backend failure to trigger it against.
 */
class MockAuthRepositoryImpl(
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    private var cachedSession: Session? = decodeSession(sessionStorage.readRaw())

    override suspend fun login(email: String, password: String, rememberMe: Boolean): ApiResult<User> {
        if (!EMAIL_REGEX.matches(email)) {
            return ApiResult.Failure(ApiError.Unknown("Enter a valid email address"))
        }
        if (password.isBlank()) {
            return ApiResult.Failure(ApiError.Unknown("Password is required"))
        }

        delay(600)

        if (password == "networkerror") {
            return ApiResult.Failure(ApiError.Network("Unable to reach the server"))
        }

        val user = User(email = email, displayName = email.substringBefore("@"))
        return ApiResult.Success(persistSession(user, rememberMe))
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        rememberMe: Boolean,
    ): ApiResult<User> {
        if (!EMAIL_REGEX.matches(email)) {
            return ApiResult.Failure(ApiError.Unknown("Enter a valid email address"))
        }
        if (displayName.isBlank()) {
            return ApiResult.Failure(ApiError.Unknown("Full name is required"))
        }
        if (password.length < 6) {
            return ApiResult.Failure(ApiError.Unknown("Password must be at least 6 characters"))
        }

        delay(600)

        if (password == "networkerror") {
            return ApiResult.Failure(ApiError.Network("Unable to reach the server"))
        }

        val user = User(email = email, displayName = displayName)
        return ApiResult.Success(persistSession(user, rememberMe))
    }

    override suspend fun logout() {
        cachedSession = null
        sessionStorage.clear()
    }

    override fun currentSession(): Session? = cachedSession

    private fun persistSession(user: User, rememberMe: Boolean): User {
        val session = Session(token = Random.nextLong().toString(radix = 16), user = user)
        cachedSession = session
        if (rememberMe) {
            sessionStorage.writeRaw(Json.encodeToString(Session.serializer(), session))
        } else {
            sessionStorage.clear()
        }
        return user
    }
}

private fun decodeSession(raw: String?): Session? = raw?.let {
    try {
        Json.decodeFromString(Session.serializer(), it)
    } catch (e: Exception) {
        null
    }
}
