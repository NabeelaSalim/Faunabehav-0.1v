package com.example.faunabahav.data.remote

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

sealed interface ApiError {
    data class Network(val message: String) : ApiError
    data class Server(val code: Int, val message: String) : ApiError
    data class Serialization(val message: String) : ApiError
    data class Unknown(val message: String) : ApiError
}

internal suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: UnknownWireValueException) {
    ApiResult.Failure(ApiError.Serialization(e.message ?: "Unrecognized value from backend"))
} catch (e: ResponseException) {
    ApiResult.Failure(ApiError.Server(e.response.status.value, e.message ?: "Server error"))
} catch (e: Throwable) {
    // On Kotlin/JS, a failed fetch() surfaces as a raw Throwable, not necessarily a
    // kotlin.Exception subtype — catching only Exception here left it uncaught in practice.
    ApiResult.Failure(ApiError.Network(e.message ?: "Network error"))
}