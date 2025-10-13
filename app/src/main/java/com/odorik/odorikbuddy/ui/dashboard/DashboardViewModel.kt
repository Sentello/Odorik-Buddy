package com.odorik.odorikbuddy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase
import com.odorik.odorikbuddy.data.model.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCreditUseCase: GetCreditUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    private val _credit = MutableStateFlow<Double?>(null)
    val credit: StateFlow<Double?> = _credit

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    fun loadData() {
        getCredit()
        getUserInfo()
    }

    private fun getCredit() {
        viewModelScope.launch {
            val result = getCreditUseCase.execute()
            result.onSuccess {
                _credit.value = it
            }.onFailure {
                _credit.value = null
            }
        }
    }

    private fun getUserInfo() {

    }
}