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
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.ThemeManager // Import ThemeManager
import androidx.compose.runtime.State // Import State for isDarkMode

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLinesUseCase: GetLinesUseCase,
    private val getLineInfoUseCase: GetLineInfoUseCase,
    private val userRepository: UserRepository,
    private val themeManager: ThemeManager // Inject ThemeManager
) : ViewModel() {

    val isDarkMode: State<Boolean> = themeManager.isDarkMode // Expose isDarkMode from ThemeManager

    fun setDarkMode(enabled: Boolean) {
        themeManager.setDarkMode(enabled) // Expose setDarkMode from ThemeManager
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