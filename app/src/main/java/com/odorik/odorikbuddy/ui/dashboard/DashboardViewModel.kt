package com.odorik.odorikbuddy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase
import com.odorik.odorikbuddy.data.model.UserInfo
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import android.util.Log
import android.content.Context
import com.odorik.odorikbuddy.R

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCreditUseCase: GetCreditUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val historyRepository: HistoryRepository,
    private val securePreferences: SecurePreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _credit = MutableStateFlow<Double?>(null)
    val credit: StateFlow<Double?> = _credit

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _todaysSpending = MutableStateFlow(0.0)
    val todaysSpending: StateFlow<Double> = _todaysSpending

    private val _thisMonthsSpending = MutableStateFlow(0.0)
    val thisMonthsSpending: StateFlow<Double> = _thisMonthsSpending

    private val _weeklySpending = MutableStateFlow<List<Double>>(emptyList())
    val weeklySpending: StateFlow<List<Double>> = _weeklySpending

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadData() {
        getCredit()
        getUserInfo()
        fetchSpendingData()
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

    private fun fetchSpendingData() {
        viewModelScope.launch {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                Log.e("DashboardViewModel", "User or password is not set.")
                _error.value = context.getString(R.string.user_or_password_not_set)
                return@launch
            }

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val now = Calendar.getInstance()
            val to = isoFormat.format(now.time)
            now.add(Calendar.DAY_OF_YEAR, -30)
            val from = isoFormat.format(now.time)

            try {
                Log.d("DashboardViewModel", "Fetching history from $from to $to")
                val history = historyRepository.getCombinedHistory(user, password, from, to)
                Log.d("DashboardViewModel", "History size: ${history.size}")
                calculateTodaysSpending(history)
                calculateThisMonthsSpending(history)
                calculateWeeklySpending(history)
                _error.value = null
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching history: ${e.message}")
                _error.value = e.message
            }
        }
    }

    private fun calculateTodaysSpending(history: List<HistoryItem>) {
        val today = Calendar.getInstance()
        _todaysSpending.value = history.filter { isSameDay(it.date, today) }.sumOf { it.price }
        Log.d("DashboardViewModel", "Today's spending: ${_todaysSpending.value}")
    }

    private fun calculateThisMonthsSpending(history: List<HistoryItem>) {
        val today = Calendar.getInstance()
        _thisMonthsSpending.value = history.filter { isSameMonth(it.date, today) }.sumOf { it.price }
        Log.d("DashboardViewModel", "This month's spending: ${_thisMonthsSpending.value}")
    }

    private fun calculateWeeklySpending(history: List<HistoryItem>) {
        val weeklyData = mutableListOf<Double>()
        val today = Calendar.getInstance()
        for (i in 6 downTo 0) {
            val day = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val spending = history.filter { isSameDay(it.date, day) }.sumOf { it.price }
            weeklyData.add(spending)
        }
        _weeklySpending.value = weeklyData
        Log.d("DashboardViewModel", "Weekly spending: $weeklyData")
    }

    private fun isSameDay(isoDate: String, calendar: Calendar): Boolean {
        val itemDate = Calendar.getInstance().apply { time = parseIsoDate(isoDate) }
        return itemDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
               itemDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameMonth(isoDate: String, calendar: Calendar): Boolean {
        val itemDate = Calendar.getInstance().apply { time = parseIsoDate(isoDate) }
        return itemDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
               itemDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
    }

    private fun parseIsoDate(isoDate: String): Date {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(isoDate) ?: Date(0)
    }
}