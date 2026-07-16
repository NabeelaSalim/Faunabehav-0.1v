package com.example.faunabahav.data.repository

import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.remote.apiCall
import com.example.faunabahav.data.session.SessionStorage
import com.example.faunabahav.model.Session
import com.example.faunabahav.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val PATH_LOGIN = "auth/login"
private const val PATH_SIGNUP = "auth/signup"

@Serializable
private data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class SignUpRequest(
    val email: String,
    val password: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
private data class AuthResponse(
    val token: String,
    val user: AuthUserResponse,
)

@Serializable
private data class AuthUserResponse(
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("role") val role: String,
)

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    private var cachedSession: Session? = decodeSession(sessionStorage.readRaw())

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean,
    ): ApiResult<User> = apiCall {
        val response: AuthResponse = httpClient.post(PATH_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

        val user = User(
            email = response.user.email,
            displayName = response.user.displayName,
            userId = response.user.userId,
            role = response.user.role,
        )
        persistSession(user, response.token, rememberMe)
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        rememberMe: Boolean,
    ): ApiResult<User> = apiCall {
        val response: AuthResponse = httpClient.post(PATH_SIGNUP) {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(email, password, displayName))
        }.body()

        val user = User(
            email = response.user.email,
            displayName = response.user.displayName,
            userId = response.user.userId,
            role = response.user.role,
        )
        persistSession(user, response.token, rememberMe)
    }

    override suspend fun logout() {
        cachedSession = null
        sessionStorage.clear()
    }

    override fun currentSession(): Session? = cachedSession

    private fun persistSession(user: User, token: String, rememberMe: Boolean): User {
        val session = Session(token = token, user = user)
        cachedSession = session
        // Always persist to storage so the HTTP client's token provider can find the token
        // for authenticated API requests. The rememberMe flag controls only whether the
        // session survives a full page reload (since we use localStorage for web).
        sessionStorage.writeRaw(Json.encodeToString(Session.serializer(), session))
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

/** Reads just the bearer token out of a stored session, for attaching to outgoing requests
 *  (see [com.example.faunabahav.data.remote.HttpClientFactory]) — a session may not exist yet
 *  (logged out, or "remember me" was off), in which case requests simply go out unauthenticated. */
fun sessionTokenFrom(raw: String?): String? = decodeSession(raw)?.token
