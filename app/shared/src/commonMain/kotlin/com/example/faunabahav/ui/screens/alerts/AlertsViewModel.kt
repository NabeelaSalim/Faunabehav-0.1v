package com.example.faunabahav.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.model.Alert
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: AlertRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Alert>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Alert>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getAlerts()) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Failure -> UiState.Error(result.error.toUiMessage())
            }
        }
    }
}