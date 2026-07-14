package com.example.faunabahav.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.model.Alert
import com.example.faunabahav.model.RiskLevel
import com.example.faunabahav.notification.AlertNotifier
import com.example.faunabahav.notification.NoOpAlertNotifier
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PollInterval = 15_000L

class AlertsViewModel(
    private val repository: AlertRepository,
    // var, not constructor val: AlertsScreen obtains the real notifier via a @Composable default
    // parameter of its own (rememberAlertNotifier()), and threading a composable-default value
    // through ANOTHER parameter's viewModelFactory/initializer closure is unreliable — the
    // ViewModel can end up permanently bound to whatever this parameter's default (NoOp) resolved
    // to on the very first creation pass. Setting this as a plain assignment in the screen's
    // composable body sidesteps that: it re-runs on every recomposition with whatever the
    // current in-scope notifier is, well before any poll tick could use a stale one.
    var alertNotifier: AlertNotifier = NoOpAlertNotifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Alert>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Alert>>> = _uiState.asStateFlow()

    // Surfaces a real failure from resolve()/acknowledge() (e.g. the backend endpoint returning
    // 404 because it doesn't exist yet) as a visible message instead of the action silently
    // doing nothing — screen clears it once shown, see AlertsScreen's LaunchedEffect(actionError).
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    // Alerts resolved in this session, so AlertsScreen can hide them from view immediately
    // instead of leaving a "Resolved" card sitting in an Active/All/risk-tier list until the
    // next reload. Real data is untouched (the alert still exists with status="Resolved" on
    // the backend) — this only affects what the current screen renders, and the screen still
    // shows them normally when the user explicitly selects the "Resolved" filter tab.
    private val _justResolvedIds = MutableStateFlow<Set<Int>>(emptySet())
    val justResolvedIds: StateFlow<Set<Int>> = _justResolvedIds.asStateFlow()

    private var pollingStarted = false

    // Alert IDs already seen, so a high-risk alert only triggers a notification once — the very
    // first fetch just establishes the baseline (nothing existing on load should re-notify), and
    // every fetch after that notifies only for ids not seen before. Null until the first fetch
    // resolves, since Set<Int>() (empty) is a legitimate "no alerts yet" baseline too.
    private var knownAlertIds: Set<Int>? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getAlerts()) {
                is ApiResult.Success -> applyAlerts(result.data)
                is ApiResult.Failure -> _uiState.value = UiState.Error(result.error.toUiMessage())
            }
        }
    }

    /** "Live updates" fallback since this backend has no WebSocket/SSE — a silent background
     *  re-fetch every [PollInterval] ms that swaps the list in place without flashing the
     *  loading skeleton. Safe to call from multiple recompositions (e.g. screen re-entered);
     *  only the first call actually starts the loop. */
    fun startPolling() {
        if (pollingStarted) return
        pollingStarted = true
        viewModelScope.launch {
            while (true) {
                delay(PollInterval)
                when (val result = repository.getAlerts()) {
                    is ApiResult.Success -> applyAlerts(result.data)
                    is ApiResult.Failure -> Unit // keep showing the last good data on a background poll failure
                }
            }
        }
    }

    private fun applyAlerts(alerts: List<Alert>) {
        val previouslyKnown = knownAlertIds
        if (previouslyKnown != null) {
            alerts
                .filter { it.riskLevel == RiskLevel.HIGH && it.id !in previouslyKnown }
                .forEach(alertNotifier::notifyHighRiskAlert)
        }
        knownAlertIds = alerts.map { it.id }.toSet()
        _uiState.value = UiState.Success(alerts)
    }

    fun resolve(alertId: Int) {
        viewModelScope.launch {
            when (val result = repository.resolveAlert(alertId)) {
                is ApiResult.Success -> {
                    replaceAlert(result.data)
                    _justResolvedIds.value = _justResolvedIds.value + alertId
                }
                is ApiResult.Failure -> _actionError.value = result.error.toUiMessage()
            }
        }
    }

    fun acknowledge(alertId: Int, userId: Int) {
        viewModelScope.launch {
            when (val result = repository.acknowledgeAlert(alertId, userId)) {
                is ApiResult.Success -> replaceAlert(result.data)
                is ApiResult.Failure -> _actionError.value = result.error.toUiMessage()
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private fun replaceAlert(updated: Alert) {
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(current.data.map { if (it.id == updated.id) updated else it })
        }
    }
}