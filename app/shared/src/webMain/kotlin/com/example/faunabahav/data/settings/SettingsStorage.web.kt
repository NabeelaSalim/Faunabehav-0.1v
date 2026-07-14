package com.example.faunabahav.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

private const val KEY_PREFIX = "faunabahav_setting_"

actual class SettingsStorage {
    actual fun getBoolean(key: String, default: Boolean): Boolean =
        when (window.localStorage.getItem(KEY_PREFIX + key)) {
            "true" -> true
            "false" -> false
            else -> default
        }

    actual fun setBoolean(key: String, value: Boolean) {
        window.localStorage.setItem(KEY_PREFIX + key, value.toString())
    }
}

@Composable
actual fun rememberSettingsStorage(): SettingsStorage = remember { SettingsStorage() }