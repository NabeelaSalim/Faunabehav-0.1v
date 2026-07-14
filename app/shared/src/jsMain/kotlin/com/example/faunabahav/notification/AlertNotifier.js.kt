package com.example.faunabahav.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.faunabahav.model.Alert
import org.w3c.notifications.DENIED
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission

private object BrowserAlertNotifier : AlertNotifier {
    override fun requestPermission() {
        if (Notification.permission != NotificationPermission.GRANTED &&
            Notification.permission != NotificationPermission.DENIED
        ) {
            Notification.requestPermission { }
        }
    }

    override fun notifyHighRiskAlert(alert: Alert) {
        if (Notification.permission != NotificationPermission.GRANTED) return
        // NotificationOptions(...)'s generated helper doesn't reliably apply its declared
        // defaults for the params this call skips — build the plain JS object directly instead.
        val options = js("({})")
        options.body = alert.notificationBody()
        options.tag = "alert-${alert.id}"
        Notification(HIGH_RISK_NOTIFICATION_TITLE, options.unsafeCast<NotificationOptions>())
    }
}

@Composable
actual fun rememberAlertNotifier(): AlertNotifier = remember { BrowserAlertNotifier }