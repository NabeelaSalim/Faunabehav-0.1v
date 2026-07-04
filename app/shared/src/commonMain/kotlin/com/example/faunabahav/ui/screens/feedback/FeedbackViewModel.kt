package com.example.faunabahav.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.FeedbackRepository
import com.example.faunabahav.model.Feedback
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val repository: FeedbackRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Feedback>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Feedback>>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState<Feedback>>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState<Feedback>> = _submitState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = repository.getFeedback()) {
                is ApiResult.Success -> UiState.Success(result.data)
                is ApiResult.Failure -> UiState.Error(result.error.toUiMessage())
            }
        }
    }

    fun submitFeedback(eventId: Int, userId: Int, correctedBehaviour: String) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            val feedback = Feedback(eventId, userId, correctedBehaviour)
            when (val result = repository.submitFeedback(feedback)) {
                is ApiResult.Success -> {
                    _submitState.value = SubmitState.Success(result.data)
                    load()
                }

                is ApiResult.Failure -> _submitState.value = SubmitState.Error(result.error.toUiMessage())
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }
}