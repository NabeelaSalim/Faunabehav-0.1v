package com.example.faunabahav.ui.state

import com.example.faunabahav.data.remote.ApiError

fun ApiError.toUiMessage(): String = when (this) {
    is ApiError.Network -> "Network error: $message"
    is ApiError.Server -> "Server error ($code): $message"
    is ApiError.Serialization -> "Unexpected data from server: $message"
    is ApiError.Unknown -> message
}