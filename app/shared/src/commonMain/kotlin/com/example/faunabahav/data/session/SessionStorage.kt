package com.example.faunabahav.data.session

import androidx.compose.runtime.Composable

/** Persists a single serialized session string across app restarts, platform-backed. */
expect class SessionStorage {
    fun readRaw(): String?
    fun writeRaw(value: String)
    fun clear()
}

@Composable
expect fun rememberSessionStorage(): SessionStorage
