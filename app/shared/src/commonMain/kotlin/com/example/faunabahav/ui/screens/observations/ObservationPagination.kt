package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PageSizeOptions = listOf(12, 24, 48)

@Composable
fun ObservationPaginationBar(
    currentPage: Int,
    itemsPerPage: Int,
    totalItems: Int,
    onPageChange: (Int) -> Unit,
    onItemsPerPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    noun: String = "observations",
) {
    val totalPages = if (totalItems == 0) 1 else ((totalItems - 1) / itemsPerPage) + 1
    val startIndex = if (totalItems == 0) 0 else currentPage * itemsPerPage + 1
    val endIndex = minOf((currentPage + 1) * itemsPerPage, totalItems)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (totalItems == 0) "No $noun" else "Showing $startIndex to $endIndex of $totalItems $noun",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { if (currentPage > 0) onPageChange(currentPage - 1) }, enabled = currentPage > 0) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous page")
            }
            val visiblePages = (0 until totalPages).toList().take(5)
            visiblePages.forEach { page ->
                PageNumberChip(page + 1, isSelected = page == currentPage, onClick = { onPageChange(page) })
            }
            IconButton(
                onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1,
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next page")
            }
        }

        ItemsPerPageDropdown(itemsPerPage, onItemsPerPageChange)
    }
}

@Composable
private fun PageNumberChip(number: Int, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(32.dp),
        onClick = onClick,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                number.toString(),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsPerPageDropdown(selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Items per page:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected.toString(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.width(90.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                PageSizeOptions.forEach { size ->
                    DropdownMenuItem(text = { Text(size.toString()) }, onClick = { onSelect(size); expanded = false })
                }
            }
        }
    }
}
