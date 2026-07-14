@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.faunabahav.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.faunabahav.model.Alert

@JsFun(
    "() => { if (typeof Notification !== 'undefined' && Notification.permission !== 'granted' " +
        "&& Notification.permission !== 'denied') { Notification.requestPermission(); } }",
)
private external fun jsRequestNotificationPermission()

@JsFun(
    "(title, body, tag) => { if (typeof Notification !== 'undefined' && Notification.permission === 'granted') " +
        "{ new Notification(title, { body: body, tag: tag }); } }",
)
private external fun jsShowNotification(title: String, body: String, tag: String)

private object WasmJsAlertNotifier : AlertNotifier {
    override fun requestPermission() = jsRequestNotificationPermission()

    override fun notifyHighRiskAlert(alert: Alert) {
        jsShowNotification(HIGH_RISK_NOTIFICATION_TITLE, alert.notificationBody(), "alert-${alert.id}")
    }
}

@Composable
actual fun rememberAlertNotifier(): AlertNotifier = remember { WasmJsAlertNotifier }