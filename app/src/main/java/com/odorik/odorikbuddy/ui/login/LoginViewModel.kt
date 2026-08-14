package com.odorik.odorikbuddy.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.repository.AuthenticationException
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    private val getCreditUseCase: GetCreditUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    fun onLoginClick(userId: String, password: String, remember: Boolean) {
        if (userId.isBlank() || password.isBlank()) {
            _loginUiState.value = LoginUiState.Error(context.getString(R.string.user_or_password_not_set))
            return
        }

        _loginUiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {

                userRepository.setSessionCredentials(userId, password)

                val result = getCreditUseCase.execute()

                result.onSuccess {
                    if (remember) {
                        userRepository.persistCredentials(userId, password)
                    } else {

                        userRepository.clearPersistedCredentials()
                    }
                    _loginUiState.value = LoginUiState.Success
                }.onFailure { e ->

                    userRepository.clearCredentials()
                    _loginUiState.value = when (e) {
                        is AuthenticationException ->
                            LoginUiState.Error(context.getString(R.string.invalid_credentials))
                        else ->
                            LoginUiState.Error(e.message ?: context.getString(R.string.unknown_error))
                    }
                }
            } catch (e: Exception) {
                userRepository.clearCredentials()
                _loginUiState.value = LoginUiState.Error(
                    e.message ?: context.getString(R.string.unknown_error)
                )
            }
        }
    }

    fun onUserIdChanged() {
        if (_loginUiState.value is LoginUiState.Error) {
            _loginUiState.value = LoginUiState.Idle
        }
    }

    fun onPasswordChanged() {
        if (_loginUiState.value is LoginUiState.Error) {
            _loginUiState.value = LoginUiState.Idle
        }
    }
}
