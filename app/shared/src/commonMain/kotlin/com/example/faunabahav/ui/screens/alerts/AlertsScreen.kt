package com.example.faunabahav.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.FeedbackRepository
import com.example.faunabahav.data.settings.SettingsKeys
import com.example.faunabahav.data.settings.rememberSettingsStorage
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.Feedback
import com.example.faunabahav.model.User
import com.example.faunabahav.notification.NoOpAlertNotifier
import com.example.faunabahav.notification.rememberAlertNotifier
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.screens.devices.DevicesViewModel
import com.example.faunabahav.ui.screens.observations.EmptyState
import com.example.faunabahav.ui.screens.observations.ObservationPaginationBar
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.theme.PrimaryGreen
import com.example.faunabahav.ui.util.AlertFilterState
import com.example.faunabahav.ui.util.AlertStatusTab
import com.example.faunabahav.ui.util.applyFilters
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlertsScreen(
    repository: AlertRepository,
    deviceRepository: DeviceRepository,
    feedbackRepository: FeedbackRepository,
    baseUrl: String,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onOpenObservation: () -> Unit = {},
    viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(repository) } },
    ),
    devicesViewModel: DevicesViewModel = viewModel(
        factory = viewModelFactory { initializer { DevicesViewModel(deviceRepository) } },
    ),
) {
    // Set directly on the body-scoped instance rather than threaded through the ViewModel's
    // constructor/factory (see the comment on AlertsViewModel.alertNotifier for why).
    val settingsStorage = rememberSettingsStorage()
    val pushNotificationsEnabled = settingsStorage.getBoolean(SettingsKeys.PUSH_NOTIFICATIONS, true)
    val realAlertNotifier = rememberAlertNotifier()
    viewModel.alertNotifier = if (pushNotificationsEnabled) realAlertNotifier else NoOpAlertNotifier

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val justResolvedIds by viewModel.justResolvedIds.collectAsStateWithLifecycle()
    val devicesState by devicesViewModel.uiState.collectAsStateWithLifecycle()
    val devices = (devicesState as? UiState.Success)?.data ?: emptyList()
    val coroutineScope = rememberCoroutineScope()

    fun submitFeedback(alert: Alert, correctedBehaviour: String) {
        val eventId = alert.eventId ?: return
        val userId = currentUser?.userId ?: return
        coroutineScope.launch {
            feedbackRepository.submitFeedback(Feedback(eventId = eventId, userId = userId, correctedBehaviour = correctedBehaviour))
        }
    }

    var filters by remember { mutableStateOf(AlertFilterState()) }
    var currentPage by remember { mutableStateOf(0) }
    var itemsPerPage by remember { mutableStateOf(10) }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    var lastLoadedAt by remember { mutableStateOf(Clock.System.now()) }
    var nowTick by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) { viewModel.startPolling() }
    LaunchedEffect(pushNotificationsEnabled) {
        if (pushNotificationsEnabled) viewModel.alertNotifier.requestPermission()
    }
    LaunchedEffect(state) {
        if (state is UiState.Success) lastLoadedAt = Clock.System.now()
    }
    LaunchedEffect(Unit) {
        while (true) {
            nowTick = Clock.System.now()
            delay(1000)
        }
    }

    val isWide = rememberIsWideScreen()

    Row(modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
            AlertsHeader(
                lastUpdated = lastLoadedAt,
                now = nowTick,
                onRefresh = { viewModel.load(); devicesViewModel.load() },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            actionError?.let { message ->
                ActionErrorBanner(message = message, onDismiss = viewModel::clearActionError)
                Spacer(Modifier.height(12.dp))
            }

            when (state) {
                is UiState.Loading -> AlertsLoadingSkeleton()
                is UiState.Error -> AlertsErrorState(
                    message = (state as UiState.Error).message,
                    onRetry = viewModel::load,
                )
                is UiState.Success -> {
                    val allAlerts = (state as UiState.Success<List<Alert>>).data

                    // One scrollable Column for the whole section (stat cards + filters + list),
                    // matching ObservationsScreen's proven pattern — NOT a LazyColumn with
                    // weight(1f) as a sibling of the stat cards/filter bar. On a narrow phone the
                    // stat cards + filter bar alone are taller than the viewport, so a weight(1f)
                    // sibling shrinks to zero height and the whole list silently disappears
                    // instead of scrolling into view. Pagination already caps this to
                    // itemsPerPage (10 by default) items, so a plain (non-lazy) Column is fine.
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        AlertStatCards(allAlerts)
                        Spacer(Modifier.height(16.dp))

                        AlertFilterBar(
                            filters = filters,
                            onFiltersChange = { filters = it; currentPage = 0 },
                            devices = devices,
                        )
                        Spacer(Modifier.height(16.dp))

                        // Alerts resolved in this session vanish from view immediately (per
                        // user request) instead of lingering with a "Resolved" status in an
                        // Active/All/risk-tier list until the next reload — except on the
                        // "Resolved" tab itself, which exists specifically to show them.
                        val filtered = allAlerts.applyFilters(filters).let { list ->
                            if (filters.statusTab == AlertStatusTab.RESOLVED) {
                                list
                            } else {
                                list.filterNot { it.id in justResolvedIds }
                            }
                        }

                        if (filtered.isEmpty()) {
                            EmptyState(
                                if (allAlerts.isEmpty()) {
                                    "No alerts yet — the system will surface one automatically as soon as wildlife is detected."
                                } else {
                                    "No alerts match this filter."
                                },
                            )
                        } else {
                            val pageStart = currentPage * itemsPerPage
                            val pageItems = filtered.drop(pageStart).take(itemsPerPage)

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                pageItems.forEach { alert ->
                                    AlertCard(
                                        alert = alert,
                                        baseUrl = baseUrl,
                                        onViewDetails = { selectedAlert = alert },
                                        onMarkResolved = { viewModel.resolve(alert.id) },
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            ObservationPaginationBar(
                                currentPage = currentPage,
                                itemsPerPage = itemsPerPage,
                                totalItems = filtered.size,
                                onPageChange = { currentPage = it },
                                onItemsPerPageChange = { itemsPerPage = it; currentPage = 0 },
                                noun = "alerts",
                            )
                        }
                    }
                }
            }
        }

        if (isWide && selectedAlert != null) {
            Surface(
                modifier = Modifier.width(380.dp).fillMaxHeight(),
                shadowElevation = 4.dp,
            ) {
                AlertDetailsPanel(
                    alert = selectedAlert!!,
                    baseUrl = baseUrl,
                    onClose = { selectedAlert = null },
                    onOpenObservation = onOpenObservation,
                    onAcknowledge = {
                        currentUser?.let { viewModel.acknowledge(selectedAlert!!.id, it.userId) }
                    },
                    onResolve = { viewModel.resolve(selectedAlert!!.id); selectedAlert = null },
                    onSubmitFeedback = { text -> submitFeedback(selectedAlert!!, text) },
                )
            }
        }
    }

    if (!isWide) {
        selectedAlert?.let { alert ->
            Dialog(onDismissRequest = { selectedAlert = null }) {
                Surface(shape = MaterialTheme.shapes.medium) {
                    AlertDetailsPanel(
                        alert = alert,
                        baseUrl = baseUrl,
                        onClose = { selectedAlert = null },
                        onOpenObservation = onOpenObservation,
                        onAcknowledge = {
                            currentUser?.let { viewModel.acknowledge(alert.id, it.userId) }
                        },
                        onResolve = { viewModel.resolve(alert.id); selectedAlert = null },
                        onSubmitFeedback = { text -> submitFeedback(alert, text) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsHeader(
    lastUpdated: Instant,
    now: Instant,
    onRefresh: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        // weight(1f) is required here: Row measures non-weighted children sequentially, so
        // without it this Column's long subtitle would claim nearly the entire row width before
        // the "Live" cluster below is ever measured, squeezing it down to a sliver — forcing
        // "Live" to wrap one letter per line (the same bug class fixed in AlertCard.kt).
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text("Alerts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Real-time wildlife alerts and deterrence activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val secondsSinceLoad = (now - lastUpdated).inWholeSeconds
            Text(
                "Last updated: ${secondsSinceLoad}s ago",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
            Spacer(Modifier.width(8.dp))
            LiveDot()
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

@Composable
private fun ActionErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun LiveDot() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(PrimaryGreen, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text("Live", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AlertsLoadingSkeleton() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(80.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading alerts…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AlertsErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(60.dp))
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
