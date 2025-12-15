package com.odorik.odorikbuddy.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.UserInfo
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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

    data class ChartDay(val date: String, val spending: Double)

    private val _spendingChartData = MutableStateFlow<List<ChartDay>>(emptyList())
    val spendingChartData: StateFlow<List<ChartDay>> = _spendingChartData

    private val _spendingChartAverage = MutableStateFlow(0.0)
    val spendingChartAverage: StateFlow<Double> = _spendingChartAverage

    private val _startDate = MutableStateFlow(java.time.LocalDate.now().minusDays(6))
    val startDate: StateFlow<java.time.LocalDate> = _startDate

    private val _endDate = MutableStateFlow(java.time.LocalDate.now())
    val endDate: StateFlow<java.time.LocalDate> = _endDate

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    fun loadData(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) _isInitialLoading.value = true else _isRefreshing.value = true

            _error.value = null

            if (isInitialLoad) {
                _credit.value = UiState.Loading
                _userInfo.value = UiState.Loading
            }

            try {
                val creditJob = launch { getCredit() }
                val userInfoJob = launch { getUserInfo() }
                val spendingJob = launch { fetchSpendingData() }

                listOf(creditJob, userInfoJob, spendingJob).joinAll()

            } catch (e: Exception) {
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

    fun clearError() {
        _error.value = null
    }

    fun updateDateRange(newStartDate: java.time.LocalDate, newEndDate: java.time.LocalDate) {
        _startDate.value = newStartDate
        _endDate.value = newEndDate
        viewModelScope.launch {
            fetchSpendingData()
        }
    }

    fun resetDateRange() {
        _startDate.value = java.time.LocalDate.now().minusDays(6)
        _endDate.value = java.time.LocalDate.now()
        viewModelScope.launch {
            fetchSpendingData()
        }
    }

    private suspend fun getCredit() {
        val result = getCreditUseCase.execute()
        result.onSuccess {
            _credit.value = UiState.Success(it)
        }.onFailure {
            val errorMessage = it.message ?: "Failed to load credit"
            _credit.value = UiState.Error(errorMessage)
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
        }
    }

    private suspend fun fetchSpendingData() {
        val user = securePreferences.getUser()
        val password = securePreferences.getPassword()

        if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
            Log.e("DashboardViewModel", "User or password is not set.")
            _error.value = context.getString(R.string.user_or_password_not_set)
            return
        }

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val to = isoFormat.format(Date.from(_endDate.value.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()))
        val from = isoFormat.format(Date.from(_startDate.value.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))

        try {
            Log.d("DashboardViewModel", "Fetching history from $from to $to")
            val history = historyRepository.getCombinedHistory(user, password, from, to)
            Log.d("DashboardViewModel", "History size: ${history.size}")
            historyRepository.insertHistory(history)
            calculateTodaysSpending(history)
            calculateThisMonthsSpending(history)
            calculateChartSpending(history)
            _error.value = null
        } catch (e: Exception) {
            val errorMessage = e.message ?: context.getString(R.string.unknown_error)
            Log.e("DashboardViewModel", "Error fetching history: $errorMessage")
            _error.value = errorMessage

            try {
                val cachedHistory = historyRepository.getCachedHistory()
                Log.d("DashboardViewModel", "Using cached history, size: ${cachedHistory.size}")
                if (cachedHistory.isNotEmpty()) {
                    calculateTodaysSpending(cachedHistory)
                    calculateThisMonthsSpending(cachedHistory)
                    calculateChartSpending(cachedHistory)
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

    private fun calculateChartSpending(history: List<HistoryItem>) {
        val chartData = mutableListOf<ChartDay>()
        val start = _startDate.value
        val end = _endDate.value
        var currentDate = start
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

        while (!currentDate.isAfter(end)) {
            val calendar = Calendar.getInstance().apply {
                time = Date.from(currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
            }
            val spending = history.filter { isSameDay(it.date, calendar) }.sumOf { it.price }
            val dateStr = dateFormat.format(calendar.time)
            chartData.add(ChartDay(dateStr, spending))
            currentDate = currentDate.plusDays(1)
        }

        _spendingChartData.value = chartData
        val average = if (chartData.isNotEmpty()) chartData.map { it.spending }.average() else 0.0
        _spendingChartAverage.value = average
        Log.d("DashboardViewModel", "Chart spending: $chartData, average: $average")
        Log.d("ViewModelChartDebug", "Updated spendingChartData: size=${chartData.size}, startDate=${_startDate.value}, endDate=${_endDate.value}, isDefaultRange=${_startDate.value.toEpochDay() == java.time.LocalDate.now().minusDays(6).toEpochDay() && _endDate.value.toEpochDay() == java.time.LocalDate.now().toEpochDay()}")
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