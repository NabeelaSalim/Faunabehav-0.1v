package com.example.faunabahav.ui.util

import com.example.faunabahav.model.Observation

/**
 * `frame_path` is inconsistent in real backend data — most rows are like "frames/wildboar.jpg",
 * but some older rows are a bare filename like "frame.jpg" with no "frames/" prefix. Normalize
 * both to a URL under the backend's `/frames` static mount.
 */
fun Observation.frameUrl(baseUrl: String): String {
    val relativePath = framePath.removePrefix("frames/").removePrefix("/")
    return "${baseUrl.trimEnd('/')}/frames/$relativePath"
}
