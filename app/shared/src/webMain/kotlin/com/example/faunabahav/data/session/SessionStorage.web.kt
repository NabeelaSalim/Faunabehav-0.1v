package com.example.faunabahav.data.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

private const val KEY_SESSION = "faunabahav_session"

actual class SessionStorage {
    actual fun readRaw(): String? = window.localStorage.getItem(KEY_SESSION)

    actual fun writeRaw(value: String) {
        window.localStorage.setItem(KEY_SESSION, value)
    }

    actual fun clear() {
        window.localStorage.removeItem(KEY_SESSION)
    }
}

@Composable
actual fun rememberSessionStorage(): SessionStorage = remember { SessionStorage() }
