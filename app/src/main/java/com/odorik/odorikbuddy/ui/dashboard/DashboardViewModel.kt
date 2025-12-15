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
import kotlinx.coroutines.joinAll
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

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _credit = MutableStateFlow<UiState<Double>>(UiState.Loading)
    val credit: StateFlow<UiState<Double>> = _credit

    private val _userInfo = MutableStateFlow<UiState<UserInfo>>(UiState.Loading)
    val userInfo: StateFlow<UiState<UserInfo>> = _userInfo

    private val _todaysSpending = MutableStateFlow(0.0)
    val todaysSpending: StateFlow<Double> = _todaysSpending

    private val _thisMonthsSpending = MutableStateFlow(0.0)
    val thisMonthsSpending: StateFlow<Double> = _thisMonthsSpending

    private val _weeklySpending = MutableStateFlow<List<Double>>(emptyList())
    val weeklySpending: StateFlow<List<Double>> = _weeklySpending

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    fun loadData(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) _isInitialLoading.value = true else _isRefreshing.value = true
            
            // Clear previous errors on every new load attempt
            _error.value = null

            // On initial load, reset states to Loading
            if (isInitialLoad) {
                _credit.value = UiState.Loading
                _userInfo.value = UiState.Loading
            }

            try {
                // We can run these in parallel for speed
                val creditJob = launch { getCredit() }
                val userInfoJob = launch { getUserInfo() }
                val spendingJob = launch { fetchSpendingData() }

                listOf(creditJob, userInfoJob, spendingJob).joinAll() // Wait for all to finish

            } catch (e: Exception) {
                // This is a fallback, but individual functions will handle their own errors
                _error.value = e.message ?: context.getString(R.string.unknown_error)
                Log.e("DashboardViewModel", "Error loading data", e)
            } finally {
                if (isInitialLoad) _isInitialLoading.value = false else _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        loadData(false)
    }

    // --- NEW FUNCTION ---
    fun clearError() {
        _error.value = null
    }

    private suspend fun getCredit() {
        val result = getCreditUseCase.execute()
        result.onSuccess {
            _credit.value = UiState.Success(it)
        }.onFailure {
            val errorMessage = it.message ?: "Failed to load credit"
            _credit.value = UiState.Error(errorMessage)
            // --- UNIFY ERROR REPORTING ---
            _error.value = errorMessage
        }
    }

    private suspend fun getUserInfo() {
        val result = getUserInfoUseCase.execute()
        result.onSuccess {
            _userInfo.value = UiState.Success(it)
        }.onFailure {
            _userInfo.value = UiState.Error(it.message ?: "Failed to load user info")
            Log.e("DashboardViewModel", "Error fetching user info", it)
            // Optionally set error, but keep separate from main error for specificity
        }
    }

    private suspend fun fetchSpendingData() {
        val user = securePreferences.getUser()
        val password = securePreferences.getPassword()
    
        if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
            android.util.Log.e("DashboardViewModel", "User or password is not set.")
            _error.value = context.getString(R.string.user_or_password_not_set)
            return
        }
    
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val now = Calendar.getInstance()
        val to = isoFormat.format(now.time)
        now.set(Calendar.DAY_OF_MONTH, 1) // Start of current month
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        val from = isoFormat.format(now.time)
    
        try {
            android.util.Log.d("DashboardViewModel", "Fetching history from $from to $to")
            val history = historyRepository.getCombinedHistory(user, password, from, to)
            android.util.Log.d("DashboardViewModel", "History size: ${history.size}")
            // Cache the fetched history
            historyRepository.insertHistory(history)
            calculateTodaysSpending(history)
            calculateThisMonthsSpending(history)
            calculateWeeklySpending(history)
            _error.value = null
        } catch (e: Exception) {
            val errorMessage = e.message ?: context.getString(R.string.unknown_error)
            Log.e("DashboardViewModel", "Error fetching history: $errorMessage")
            // --- UNIFY ERROR REPORTING ---
            _error.value = errorMessage
            
            // The fallback to cached data logic is still good, just make sure to update the error message
            try {
                val cachedHistory = historyRepository.getCachedHistory()
                Log.d("DashboardViewModel", "Using cached history, size: ${cachedHistory.size}")
                if (cachedHistory.isNotEmpty()) {
                    // ... calculate with cached data ...
                    calculateTodaysSpending(cachedHistory)
                    calculateThisMonthsSpending(cachedHistory)
                    calculateWeeklySpending(cachedHistory)
                    _error.value = "$errorMessage (using cached data)"
                } else {
                    _error.value = "$errorMessage (no cached data available)"
                }
            } catch (cacheError: Exception) {
                Log.e("DashboardViewModel", "Error loading cached history: ${cacheError.message}")
                _error.value = "$errorMessage (cache also unavailable)"
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