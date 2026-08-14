package com.odorik.odorikbuddy.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.util.ApiDates
import com.odorik.odorikbuddy.util.BackoffPolicy
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
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
    private val historyRepository: HistoryRepository,
    private val appPreferences: AppPreferences,
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

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying

    private val retryBackoff = BackoffPolicy()

    init {
        loadSavedDateRange()


        viewModelScope.launch {
            error.collect { currentError ->
                if (!currentError.isNullOrEmpty()) {
                    startErrorRetry()
                } else {
                    stopErrorRetry()
                }
            }
        }
    }

    fun startErrorRetry() {
        if (_isRetrying.value) return
        _isRetrying.value = true
        viewModelScope.launch {
            var attempt = 0
            while (_isRetrying.value && attempt < retryBackoff.maxAttempts) {
                attempt++
                kotlinx.coroutines.delay(retryBackoff.delayBeforeAttempt(attempt))
                if (!_isRetrying.value) break
                loadDataInternal(false)
                if (_error.value == null) break
            }
            _isRetrying.value = false
        }
    }

    fun stopErrorRetry() {
        _isRetrying.value = false
    }

    private fun getCurrentWeekRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = start.plusDays(6)
        return start to end
    }

    fun loadData(isInitialLoad: Boolean) {
        viewModelScope.launch { loadDataInternal(isInitialLoad) }
    }

    private suspend fun loadDataInternal(isInitialLoad: Boolean) {
        if (isInitialLoad) _isInitialLoading.value = true else _isRefreshing.value = true

        _error.value = null

        if (isInitialLoad) {
            _credit.value = UiState.Loading
        }

        try {

            if (isInitialLoad && appPreferences.getString("dashboard_start_date") == null) {
                val (start, end) = getCurrentWeekRange()
                _startDate.value = start
                _endDate.value = end
            }

            coroutineScope {
                val creditJob = launch { getCredit() }
                val spendingJob = launch { fetchSpendingData() }
                listOf(creditJob, spendingJob).joinAll()
            }

        } catch (e: Exception) {
            val localizedContext = localeManager.createLocaleContext(context)
            _error.value = ErrorMessageUtil.standardizeError(e, localizedContext)
        } finally {
            if (isInitialLoad) _isInitialLoading.value = false else _isRefreshing.value = false
        }
    }

    fun refresh() {
        loadData(false)
    }

    fun clearError() {
        _error.value = null
    }

    private fun loadSavedDateRange() {
        val startStr = appPreferences.getString("dashboard_start_date")
        val endStr = appPreferences.getString("dashboard_end_date")
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
        appPreferences.saveString("dashboard_start_date", start.toEpochDay().toString())
        appPreferences.saveString("dashboard_end_date", end.toEpochDay().toString())
    }

    fun updateDateRange(newStartDate: java.time.LocalDate, newEndDate: java.time.LocalDate) {
        val (currentStart, currentEnd) = getCurrentWeekRange()


        if (newStartDate == currentStart && newEndDate == currentEnd) {
            appPreferences.clearString("dashboard_start_date")
            appPreferences.clearString("dashboard_end_date")
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
        appPreferences.clearString("dashboard_start_date")
        appPreferences.clearString("dashboard_end_date")

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
            val errorMessage = ErrorMessageUtil.standardizeError(it, localizedContext)
            _credit.value = UiState.Error(errorMessage)
            _error.value = errorMessage
        }
    }

    private suspend fun fetchSpendingData() {
        val to = ApiDates.formatUtc(_endDate.value.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant())
        val from = ApiDates.formatUtc(_startDate.value.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())

        try {
            val history = historyRepository.getCombinedHistory(from, to)
            historyRepository.insertHistory(history)
            calculateTodaysSpending(history)
            calculateSelectedPeriodSpending(history)
            calculateChartSpending(history)
            _error.value = null
        } catch (e: Exception) {
            val localizedContext = localeManager.createLocaleContext(context)
            val errorMessage = ErrorMessageUtil.standardizeError(e, localizedContext)
            _error.value = errorMessage

            try {
                val cachedHistory = historyRepository.getCachedHistory()


                val filteredCache = cachedHistory.filter { item ->
                    try {
                        val itemDate = parseIsoDate(item.date).toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        !itemDate.isBefore(_startDate.value) && !itemDate.isAfter(_endDate.value)
                    } catch (ex: Exception) {
                        false
                    }
                }

                if (filteredCache.isNotEmpty()) {
                    calculateTodaysSpending(filteredCache)
                    calculateSelectedPeriodSpending(filteredCache)
                    calculateChartSpending(filteredCache)
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
        return ApiDates.parse(isoDate)?.let { Date.from(it) } ?: Date(0)
    }
}