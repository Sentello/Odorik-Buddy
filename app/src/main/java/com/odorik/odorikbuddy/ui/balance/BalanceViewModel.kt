package com.odorik.odorikbuddy.ui.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.odorik.odorikbuddy.data.local.SecureStorage // Assuming this path

@HiltViewModel
class BalanceViewModel @Inject constructor(
    private val odorikApi: OdorikApi,
    private val secureStorage: SecureStorage // Inject SecureStorage
) : ViewModel() {
    private val _balance = MutableStateFlow<String?>(null)
    val balance: StateFlow<String?> = _balance

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchBalance() = viewModelScope.launch {
        try {
            val user = secureStorage.getSecureUser()
            val password = secureStorage.getSecurePassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "Authentication credentials not set."
                return@launch
            }

            val balanceResult = odorikApi.getCredit(user, password)
            if (balanceResult.startsWith("error")) {
                _error.value = balanceResult
            } else {
                _balance.value = balanceResult
            }
        } catch (e: Exception) {
            _error.value = "Network error: ${e.message}"
        }
    }
}
