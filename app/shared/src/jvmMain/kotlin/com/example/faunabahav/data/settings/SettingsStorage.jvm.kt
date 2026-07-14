package com.example.faunabahav.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Desktop is out of active scope (Android + Web only) — in-memory only, does not survive restart. */
actual class SettingsStorage {
    private val values = mutableMapOf<String, Boolean>()

    actual fun getBoolean(key: String, default: Boolean): Boolean = values[key] ?: default

    actual fun setBoolean(key: String, value: Boolean) {
        values[key] = value
    }
}

@Composable
actual fun rememberSettingsStorage(): SettingsStorage = remember { SettingsStorage() }