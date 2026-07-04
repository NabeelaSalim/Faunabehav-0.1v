package com.example.faunabahav.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.User
import com.example.faunabahav.ui.components.UserProfileCard
import com.example.faunabahav.ui.theme.DarkGreen
import com.example.faunabahav.ui.theme.ForestGreen

@Composable
fun AppSidebar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    user: User?,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Brush.verticalGradient(listOf(ForestGreen, DarkGreen))),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("FaunaBehav", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }

        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Destination.entries.forEach { dest ->
                SidebarItem(
                    destination = dest,
                    isSelected = dest == selected,
                    onClick = { onSelect(dest) },
                )
            }
        }

        UserProfileCard(user = user, onLogout = onLogout)
    }
}

@Composable
private fun SidebarItem(destination: Destination, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(destination.icon, contentDescription = destination.label, tint = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(destination.label, color = Color.White)
    }
}
