package com.odorik.odorikbuddy.ui.settings


import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.backup.ImportOptions
import com.odorik.odorikbuddy.data.backup.ImportReport
import com.odorik.odorikbuddy.data.backup.ReadResult
import com.odorik.odorikbuddy.data.backup.SettingsBackupRepository
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.AppTheme
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.NavIconStyle
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.ThemeMode
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.ui.calls.TileSize
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
    private val updateWorkManager: UpdateWorkManager,
    private val settingsBackupRepository: SettingsBackupRepository,
    private val simRepository: com.odorik.odorikbuddy.data.repository.SimRepository
) : ViewModel() {

    /** One-shot results of export / import, shown as a snackbar or toast. */
    sealed interface BackupUiEvent {
        data class Exported(val uri: Uri, val tileCount: Int) : BackupUiEvent
        data object ExportFailed : BackupUiEvent
        data class ImportFailed(@StringRes val messageRes: Int) : BackupUiEvent
        data class Imported(val report: ImportReport) : BackupUiEvent
    }

    val themeMode: State<ThemeMode> = themeManager.themeMode
    val appTheme: State<AppTheme> = themeManager.appTheme
    val navIconStyle: State<NavIconStyle> = themeManager.navIconStyle

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

    private val _tileSize = MutableStateFlow(
        TileSize.fromKey(appPreferences.getString(AppPreferences.KEY_TILE_SIZE))
    )
    val tileSize: StateFlow<TileSize> = _tileSize.asStateFlow()

    /**
     * Phone numbers of the account's Odorik SIM cards. Empty = no SIM section at all (agreed
     * with the user: no "you have no SIM" message, nothing to hide). Cached so the section
     * doesn't flicker; refreshed on every Settings visit; an error keeps the cached value.
     */
    private val _simNumbers = MutableStateFlow(readCachedSimNumbers())
    val simNumbers: StateFlow<List<String>> = _simNumbers.asStateFlow()

    init {
        getLines()
        refreshSims()
    }

    private fun readCachedSimNumbers(): List<String> =
        appPreferences.getString(AppPreferences.KEY_SIM_NUMBERS).orEmpty()
            .split(',').map { it.trim() }.filter { it.isNotEmpty() }

    fun refreshSims() {
        viewModelScope.launch {
            simRepository.getSimCards().onSuccess { sims ->
                val numbers = sims.mapNotNull { it.phoneNumber?.takeIf(String::isNotBlank) }
                appPreferences.saveString(AppPreferences.KEY_SIM_NUMBERS, numbers.joinToString(","))
                _simNumbers.value = numbers
            }
        }
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

    fun setNavIconStyle(style: NavIconStyle) {
        themeManager.setNavIconStyle(style)
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

    fun setTileSize(size: TileSize) {
        appPreferences.saveString(AppPreferences.KEY_TILE_SIZE, size.name)
        _tileSize.value = size
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

    // --- Settings backup ---

    private val _backupEvents = Channel<BackupUiEvent>(Channel.BUFFERED)
    val backupEvents = _backupEvents.receiveAsFlow()

    /** A read and validated backup waiting for the user's confirmation. */
    private val _pendingImport = MutableStateFlow<ReadResult.Ok?>(null)
    val pendingImport: StateFlow<ReadResult.Ok?> = _pendingImport.asStateFlow()

    private val _backupBusy = MutableStateFlow(false)
    val backupBusy: StateFlow<Boolean> = _backupBusy.asStateFlow()

    fun exportBackup(uri: Uri) = runBackupJob {
        settingsBackupRepository.writeBackup(uri)
            .onSuccess { _backupEvents.send(BackupUiEvent.Exported(uri, it)) }
            .onFailure { _backupEvents.send(BackupUiEvent.ExportFailed) }
    }

    fun loadImport(uri: Uri) = runBackupJob {
        when (val result = settingsBackupRepository.readBackup(uri)) {
            is ReadResult.Ok -> _pendingImport.value = result
            ReadResult.NewerVersion ->
                _backupEvents.send(BackupUiEvent.ImportFailed(R.string.backup_error_newer_version))
            ReadResult.ReadFailed ->
                _backupEvents.send(BackupUiEvent.ImportFailed(R.string.backup_error_read_failed))
            ReadResult.NotABackup, ReadResult.TooLarge ->
                _backupEvents.send(BackupUiEvent.ImportFailed(R.string.backup_error_not_a_backup))
        }
    }

    fun confirmImport(options: ImportOptions) {
        val pending = _pendingImport.value ?: return
        _pendingImport.value = null
        runBackupJob {
            val report = settingsBackupRepository.applyBackup(pending.backup, options)
            refreshFromPreferences()
            _backupEvents.send(BackupUiEvent.Imported(report))
        }
    }

    fun cancelImport() {
        _pendingImport.value = null
    }

    private fun runBackupJob(block: suspend () -> Unit) {
        if (_backupBusy.value) return
        _backupBusy.value = true
        viewModelScope.launch {
            try {
                block()
            } finally {
                _backupBusy.value = false
            }
        }
    }

    /** Re-reads every value this screen caches, after an import changed them underneath. */
    private fun refreshFromPreferences() {
        _historyPeriod.value = getHistoryPeriod()
        _phoneNumber.value = getPhoneNumber()
        _autoUpdateEnabled.value = appPreferences.autoUpdateEnabled
        _directCallsEnabled.value = appPreferences.directCallsEnabled
        _tileSize.value = TileSize.fromKey(appPreferences.getString(AppPreferences.KEY_TILE_SIZE))
        _language.value = localeManager.getPreferredLanguage()
    }

    fun logout() {
        appPreferences.clearString(AppPreferences.KEY_SIM_NUMBERS)
        userRepository.clearCredentials()
        viewModelScope.launch { _logoutEvent.send(Unit) }
    }
}
