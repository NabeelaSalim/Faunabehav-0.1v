package com.example.faunabahav.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.AuthRepository
import com.example.faunabahav.model.User
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Deliberately constructed once at the top of App(), not per-screen: its isAuthenticated state
 * gates whether LoginScreen or the main navigation shell renders at all, and is also read from
 * the logout affordances nested inside the sidebar/Settings.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)

    /** null while the startup session check hasn't resolved yet. */
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState<User>>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState<User>> = _submitState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)

    /** The signed-in user, kept in sync with login/sign-up/logout so screens like Settings and
     * the sidebar profile card can show real session data instead of a placeholder. */
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            val session = authRepository.currentSession()
            _isAuthenticated.value = session != null
            _currentUser.value = session?.user
        }
    }

    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            when (val result = authRepository.login(email, password, rememberMe)) {
                is ApiResult.Success -> {
                    _submitState.value = SubmitState.Success(result.data)
                    _currentUser.value = result.data
                    _isAuthenticated.value = true
                }

                is ApiResult.Failure -> _submitState.value = SubmitState.Error(result.error.toUiMessage())
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            when (val result = authRepository.signUp(email, password, displayName, rememberMe)) {
                is ApiResult.Success -> {
                    _submitState.value = SubmitState.Success(result.data)
                    _currentUser.value = result.data
                    _isAuthenticated.value = true
                }

                is ApiResult.Failure -> _submitState.value = SubmitState.Error(result.error.toUiMessage())
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _submitState.value = SubmitState.Idle
            _currentUser.value = null
            _isAuthenticated.value = false
        }
    }
}
