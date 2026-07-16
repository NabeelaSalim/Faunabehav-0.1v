package com.example.faunabahav.ui.util

import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.Species

/** Renders a fraction (0.0-1.0) as a whole-number percentage, or a dash when absent
 *  (e.g. behaviour confidence is null for image uploads — YOLO-only, no R3D-18 result). */
fun Double?.toPercentOrDash(): String = this?.let { "${(it * 100).toInt()}%" } ?: "—"

/** "WILD_BOAR" -> "Wild boar" */
fun Species.displayName(): String = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

/** "AGGRESSIVE_DESTRUCTIVE" -> "Aggressive destructive", or "N/A (image)" when absent. */
fun BehaviourCategory?.displayNameOrImageNote(): String =
    this?.name?.lowercase()?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: "N/A (image)"

/** The system only ever classifies these three species — a plain emoji glyph avoids needing a
 *  custom icon asset per animal while still being visually distinct per species. */
fun Species.emoji(): String = when (this) {
    Species.WILD_BOAR -> "🐗"
    Species.MONKEY -> "🐒"
    Species.BIRD -> "🐦"
    Species.UNKNOWN -> "❓"
}

/** Renders an enum-ish name field, or a dash when the underlying value is absent. */
fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "—"

/** Renders a raw byte count as a human file size (e.g. "15.6 MB"). */
fun Int.toFileSizeLabel(): String {
    val kb = this / 1024.0
    if (kb < 1024) return "${(kb * 10).toInt() / 10.0} KB"
    val mb = kb / 1024.0
    return "${(mb * 10).toInt() / 10.0} MB"
}
