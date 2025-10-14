package com.odorik.odorikbuddy.ui.settings

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
import com.odorik.odorikbuddy.data.local.ThemeManager 
import com.odorik.odorikbuddy.data.local.LocaleManager
import androidx.compose.runtime.State 

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLinesUseCase: GetLinesUseCase,
    private val getLineInfoUseCase: GetLineInfoUseCase,
    private val userRepository: UserRepository,
    private val themeManager: ThemeManager, 
    private val localeManager: LocaleManager
) : ViewModel() {

    val isDarkMode: State<Boolean> = themeManager.isDarkMode 

    fun setDarkMode(enabled: Boolean) {
        themeManager.setDarkMode(enabled) 
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