package com.example.faunabahav.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** iOS is out of active scope (Android + Web only), matching SessionStorage.ios.kt — local
 *  notifications would need UNUserNotificationCenter authorization wired up in the host app. */
@Composable
actual fun rememberAlertNotifier(): AlertNotifier = remember { NoOpAlertNotifier }