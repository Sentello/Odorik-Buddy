package com.odorik.odorikbuddy.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.UserInfo
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
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
    private val localeManager: LocaleManager,
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

    private val _selectedPeriodSpending = MutableStateFlow(0.0)
    val selectedPeriodSpending: StateFlow<Double> = _selectedPeriodSpending

    data class ChartDay(val date: String, val spending: Double)

    private val _spendingChartData = MutableStateFlow<List<ChartDay>>(emptyList())
    val spendingChartData: StateFlow<List<ChartDay>> = _spendingChartData

    private val _spendingChartAverage = MutableStateFlow(0.0)
    val spendingChartAverage: StateFlow<Double> = _spendingChartAverage

    private val _startDate = MutableStateFlow(getCurrentWeekRange().first)
    val startDate: StateFlow<LocalDate> = _startDate

    private val _endDate = MutableStateFlow(getCurrentWeekRange().second)
    val endDate: StateFlow<LocalDate> = _endDate

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    init {
        loadSavedDateRange()
    }

    private fun getCurrentWeekRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = start.plusDays(6)
        return start to end
    }

    fun loadData(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) _isInitialLoading.value = true else _isRefreshing.value = true

            _error.value = null

            if (isInitialLoad) {
                _credit.value = UiState.Loading
                _userInfo.value = UiState.Loading
            }

            try {

                if (isInitialLoad && securePreferences.getString("dashboard_start_date") == null) {
                    val (start, end) = getCurrentWeekRange()
                    _startDate.value = start
                    _endDate.value = end
                }

                val creditJob = launch { getCredit() }
                val userInfoJob = launch { getUserInfo() }
                val spendingJob = launch { fetchSpendingData() }

                listOf(creditJob, userInfoJob, spendingJob).joinAll()

            } catch (e: Exception) {
                val localizedContext = localeManager.createLocaleContext(context)
                _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
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

    private fun loadSavedDateRange() {
        val startStr = securePreferences.getString("dashboard_start_date")
        val endStr = securePreferences.getString("dashboard_end_date")
        if (startStr != null && endStr != null) {
            try {
                val startEpoch = startStr.toLong()
                val endEpoch = endStr.toLong()
                _startDate.value = java.time.LocalDate.ofEpochDay(startEpoch)
                _endDate.value = java.time.LocalDate.ofEpochDay(endEpoch)
            } catch (e: Exception) {

                val (start, end) = getCurrentWeekRange()
                _startDate.value = start
                _endDate.value = end
            }
        } else {

            val (start, end) = getCurrentWeekRange()
            _startDate.value = start
            _endDate.value = end
        }
    }

    private fun saveDateRange(start: java.time.LocalDate, end: java.time.LocalDate) {
        securePreferences.saveString("dashboard_start_date", start.toEpochDay().toString())
        securePreferences.saveString("dashboard_end_date", end.toEpochDay().toString())
    }

    fun updateDateRange(newStartDate: java.time.LocalDate, newEndDate: java.time.LocalDate) {
        val (currentStart, currentEnd) = getCurrentWeekRange()


        if (newStartDate == currentStart && newEndDate == currentEnd) {
            securePreferences.clearString("dashboard_start_date")
            securePreferences.clearString("dashboard_end_date")
        } else {
            saveDateRange(newStartDate, newEndDate)
        }

        _startDate.value = newStartDate
        _endDate.value = newEndDate

        viewModelScope.launch {
            fetchSpendingData()
        }
    }

    fun resetDateRange() {
        val (start, end) = getCurrentWeekRange()
        _startDate.value = start
        _endDate.value = end
        securePreferences.clearString("dashboard_start_date")
        securePreferences.clearString("dashboard_end_date")

        viewModelScope.launch {
            fetchSpendingData()
        }
    }

    private suspend fun getCredit() {
        val result = getCreditUseCase.execute()
        result.onSuccess {
            _credit.value = UiState.Success(it)
        }.onFailure {
            val localizedContext = localeManager.createLocaleContext(context)
            val errorMessage = ErrorMessageUtil.standardizeError(it.message ?: "Failed to load credit", localizedContext)
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
        }
    }

    private suspend fun fetchSpendingData() {
        val user = securePreferences.getUser()
        val password = securePreferences.getPassword()

        if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
            _error.value = context.getString(R.string.user_or_password_not_set)
            return
        }

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val to = isoFormat.format(Date.from(_endDate.value.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()))
        val from = isoFormat.format(Date.from(_startDate.value.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))

        try {
            val history = historyRepository.getCombinedHistory(user, password, from, to)
            historyRepository.insertHistory(history)
            calculateTodaysSpending(history)
            calculateSelectedPeriodSpending(history)
            calculateChartSpending(history)
            _error.value = null
        } catch (e: Exception) {
            val localizedContext = localeManager.createLocaleContext(context)
            val errorMessage = ErrorMessageUtil.standardizeError(e.message, localizedContext)
            _error.value = errorMessage

            try {
                val cachedHistory = historyRepository.getCachedHistory()
                if (cachedHistory.isNotEmpty()) {
                    calculateTodaysSpending(cachedHistory)
                    calculateSelectedPeriodSpending(cachedHistory)
                    calculateChartSpending(cachedHistory)
                    _error.value = "$errorMessage (using cached data)"
                } else {
                    _error.value = "$errorMessage (no cached data available)"
                }
            } catch (cacheError: Exception) {
                _error.value = "$errorMessage (cache also unavailable)"
            }
        }
    }

    private fun calculateTodaysSpending(history: List<HistoryItem>) {
        val today = Calendar.getInstance()
        _todaysSpending.value = history.filter { isSameDay(it.date, today) }.sumOf { it.price }
    }

    private fun calculateSelectedPeriodSpending(history: List<HistoryItem>) {
        _selectedPeriodSpending.value = history.sumOf { it.price }
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