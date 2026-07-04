package com.example.faunabahav.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.AnalyticsRepository
import com.example.faunabahav.model.AnalyticsSummary
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<AnalyticsSummary>>(UiState.Loading)
    val uiState: StateFlow<UiState<AnalyticsSummary>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getAnalytics()) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Failure -> UiState.Error(result.error.toUiMessage())
            }
        }
    }
}