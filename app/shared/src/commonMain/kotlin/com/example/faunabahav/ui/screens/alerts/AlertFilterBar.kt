package com.example.faunabahav.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.Species
import com.example.faunabahav.ui.components.FilterDropdown
import com.example.faunabahav.ui.theme.DarkGreen
import com.example.faunabahav.ui.util.AlertFilterState
import com.example.faunabahav.ui.util.AlertSort
import com.example.faunabahav.ui.util.AlertStatusTab
import com.example.faunabahav.ui.util.displayName
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AlertFilterBar(
    filters: AlertFilterState,
    onFiltersChange: (AlertFilterState) -> Unit,
    devices: List<Device>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AlertStatusTab.entries.forEach { tab ->
                StatusPill(
                    label = tab.label,
                    selected = filters.statusTab == tab,
                    onClick = { onFiltersChange(filters.copy(statusTab = tab)) },
                )
            }
            Spacer(Modifier.widthIn(min = 16.dp))
            SortDropdown(selected = filters.sort, onSelect = { onFiltersChange(filters.copy(sort = it)) })
        }
        Spacer(Modifier.widthIn(min = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterDropdown(
                label = "Species",
                selectedLabel = filters.species?.displayName() ?: "All Species",
                options = listOf<Species?>(null) + Species.entries,
                optionLabel = { it?.displayName() ?: "All Species" },
                onSelect = { onFiltersChange(filters.copy(species = it)) },
            )
            FilterDropdown(
                label = "Camera",
                selectedLabel = devices.find { it.id == filters.deviceId }?.name ?: "All Cameras",
                options = listOf<Int?>(null) + devices.map { it.id },
                optionLabel = { id -> id?.let { i -> devices.find { it.id == i }?.name } ?: "All Cameras" },
                onSelect = { onFiltersChange(filters.copy(deviceId = it)) },
            )
            FilterDropdown(
                label = "Behaviour",
                selectedLabel = filters.behaviour?.name ?: "All",
                options = listOf<BehaviourCategory?>(null) + BehaviourCategory.entries,
                optionLabel = { it?.name ?: "All" },
                onSelect = { onFiltersChange(filters.copy(behaviour = it)) },
            )
            DateRangeFilter(
                dateRange = filters.dateRange,
                onSelect = { onFiltersChange(filters.copy(dateRange = it)) },
            )
            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = { onFiltersChange(filters.copy(searchQuery = it)) },
                placeholder = { Text("Search alerts…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.widthIn(min = 200.dp),
            )
            if (!filters.isDefault) {
                TextButton(onClick = { onFiltersChange(AlertFilterState()) }) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.widthIn(16.dp))
                    Text("Reset Filters")
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) DarkGreen else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        label,
        color = contentColor,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdown(selected: AlertSort, onSelect: (AlertSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "Sort: ${selected.label}",
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.widthIn(min = 190.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AlertSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sort.label) },
                    onClick = {
                        onSelect(sort)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilter(dateRange: ClosedRange<LocalDate>?, onSelect: (ClosedRange<LocalDate>?) -> Unit) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf(dateRange?.start) }

    val label = if (dateRange != null) "${dateRange.start} to ${dateRange.endInclusive}" else "Date range"

    Button(onClick = { showStartPicker = true }) {
        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.widthIn(16.dp))
        Text(label)
    }

    if (showStartPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingStart = state.selectedDateMillis?.let { it.toLocalDate() }
                    showStartPicker = false
                    if (pendingStart != null) showEndPicker = true
                }) { Text("Next: pick end date") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val end = state.selectedDateMillis?.let { it.toLocalDate() }
                    val start = pendingStart
                    if (start != null && end != null) {
                        onSelect(if (start <= end) start..end else end..start)
                    }
                    showEndPicker = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
