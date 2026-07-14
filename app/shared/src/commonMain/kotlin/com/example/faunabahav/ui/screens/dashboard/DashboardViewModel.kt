package com.example.faunabahav.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.DashboardRepository
import com.example.faunabahav.model.DashboardSummary
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<DashboardSummary>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardSummary>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getDashboardSummary()) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Failure -> UiState.Error(result.error.toUiMessage())
            }
        }
    }

    /** Background poll refresh: no Loading state, so the dashboard never blanks out mid-poll.
     *  MutableStateFlow only notifies collectors when the new value is unequal to the current
     *  one (DashboardSummary is a data class), so composables reading this state simply don't
     *  recompose at all when a poll comes back with unchanged data — a real no-op refresh. */
    fun refreshSilently() {
        viewModelScope.launch {
            when (val result = repository.getDashboardSummary()) {
                is ApiResult.Success -> _uiState.value = UiState.Success(result.data)
                is ApiResult.Failure -> Unit // keep showing the last good data on a background poll failure
            }
        }
    }
}