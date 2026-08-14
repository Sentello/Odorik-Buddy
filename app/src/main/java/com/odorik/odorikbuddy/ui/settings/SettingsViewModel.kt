package com.odorik.odorikbuddy.ui.settings


import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.AppTheme
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.ThemeMode
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLinesUseCase: GetLinesUseCase,
    private val userRepository: UserRepository,
    private val themeManager: ThemeManager,
    private val localeManager: LocaleManager,
    private val appPreferences: AppPreferences,
    private val updateWorkManager: UpdateWorkManager
) : ViewModel() {

    val themeMode: State<ThemeMode> = themeManager.themeMode
    val appTheme: State<AppTheme> = themeManager.appTheme

    private val _language = MutableStateFlow(localeManager.getPreferredLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines.asStateFlow()

    private val _selectedLine = MutableStateFlow<Line?>(null)
    val selectedLine: StateFlow<Line?> = _selectedLine.asStateFlow()

    private val _logoutEvent = Channel<Unit>(Channel.CONFLATED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    private val _historyPeriod = MutableStateFlow(getHistoryPeriod())
    val historyPeriod: StateFlow<Int> = _historyPeriod.asStateFlow()

    private val _phoneNumber = MutableStateFlow(getPhoneNumber())
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _autoUpdateEnabled = MutableStateFlow(appPreferences.autoUpdateEnabled)
    val autoUpdateEnabled: StateFlow<Boolean> = _autoUpdateEnabled.asStateFlow()

    private val _directCallsEnabled = MutableStateFlow(appPreferences.directCallsEnabled)
    val directCallsEnabled: StateFlow<Boolean> = _directCallsEnabled.asStateFlow()

    init {
        getLines()
    }

    private fun getHistoryPeriod(): Int = appPreferences.historyPeriodDays

    private fun getPhoneNumber(): String {
        return appPreferences.getString("phone_number", "") ?: ""
    }

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun setAppTheme(theme: AppTheme) {
        themeManager.setAppTheme(theme)
    }

    fun setLanguage(lang: String) {
        localeManager.setPreferredLanguage(lang)
        _language.value = lang
    }

    fun setHistoryPeriod(days: Int) {
        appPreferences.historyPeriodDays = days
        _historyPeriod.value = days
    }

    fun setPhoneNumber(number: String) {
        val formattedNumber = if (number.startsWith("+")) {
            number.replace("+", "00")
        } else {
            number
        }
        appPreferences.saveString("phone_number", formattedNumber)
        _phoneNumber.value = formattedNumber
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        updateWorkManager.setAutoUpdateEnabled(enabled)
        _autoUpdateEnabled.value = appPreferences.autoUpdateEnabled
    }

    fun setDirectCallsEnabled(enabled: Boolean) {
        appPreferences.directCallsEnabled = enabled
        _directCallsEnabled.value = appPreferences.directCallsEnabled
    }

    fun performImmediateUpdateCheck() {
        updateWorkManager.performImmediateUpdateCheck()
    }

    fun getLines() {
        viewModelScope.launch {
            val result = getLinesUseCase.execute()
            result.onSuccess {
                _lines.value = it
            }
        }
    }

    fun onLineSelected(line: Line) {
        _selectedLine.value = line
    }

    fun onDismissLineDialog() {
        _selectedLine.value = null
    }

    fun logout() {
        userRepository.clearCredentials()
        viewModelScope.launch { _logoutEvent.send(Unit) }
    }
}
