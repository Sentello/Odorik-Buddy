package com.odorik.odorikbuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.domain.usecase.AuthenticationException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getCreditUseCase: GetCreditUseCase
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    fun onLoginClick(userId: String, password: String, remember: Boolean) {
        _loginUiState.value = LoginUiState.Loading // Set loading state

        viewModelScope.launch {
            userRepository.saveCredentials(userId, password, remember) // Save credentials first

            val result = getCreditUseCase.execute() // Attempt to get credit (which includes authentication)

            result.onSuccess {
                _loginUiState.value = LoginUiState.Success // Login successful
            }.onFailure { e ->
                if (e is AuthenticationException) {
                    _loginUiState.value = LoginUiState.Error("Invalid credentials")
                } else {
                    _loginUiState.value = LoginUiState.Error(e.message ?: "An unknown error occurred")
                }
            }
        }
    }
}