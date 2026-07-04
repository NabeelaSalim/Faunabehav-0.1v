package com.example.faunabahav.data.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** iOS is out of active scope (Android + Web only) — in-memory only, does not survive restart. */
actual class SessionStorage {
    private var value: String? = null

    actual fun readRaw(): String? = value

    actual fun writeRaw(value: String) {
        this.value = value
    }

    actual fun clear() {
        value = null
    }
}

@Composable
actual fun rememberSessionStorage(): SessionStorage = remember { SessionStorage() }
