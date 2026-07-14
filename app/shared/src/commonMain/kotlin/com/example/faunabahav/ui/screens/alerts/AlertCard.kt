package com.example.faunabahav.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.faunabahav.model.Alert
import com.example.faunabahav.ui.components.RiskBadge
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.theme.AccentOrange
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.DarkGreen
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.color
import com.example.faunabahav.ui.util.displayName
import com.example.faunabahav.ui.util.displayNameOrImageNote
import com.example.faunabahav.ui.util.emoji
import com.example.faunabahav.ui.util.formatTimeOnly
import com.example.faunabahav.ui.util.toActionChips
import com.example.faunabahav.ui.util.toFrameUrl
import com.example.faunabahav.ui.util.toPercentOrDash

private val WideThumbnailWidth = 180.dp
private val WideThumbnailHeight = 120.dp
private val NarrowThumbnailSize = 72.dp
private val StripWidth = 4.dp
private val ActionColumnWidth = 160.dp

/**
 * Two structurally different layouts, not just a resized version of one another:
 *
 * Wide (desktop/tablet): a single horizontal Row — [strip][fixed 180x120 thumbnail][info
 * column, weight(1f)][fixed-width action buttons] — matching docs/alerts.png.
 *
 * Narrow (phone): a fixed 180dp-wide thumbnail plus up-to-160dp-wide action column together
 * already approach a typical phone's ~360-411dp width before the flexible info column gets
 * anything, so on phones the info rows go full-width below a compact thumbnail+title header,
 * with the two actions as a full-width button row at the bottom — never three width-competing
 * siblings squeezed into one Row.
 */
@Composable
fun AlertCard(
    alert: Alert,
    baseUrl: String,
    onViewDetails: () -> Unit,
    onMarkResolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isResolved = alert.status.equals("resolved", ignoreCase = true)
    val stripColor = alert.riskLevel.color()

    Card(modifier.fillMaxWidth()) {
        if (rememberIsWideScreen()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Colored left border matching the card's real (content-driven) height,
                    // painted in the draw phase using the Row's own final measured size — no
                    // intrinsic-measurement pass involved, so it's compatible with the
                    // BoxWithConstraints-based thumbnail sibling.
                    .drawBehind { drawRect(color = stripColor, size = Size(StripWidth.toPx(), size.height)) }
                    .padding(start = StripWidth)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AlertThumbnail(alert, baseUrl, WideThumbnailWidth, WideThumbnailHeight)

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AlertCardHeader(alert)
                    AlertCardInfoRows(alert)
                }

                // A bounded (not unbounded) width is required here: on a wide screen there's
                // ample room regardless, but leaving this Row child fully unconstrained still
                // lets Compose's layout pass hand it more space than its wrap-content buttons
                // need, at the expense of the weight(1f) info column next to it.
                Column(
                    modifier = Modifier.widthIn(max = ActionColumnWidth),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = onViewDetails) { Text("View Details →") }
                    if (!isResolved) {
                        Button(
                            onClick = onMarkResolved,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        ) { Text("Mark Resolved") }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(color = stripColor, size = Size(StripWidth.toPx(), size.height)) }
                    .padding(start = StripWidth)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AlertThumbnail(alert, baseUrl, NarrowThumbnailSize, NarrowThumbnailSize)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AlertCardHeader(alert)
                    }
                }
                Spacer(Modifier.height(10.dp))
                AlertCardInfoRows(alert)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onViewDetails, modifier = Modifier.weight(1f)) {
                        Text("View Details →")
                    }
                    if (!isResolved) {
                        Button(
                            onClick = onMarkResolved,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            modifier = Modifier.weight(1f),
                        ) { Text("Mark Resolved") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCardHeader(alert: Alert) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(alert.species.emoji(), style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${alert.species.displayName().uppercase()} DETECTED",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.weight(1f, fill = false).padding(start = 6.dp),
        )
        Spacer(Modifier.width(8.dp))
        RiskBadge(alert.riskLevel)
    }
    Text(
        alert.behaviourCategory.displayNameOrImageNote(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AlertCardInfoRows(alert: Alert) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MetaRow(Icons.Filled.PhotoCamera, alert.camera ?: "Unknown camera")
        MetaRow(Icons.Filled.Place, alert.location)
        MetaRow(Icons.Filled.AccessTime, formatTimeOnly(alert.timestamp))

        LabeledValue("Confidence", alert.confidence.toPercentOrDash())
        LabeledValue(
            "Deterrence",
            alert.deterrenceAction.toActionChips()
                .joinToString(" + ") { it.label.removeSuffix(" Activated").removeSuffix(" Sent") }
                .ifBlank { "None" },
        )
        StatusValue(alert.status)
    }
}

@Composable
private fun AlertThumbnail(alert: Alert, baseUrl: String, width: Dp, height: Dp) {
    val framePath = alert.framePath
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (framePath != null) {
            AsyncImage(
                model = framePath.toFrameUrl(baseUrl),
                contentDescription = "${alert.species.displayName()} detection",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Real backend has no frame_path for this alert (e.g. an older row from before
            // images were tracked) — an icon placeholder, never a stock photo.
            Icon(
                Icons.Filled.BrokenImage,
                contentDescription = "No image available",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun StatusValue(status: String) {
    val color = when {
        status.equals("resolved", ignoreCase = true) -> PrimaryGreen
        status.equals("monitoring", ignoreCase = true) -> AccentOrange
        else -> DangerRed
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Circle, contentDescription = null, tint = color, modifier = Modifier.height(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(status, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
