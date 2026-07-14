package com.example.faunabahav.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** [tokenProvider] is read fresh on every request (not captured once) so login/logout during the
 *  app's lifetime is reflected immediately without needing to recreate the client. Returns null
 *  when there's no active session — requests simply go out unauthenticated, same as today. */
fun createHttpClient(
    baseUrl: String = defaultBaseUrl(),
    tokenProvider: () -> String? = { null },
): HttpClient = HttpClient {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    install(Logging) {
        level = LogLevel.INFO
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }

    defaultRequest {
        url(baseUrl)
        tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }
}