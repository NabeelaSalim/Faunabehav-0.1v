package com.example.faunabahav.ui.screens.observations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.Observation
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ObservationsViewModel(
    private val repository: ObservationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Observation>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Observation>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getObservations()) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Failure -> UiState.Error(result.error.toUiMessage())
            }
        }
    }
}