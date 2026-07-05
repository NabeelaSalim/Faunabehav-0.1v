package com.example.faunabahav.ui.util

import com.example.faunabahav.model.Observation

/**
 * The backend's real deterrence_action field is a comma-joined string of raw action tokens
 * (e.g. "activate_siren, send_push_notification, log_event"). Only three tokens correspond to
 * an actual physical deterrent the backend can trigger — everything else (log_event,
 * log_uncertain_event, send_sms, send_push_notification) is a notification/logging side effect,
 * not a deterrent. An observation whose only tokens are non-physical is bucketed as "Other"
 * (monitored, no physical deterrent fired) rather than silently dropped, so the total still
 * reconciles with the real number of observations that went through a deterrence decision.
 */
private val PhysicalDeterrenceTokens = mapOf(
    "activate_siren" to "Siren",
    "activate_strobe" to "Light",
    "activate_sound" to "Sound",
)

/**
 * Real, deterministic status label for a single observation row, derived the same way as
 * deterrenceCategoryBreakdown(): null deterrenceAction means the image-only pipeline never
 * reached the deterrence stage; a real physical token means it was triggered; otherwise the
 * event was only logged/notified with no physical deterrent.
 */
fun Observation.statusLabel(): String {
    val action = deterrenceAction ?: return "New Detection"
    val tokens = action.split(",").map { it.trim().lowercase() }
    return if (tokens.any { it in setOf("activate_siren", "activate_strobe", "activate_sound") }) {
        "Triggered"
    } else {
        "Monitoring"
    }
}

fun List<Observation>.deterrenceCategoryBreakdown(): Map<String, Int> {
    val counts = linkedMapOf("Sound" to 0, "Light" to 0, "Siren" to 0, "Other" to 0)
    for (observation in this) {
        val action = observation.deterrenceAction ?: continue
        val tokens = action.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) continue

        var matchedPhysical = false
        for (token in tokens) {
            val category = PhysicalDeterrenceTokens[token]
            if (category != null) {
                counts[category] = (counts[category] ?: 0) + 1
                matchedPhysical = true
            }
        }
        if (!matchedPhysical) {
            counts["Other"] = (counts["Other"] ?: 0) + 1
        }
    }
    return counts
}
