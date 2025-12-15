package com.odorik.odorikbuddy.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetLineInfoUseCase
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.model.LineInfo
import com.odorik.odorikbuddy.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.ThemeManager // Import ThemeManager
import com.odorik.odorikbuddy.data.local.LocaleManager
import androidx.compose.runtime.State // Import State for isDarkMode

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLinesUseCase: GetLinesUseCase,
    private val getLineInfoUseCase: GetLineInfoUseCase,
    private val userRepository: UserRepository,
    private val themeManager: ThemeManager, // Inject ThemeManager
    private val localeManager: LocaleManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val isDarkMode: State<Boolean> = themeManager.isDarkMode // Expose isDarkMode from ThemeManager

    fun setDarkMode(enabled: Boolean) {
        themeManager.setDarkMode(enabled) // Expose setDarkMode from ThemeManager
    }

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(lang: String) {
        localeManager.setPreferredLanguage(lang)
        _language.value = lang
    }

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    private val _lineInfo = MutableStateFlow<LineInfo?>(null)
    val lineInfo: StateFlow<LineInfo?> = _lineInfo

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent

    private val _historyPeriod = MutableStateFlow(getHistoryPeriod())
    val historyPeriod: StateFlow<Int> = _historyPeriod.asStateFlow()

    private fun getHistoryPeriod(): Int {
        return sharedPreferences.getInt("history_period_days", 90) // Default 90 days
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

    fun getLineInfo(lineId: String) {
        viewModelScope.launch {
            val result = getLineInfoUseCase.execute(lineId)
            result.onSuccess {
                _lineInfo.value = it
            }
        }
    }

    fun logout() {
        userRepository.clearCredentials()
        _logoutEvent.value = true
    }
}