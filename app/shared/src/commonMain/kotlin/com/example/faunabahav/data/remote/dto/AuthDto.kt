package com.example.faunabahav.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val username: String,
)

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: Int,
    val email: String,
    val username: String,
    val role: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    val user: UserDto,
)
