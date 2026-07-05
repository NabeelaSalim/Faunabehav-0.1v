package com.example.faunabahav.ui.util

import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.Outcome
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.model.Species

/**
 * These helpers turn the real fields the backend already returns (actions, risk_level,
 * outcome, species, behaviour) into the reference design's readable card copy. Nothing here
 * invents a value — every branch is a deterministic label/sentence for a real, already-computed
 * backend result; the underlying facts (which actions fired, what risk tier, what outcome) all
 * come from run_pipeline.py, not the UI.
 */

fun deterrenceLabel(actions: List<String>): String {
    val set = actions.toSet()
    return when {
        set.isEmpty() -> "N/A (image)"
        "activate_siren" in set && "activate_strobe" in set -> "Siren + Strobe Light"
        "activate_siren" in set -> "Siren Alert"
        "activate_sound" in set -> "Sound Deterrent"
        "log_uncertain_event" in set -> "None — Uncertain Detection"
        else -> "Monitoring Only"
    }
}

fun deterrenceExplanation(actions: List<String>): String {
    val set = actions.toSet()
    return when {
        set.isEmpty() -> "Behaviour analysis requires video, so no deterrence decision was made for this image."
        "activate_strobe" in set -> "High-frequency sound and flashing light have been activated to deter the animal."
        "activate_siren" in set -> "A siren alert has been activated in response to this detection."
        "activate_sound" in set -> "A graduated sound deterrent has been activated in response to this detection."
        "log_uncertain_event" in set -> "Detection confidence was too low to trigger an automated deterrent — logged for review instead."
        else -> "Risk was assessed as low, so this observation was logged without an active deterrent."
    }
}

fun actionStatusLabel(actions: List<String>): String = when {
    actions.isEmpty() -> "N/A"
    actions.any { it.startsWith("activate_") } -> "Triggered"
    else -> "Logged Only"
}

fun actionStatusExplanation(actions: List<String>): String = when {
    actions.isEmpty() -> "No deterrence stage runs for image uploads."
    actions.any { it.startsWith("activate_") } -> "Deterrent action has been activated successfully."
    else -> "No deterrent was required — event has been logged for monitoring."
}

fun riskExplanation(riskLevel: RiskLevel?, species: Species, behaviourCategory: BehaviourCategory?): String {
    if (riskLevel == null || behaviourCategory == null) {
        return "Risk assessment requires a behaviour classification, which needs video rather than a still image."
    }
    val speciesLabel = species.name.lowercase().replace('_', ' ')
    val behaviourLabel = behaviourCategory.name.lowercase().replace('_', ' ')
    return when (riskLevel) {
        RiskLevel.HIGH -> "${speciesLabel.replaceFirstChar { it.uppercase() }} detected exhibiting $behaviourLabel — classified as high risk."
        RiskLevel.MEDIUM -> "${speciesLabel.replaceFirstChar { it.uppercase() }} detected exhibiting $behaviourLabel — classified as medium risk."
        RiskLevel.LOW -> "${speciesLabel.replaceFirstChar { it.uppercase() }} detected exhibiting $behaviourLabel — classified as low risk."
    }
}

fun outcomeLabel(outcome: Outcome?): String = when (outcome) {
    Outcome.DETERRENCE_ACTIVATED -> "Animal Successfully Deterred"
    Outcome.FARMER_INTERVENTION_REQUIRED -> "Farmer Intervention Required"
    Outcome.MONITORING_CONTINUES -> "Monitoring Continues"
    null -> "N/A (image)"
}

fun outcomeExplanation(outcome: Outcome?): String = when (outcome) {
    Outcome.DETERRENCE_ACTIVATED ->
        "Automated deterrence was triggered successfully in response to this high-confidence detection."
    Outcome.FARMER_INTERVENTION_REQUIRED ->
        "High risk was detected with low model confidence — manual review is recommended."
    Outcome.MONITORING_CONTINUES ->
        "No urgent action needed. The system will keep monitoring for further activity."
    null -> "Behaviour analysis requires video, so no outcome was determined for this image."
}
