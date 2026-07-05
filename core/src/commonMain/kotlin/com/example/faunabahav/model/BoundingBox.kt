package com.example.faunabahav.model

import kotlinx.serialization.Serializable

/** Pixel coordinates relative to the frame the detection ran on (see [Observation.frameWidth]/[frameHeight]). */
@Serializable
data class BoundingBox(
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
)
