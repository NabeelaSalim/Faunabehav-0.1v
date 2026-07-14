package com.example.faunabahav.data.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "faunabahav_settings"

actual class SettingsStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    actual fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

@Composable
actual fun rememberSettingsStorage(): SettingsStorage {
    val context = LocalContext.current
    return remember(context) { SettingsStorage(context) }
}