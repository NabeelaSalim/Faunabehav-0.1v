package com.example.faunabahav.ui.screens.inference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.model.InferenceResult
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InferenceViewModel(
    private val repository: ObservationRepository,
) : ViewModel() {
    private val _submitState = MutableStateFlow<SubmitState<InferenceResult>>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState<InferenceResult>> = _submitState.asStateFlow()

    fun submitObservation(
        deviceId: Int,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            _submitState.value = when (
                val result = repository.submitObservation(deviceId, fileBytes, fileName, contentType)
            ) {
                is ApiResult.Success -> SubmitState.Success(result.data)
                is ApiResult.Failure -> SubmitState.Error(result.error.toUiMessage())
            }
        }
    }

    fun reset() {
        _submitState.value = SubmitState.Idle
    }
}