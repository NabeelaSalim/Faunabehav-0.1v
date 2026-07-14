package com.example.faunabahav.data.settings

import androidx.compose.runtime.Composable

object SettingsKeys {
    const val PUSH_NOTIFICATIONS = "push_notifications"
    const val WEATHER_ALERTS = "weather_alerts"
    const val DARK_MODE = "dark_mode"
}

expect class SettingsStorage {
    fun getBoolean(key: String, default: Boolean): Boolean
    fun setBoolean(key: String, value: Boolean)
}

@Composable
expect fun rememberSettingsStorage(): SettingsStorage