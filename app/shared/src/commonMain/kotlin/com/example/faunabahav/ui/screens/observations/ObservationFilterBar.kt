package com.example.faunabahav.ui.screens.observations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.faunabahav.model.BehaviourCategory
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.model.Species
import com.example.faunabahav.ui.components.FilterDropdown
import com.example.faunabahav.ui.util.ObservationFilterState
import com.example.faunabahav.ui.util.ObservationSort
import com.example.faunabahav.ui.util.displayName
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ObservationFilterBar(
    filters: ObservationFilterState,
    onFiltersChange: (ObservationFilterState) -> Unit,
    devices: List<Device>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterDropdown(
                label = "Risk Level",
                selectedLabel = filters.riskLevel?.name ?: "All",
                options = listOf<RiskLevel?>(null) + RiskLevel.entries,
                optionLabel = { it?.name ?: "All" },
                onSelect = { onFiltersChange(filters.copy(riskLevel = it)) },
            )
            FilterDropdown(
                label = "Species",
                selectedLabel = filters.species?.displayName() ?: "All",
                options = listOf<Species?>(null) + Species.entries,
                optionLabel = { it?.displayName() ?: "All" },
                onSelect = { onFiltersChange(filters.copy(species = it)) },
            )
            FilterDropdown(
                label = "Behaviour",
                selectedLabel = filters.behaviour?.name ?: "All",
                options = listOf<BehaviourCategory?>(null) + BehaviourCategory.entries,
                optionLabel = { it?.name ?: "All" },
                onSelect = { onFiltersChange(filters.copy(behaviour = it)) },
            )
            FilterDropdown(
                label = "Camera",
                selectedLabel = devices.find { it.id == filters.deviceId }?.name ?: "All",
                options = listOf<Int?>(null) + devices.map { it.id },
                optionLabel = { id -> id?.let { i -> devices.find { it.id == i }?.name } ?: "All" },
                onSelect = { onFiltersChange(filters.copy(deviceId = it)) },
            )
            DateRangeFilter(
                dateRange = filters.dateRange,
                onSelect = { onFiltersChange(filters.copy(dateRange = it)) },
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = { onFiltersChange(filters.copy(searchQuery = it)) },
                placeholder = { Text("Search observations…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.widthIn(min = 220.dp),
            )
            SortDropdown(
                selected = filters.sort,
                onSelect = { onFiltersChange(filters.copy(sort = it)) },
            )
            if (!filters.isDefault) {
                TextButton(onClick = { onFiltersChange(ObservationFilterState()) }) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                    Text("Reset Filters")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdown(selected: ObservationSort, onSelect: (ObservationSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sort by") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.width(190.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ObservationSort.entries.forEach { sort ->
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
        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.width(16.dp))
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
