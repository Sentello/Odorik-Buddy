package com.odorik.odorikbuddy.ui.settings

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLinesUseCase: GetLinesUseCase,
    private val userRepository: UserRepository,
    private val themeManager: ThemeManager,
    private val localeManager: LocaleManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val isDarkMode: State<Boolean> = themeManager.isDarkMode

    fun setDarkMode(enabled: Boolean) {
        themeManager.setDarkMode(enabled)
    }

    private val _language = MutableStateFlow(localeManager.getPreferredLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(lang: String) {
        localeManager.setPreferredLanguage(lang)
        _language.value = lang
    }

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    private val _selectedLine = MutableStateFlow<Line?>(null)
    val selectedLine: StateFlow<Line?> = _selectedLine.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent

    private val _historyPeriod = MutableStateFlow(getHistoryPeriod())
    val historyPeriod: StateFlow<Int> = _historyPeriod.asStateFlow()

    private fun getHistoryPeriod(): Int {
        return sharedPreferences.getInt("history_period_days", 90)
    }

    fun setHistoryPeriod(days: Int) {
        sharedPreferences.edit().putInt("history_period_days", days).apply()
        _historyPeriod.value = days
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
        _logoutEvent.value = true
    }
}