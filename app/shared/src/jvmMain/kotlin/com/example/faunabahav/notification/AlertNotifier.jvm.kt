package com.example.faunabahav.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.faunabahav.model.Alert
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import java.awt.image.BufferedImage

/** Desktop has no permission model to request, and no reliable way to de-dupe a tray "balloon"
 *  by tag the way browser/Android notifications do — each call just posts a new balloon. */
private object DesktopAlertNotifier : AlertNotifier {
    private val trayIcon: TrayIcon? by lazy { createTrayIcon() }

    private fun createTrayIcon(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        return try {
            val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).also {
                it.graphics.apply {
                    color = java.awt.Color.RED
                    fillOval(0, 0, 16, 16)
                }
            }
            TrayIcon(image, "FaunaBahav").apply {
                isImageAutoSize = true
                SystemTray.getSystemTray().add(this)
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun requestPermission() = Unit

    override fun notifyHighRiskAlert(alert: Alert) {
        val icon = trayIcon ?: run { Toolkit.getDefaultToolkit().beep(); return }
        icon.displayMessage(HIGH_RISK_NOTIFICATION_TITLE, alert.notificationBody(), TrayIcon.MessageType.WARNING)
    }
}

@Composable
actual fun rememberAlertNotifier(): AlertNotifier = remember { DesktopAlertNotifier }