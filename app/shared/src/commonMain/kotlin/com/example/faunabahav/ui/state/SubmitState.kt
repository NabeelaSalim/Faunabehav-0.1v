package com.example.faunabahav.ui.state

sealed interface SubmitState<out T> {
    data object Idle : SubmitState<Nothing>
    data object Submitting : SubmitState<Nothing>
    data class Success<T>(val data: T) : SubmitState<T>
    data class Error(val message: String) : SubmitState<Nothing>
}