package com.example.faunabahav.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.faunabahav.model.Alert

private const val CHANNEL_ID = "high_risk_alerts"
private const val PERMISSION_REQUEST_CODE = 4821

private class AndroidAlertNotifier(private val context: Context) : AlertNotifier {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "High-risk wildlife alerts",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Immediate alerts when a high-risk animal is detected" }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    override fun requestPermission() {
        if (hasPermission()) return
        val activity = context as? Activity ?: return
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PERMISSION_REQUEST_CODE,
        )
    }

    override fun notifyHighRiskAlert(alert: Alert) {
        if (!hasPermission()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(HIGH_RISK_NOTIFICATION_TITLE)
            .setContentText(alert.notificationBody())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(alert.id, notification)
    }
}

@Composable
actual fun rememberAlertNotifier(): AlertNotifier {
    val context = LocalContext.current
    return remember(context) { AndroidAlertNotifier(context) }
}