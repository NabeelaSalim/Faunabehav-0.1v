package com.example.faunabahav.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen

/** Mirrors the backend's real RISK_MAP in run_pipeline.py — used only to pick a consistent
 *  accent colour per behaviour in the UI, not to recompute risk (the real risk_level from the
 *  backend is always used for the risk badge itself). */
fun BehaviourCategory.riskTint(): RiskLevel = when (this) {
    BehaviourCategory.FEEDING_FORAGING, BehaviourCategory.AGGRESSIVE_DESTRUCTIVE -> RiskLevel.HIGH
    BehaviourCategory.VIGILANCE_ALERT, BehaviourCategory.SOCIAL_INTERACTION -> RiskLevel.MEDIUM
    BehaviourCategory.LOCOMOTION, BehaviourCategory.RESTING_PASSIVE -> RiskLevel.LOW
    BehaviourCategory.UNKNOWN_UNCLEAR -> RiskLevel.MEDIUM
}

fun RiskLevel.color(): Color = when (this) {
    RiskLevel.HIGH -> DangerRed
    RiskLevel.MEDIUM -> AccentOrange
    RiskLevel.LOW -> PrimaryGreen
}

fun BehaviourCategory.icon(): ImageVector = when (this) {
    BehaviourCategory.LOCOMOTION -> Icons.AutoMirrored.Filled.DirectionsWalk
    BehaviourCategory.FEEDING_FORAGING -> Icons.Filled.Restaurant
    BehaviourCategory.AGGRESSIVE_DESTRUCTIVE -> Icons.Filled.Warning
    BehaviourCategory.VIGILANCE_ALERT -> Icons.Filled.PriorityHigh
    BehaviourCategory.SOCIAL_INTERACTION -> Icons.Filled.Groups
    BehaviourCategory.RESTING_PASSIVE -> Icons.Filled.Hotel
    BehaviourCategory.UNKNOWN_UNCLEAR -> Icons.Filled.PriorityHigh
}

data class ActionChip(val icon: ImageVector, val label: String, val color: Color)

/** [deterrenceAction] is the backend's real comma-joined action-token string (or null for image
 *  uploads, which never reach the deterrence stage). Notification-only tokens are surfaced as
 *  their own real chip rather than folded away, matching the reference's "Farmer Notified" chip. */
fun String?.toActionChips(): List<ActionChip> {
    val tokens = this?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() } ?: return emptyList()
    if (tokens.isEmpty()) return emptyList()

    val chips = mutableListOf<ActionChip>()
    if ("activate_siren" in tokens) chips += ActionChip(Icons.AutoMirrored.Filled.VolumeUp, "Siren Activated", DangerRed)
    if ("activate_strobe" in tokens) chips += ActionChip(Icons.Filled.FlashOn, "Strobe Activated", DangerRed)
    if ("activate_sound" in tokens) chips += ActionChip(Icons.AutoMirrored.Filled.VolumeUp, "Sound Activated", AccentOrange)
    if ("send_push_notification" in tokens) chips += ActionChip(Icons.Filled.Notifications, "Farmer Notified", DangerRed)
    if ("send_sms" in tokens) chips += ActionChip(Icons.Filled.Sms, "SMS Sent", DangerRed)
    if (chips.isEmpty()) chips += ActionChip(Icons.Filled.CheckCircle, "Logged Only", PrimaryGreen)
    return chips
}
