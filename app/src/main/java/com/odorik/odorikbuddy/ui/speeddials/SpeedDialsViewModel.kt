package com.odorik.odorikbuddy.ui.speeddials

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.model.SpeedDial
import com.odorik.odorikbuddy.data.repository.SpeedDialRepository
import com.odorik.odorikbuddy.data.repository.freeShortcuts
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import com.odorik.odorikbuddy.util.OdorikApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SpeedDialsUiState(
    val isLoading: Boolean = true,
    val items: List<SpeedDial> = emptyList(),
    /** Load error → full-screen error state with Retry. */
    val loadError: String? = null,
    /** Set after a write returned `unauthorized` (line login): view only. */
    val readOnly: Boolean = false,
    val isSaving: Boolean = false,
    /** Error of the last save/delete, shown inside the open sheet. */
    val sheetError: String? = null,
    /** Every line with the speed dials it sees (account + own). */
    val lineSpeedDials: List<Pair<com.odorik.odorikbuddy.data.model.Line, List<SpeedDial>>> = emptyList()
) {
    val freeShortcuts: List<Int> get() = freeShortcuts(items.map { it.shortcut })
}

sealed interface SpeedDialsEvent {
    object Saved : SpeedDialsEvent
    object Deleted : SpeedDialsEvent
    /** The sheet should close (after success or after switching to read-only). */
    object CloseSheet : SpeedDialsEvent
}

@HiltViewModel
class SpeedDialsViewModel @Inject constructor(
    private val repository: SpeedDialRepository,
    private val accountRepository: com.odorik.odorikbuddy.data.repository.AccountRepository,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedDialsUiState())
    val uiState: StateFlow<SpeedDialsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SpeedDialsEvent>(Channel.BUFFERED)
    val events: Flow<SpeedDialsEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            repository.getSpeedDials()
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, loadError = message(e)) } }
            loadLineSpeedDials()
        }
    }

    /** Each line's own speed dials (read-only: Odorik's API can't create or change them). */
    private suspend fun loadLineSpeedDials() {
        val lines = accountRepository.getLines().getOrNull().orEmpty()
        val perLine = lines.map { line ->
            line to repository.getLineSpeedDials(line.id).getOrNull().orEmpty()
        }
        _uiState.update { it.copy(lineSpeedDials = perLine) }
    }

    fun clearSheetError() = _uiState.update { it.copy(sheetError = null) }

    /**
     * [original] == null → create, else update with only the changed fields.
     * [number] is sent as typed (trimmed): Odorik accepts `*100`, `skype:…` and similar.
     */
    fun save(original: SpeedDial?, shortcut: Int?, name: String, number: String) {
        if (_uiState.value.isSaving) return
        val trimmedName = name.trim()
        val trimmedNumber = number.trim()
        if (trimmedName.isEmpty() || trimmedNumber.isEmpty()) return
        write(SpeedDialsEvent.Saved) {
            if (original == null) {
                repository.create(trimmedNumber, trimmedName, shortcut)
            } else {
                repository.update(
                    original = original.shortcut,
                    newShortcut = shortcut?.takeIf { it != original.shortcut },
                    number = trimmedNumber.takeIf { it != original.number },
                    name = trimmedName.takeIf { it != original.name.orEmpty() }
                )
            }
        }
    }

    fun delete(item: SpeedDial) {
        if (_uiState.value.isSaving) return
        write(SpeedDialsEvent.Deleted) { repository.delete(item.shortcut) }
    }

    /** Runs a write: success → [successEvent], close sheet, reload; `unauthorized` → read-only. */
    private fun write(successEvent: SpeedDialsEvent, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, sheetError = null) }
            val result = block()
            _uiState.update { it.copy(isSaving = false) }
            result.onSuccess {
                // Close first: the screen shows the snackbar for the success event, which
                // suspends its collector for a few seconds.
                _events.send(SpeedDialsEvent.CloseSheet)
                _events.send(successEvent)
                load()
            }.onFailure { e ->
                if (e is OdorikApiException && e.code == "unauthorized") {
                    _uiState.update { it.copy(readOnly = true) }
                    _events.send(SpeedDialsEvent.CloseSheet)
                } else {
                    _uiState.update { it.copy(sheetError = message(e)) }
                }
            }
        }
    }

    suspend fun phoneNumbersOf(contentResolver: ContentResolver, uri: Uri): List<String> =
        withContext(Dispatchers.IO) { getPhoneNumbersForContactUseCase(contentResolver, uri) }

    private fun message(e: Throwable): String =
        ErrorMessageUtil.standardizeError(e, context, localeManager)
}
