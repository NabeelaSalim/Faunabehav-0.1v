package com.example.faunabahav.data.session

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "faunabahav_session"
private const val KEY_SESSION = "session"

actual class SessionStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun readRaw(): String? = prefs.getString(KEY_SESSION, null)

    actual fun writeRaw(value: String) {
        prefs.edit().putString(KEY_SESSION, value).apply()
    }

    actual fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }
}

@Composable
actual fun rememberSessionStorage(): SessionStorage {
    val context = LocalContext.current
    return remember(context) { SessionStorage(context) }
}
