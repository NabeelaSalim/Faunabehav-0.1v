package com.example.faunabahav.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.faunabahav.model.BoundingBox
import com.example.faunabahav.ui.theme.AccentOrange

@Composable
fun ObservationThumbnail(
    url: String,
    modifier: Modifier = Modifier,
    boundingBox: BoundingBox? = null,
    frameWidth: Int? = null,
    frameHeight: Int? = null,
    boxColor: Color = AccentOrange,
    /** Real text from the backend response (e.g. "Wild Boar 93%") drawn as a small chip above
     *  the box, matching the reference design — null draws just the box with no label. */
    label: String? = null,
) {
    val canOverlayBox = boundingBox != null && frameWidth != null && frameHeight != null &&
        frameWidth > 0 && frameHeight > 0

    if (!canOverlayBox) {
        AsyncImage(
            model = url,
            contentDescription = "Observation frame",
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        return
    }

    // A real box requires exact scale math, which ContentScale.Crop's hidden cropping makes
    // unreliable — Fit keeps the whole frame visible so the overlay lines up precisely.
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Observation frame",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        val density = LocalDensity.current
        val boxWidthPx = with(density) { maxWidth.toPx() }
        val boxHeightPx = with(density) { maxHeight.toPx() }
        val textMeasurer = rememberTextMeasurer()

        Canvas(Modifier.fillMaxSize()) {
            val imageAspect = frameWidth.toFloat() / frameHeight.toFloat()
            val boxAspect = boxWidthPx / boxHeightPx
            val (renderedWidth, renderedHeight) = if (imageAspect > boxAspect) {
                boxWidthPx to boxWidthPx / imageAspect
            } else {
                boxHeightPx * imageAspect to boxHeightPx
            }
            val offsetX = (boxWidthPx - renderedWidth) / 2f
            val offsetY = (boxHeightPx - renderedHeight) / 2f
            val scale = renderedWidth / frameWidth.toFloat()

            val left = offsetX + boundingBox.x1.toFloat() * scale
            val top = offsetY + boundingBox.y1.toFloat() * scale
            val right = offsetX + boundingBox.x2.toFloat() * scale
            val bottom = offsetY + boundingBox.y2.toFloat() * scale

            drawRect(
                color = boxColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 3.dp.toPx()),
            )

            if (label != null) {
                val textLayout = textMeasurer.measure(
                    label,
                    style = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                )
                val padding = 4.dp.toPx()
                val chipWidth = textLayout.size.width + padding * 2
                val chipHeight = textLayout.size.height + padding * 2
                val chipTop = (top - chipHeight).coerceAtLeast(0f)
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, chipTop),
                    size = Size(chipWidth, chipHeight),
                )
                drawText(
                    textLayout,
                    topLeft = Offset(left + padding, chipTop + padding),
                )
            }
        }
    }
}
