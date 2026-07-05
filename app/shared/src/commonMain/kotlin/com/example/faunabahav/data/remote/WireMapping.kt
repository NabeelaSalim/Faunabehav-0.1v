package com.example.faunabahav.data.remote

import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.MediaType
import com.example.faunabahav.model.Outcome
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.model.Species
import kotlin.time.Instant

class UnknownWireValueException(field: String, rawValue: String) :
    Exception("Unrecognized \"$field\" value from backend: \"$rawValue\"")

private fun String.normalizeToken(): String =
    trim().lowercase().replace(Regex("[\\s-]+"), "_")

private val TIMEZONE_OFFSET_SUFFIX = Regex("(Z|[+-]\\d{2}:?\\d{2})$")

/**
 * The backend emits naive datetimes with no timezone offset (e.g. "2026-06-29T11:14:39.872369"),
 * which `Instant.parse` rejects outright. There's no way to know for certain what timezone the
 * server's clock is in, but treating it as UTC is the most common convention and the only way to
 * get a usable Instant out of this until the backend starts including an offset.
 */
fun parseTimestamp(raw: String): Instant {
    val withOffset = if (TIMEZONE_OFFSET_SUFFIX.containsMatchIn(raw)) raw else "${raw}Z"
    return Instant.parse(withOffset)
}

/**
 * The backend's `animal` field is a free-text string with no server-side validation, but the
 * product only ever trains/expects these three species. Normalize common casings and fail loudly
 * on anything else, since silently misclassifying a species would be worse than surfacing an error.
 */
fun parseSpecies(raw: String): Species = when (raw.normalizeToken()) {
    "monkey" -> Species.MONKEY
    "wild_boar", "boar", "wildboar" -> Species.WILD_BOAR
    "bird" -> Species.BIRD
    else -> throw UnknownWireValueException("animal", raw)
}

/**
 * Same free-text situation as `animal`, but here an unrecognized value falls back to
 * UNKNOWN_UNCLEAR instead of failing — the product already treats behaviour uncertainty as an
 * expected, normal outcome rather than an error.
 */
fun parseBehaviourCategory(raw: String): BehaviourCategory = when (raw.normalizeToken()) {
    "feeding_foraging" -> BehaviourCategory.FEEDING_FORAGING
    "locomotion" -> BehaviourCategory.LOCOMOTION
    "vigilance_alert" -> BehaviourCategory.VIGILANCE_ALERT
    "aggressive_destructive" -> BehaviourCategory.AGGRESSIVE_DESTRUCTIVE
    "resting_passive" -> BehaviourCategory.RESTING_PASSIVE
    "social_interaction" -> BehaviourCategory.SOCIAL_INTERACTION
    else -> BehaviourCategory.UNKNOWN_UNCLEAR
}

/**
 * risk_level drives automatic deterrence decisions, so unlike behaviour, an unrecognized value
 * must fail loudly rather than silently default to a risk tier that wasn't actually predicted.
 */
fun parseRiskLevel(raw: String): RiskLevel = when (raw.normalizeToken()) {
    "low" -> RiskLevel.LOW
    "medium" -> RiskLevel.MEDIUM
    "high" -> RiskLevel.HIGH
    else -> throw UnknownWireValueException("risk_level", raw)
}

/**
 * outcome is a backend-computed label describing how the automated deterrence decision
 * resolved (not a physical confirmation) — null for image uploads, which never reach the
 * deterrence stage.
 */
fun parseOutcome(raw: String): Outcome = when (raw.normalizeToken()) {
    "deterrence_activated" -> Outcome.DETERRENCE_ACTIVATED
    "farmer_intervention_required" -> Outcome.FARMER_INTERVENTION_REQUIRED
    "monitoring_continues" -> Outcome.MONITORING_CONTINUES
    else -> throw UnknownWireValueException("outcome", raw)
}

fun parseMediaType(raw: String): MediaType = when (raw.normalizeToken()) {
    "image" -> MediaType.IMAGE
    "video" -> MediaType.VIDEO
    else -> throw UnknownWireValueException("media_type", raw)
}