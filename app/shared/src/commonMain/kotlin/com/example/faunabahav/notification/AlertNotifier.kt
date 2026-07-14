package com.example.faunabahav.notification

import androidx.compose.runtime.Composable
import com.example.faunabahav.model.Alert
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote

/** Fires a local OS-level notification (browser Notification API, Android NotificationManager,
 *  etc.) for high-risk alerts — best-effort "live" delivery while the app is open, since the
 *  backend has no push/SMS/email channel of its own (see AlertsViewModel's polling fallback). */
interface AlertNotifier {
    fun requestPermission()
    fun notifyHighRiskAlert(alert: Alert)
}

@Composable
expect fun rememberAlertNotifier(): AlertNotifier

object NoOpAlertNotifier : AlertNotifier {
    override fun requestPermission() = Unit
    override fun notifyHighRiskAlert(alert: Alert) = Unit
}

internal const val HIGH_RISK_NOTIFICATION_TITLE = "High-risk wildlife alert"

internal fun Alert.notificationBody(): String =
    "${species.displayName()} — ${behaviourCategory.displayNameOrImageNote()} at $location"