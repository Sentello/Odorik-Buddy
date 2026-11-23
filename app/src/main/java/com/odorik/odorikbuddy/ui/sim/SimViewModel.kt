package com.odorik.odorikbuddy.ui.sim

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.model.SimCard
import com.odorik.odorikbuddy.data.repository.SimRepository
import com.odorik.odorikbuddy.data.repository.bytesPerDay
import com.odorik.odorikbuddy.data.repository.bytesThisMonth
import com.odorik.odorikbuddy.data.repository.dataPackageLimitBytes
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class SimUiState(
    val isLoading: Boolean = true,
    val card: SimCard? = null,
    val loadError: String? = null,
    /** Bytes used this calendar month; null = usage couldn't be loaded. */
    val usedThisMonth: Long? = null,
    /** Package limit in bytes; null = unknown / pay per kB (no progress bar). */
    val limit: Long? = null,
    /** Last 10 days, oldest first. */
    val daily: List<Long> = emptyList(),
    /** API key of the switch being changed, or "fup"/"state" while those run. */
    val busy: String? = null
)

/**
 * One Odorik SIM. Written from the docs only — nobody on the team has an Odorik SIM — so every
 * change is followed by a fresh GET: the screen always shows Odorik's real state, never an
 * optimistic one.
 */
@HiltViewModel
class SimViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SimRepository,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val number: String = savedStateHandle.get<String>(SettingsRoutes.SIM_NUMBER_ARG).orEmpty()

    private val _uiState = MutableStateFlow(SimUiState())
    val uiState: StateFlow<SimUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.card == null, loadError = null) }
            repository.getSimCard(number)
                .onSuccess { card ->
                    _uiState.update {
                        it.copy(isLoading = false, card = card, limit = dataPackageLimitBytes(card.dataPackage))
                    }
                    loadUsage()
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, loadError = message(e)) } }
        }
    }

    private suspend fun loadUsage() {
        val zone = ZoneId.of("Europe/Prague")
        val today = LocalDate.now(zone)
        val from = minOf(today.withDayOfMonth(1), today.minusDays(9)).atStartOfDay(zone).toInstant()
        repository.getMobileData(number, from, Instant.now())
            .onSuccess { records ->
                _uiState.update {
                    it.copy(usedThisMonth = bytesThisMonth(records, today), daily = bytesPerDay(records, today))
                }
            }
            .onFailure { _uiState.update { it.copy(usedThisMonth = null, daily = emptyList()) } }
    }

    /** [key] is the API field: "mobile_data", "lte" or "missed_calls_register". */
    fun setSwitch(key: String, value: Boolean) =
        change(key, R.string.sim_change_saved) { repository.update(number, mapOf(key to value.toString())) }

    fun restartData() = change("fup", R.string.sim_fup_done) { repository.restartData(number) }

    fun setSuspended(suspended: Boolean) = change(
        "state",
        if (suspended) R.string.sim_suspended_done else R.string.sim_reactivated_done
    ) {
        repository.update(number, mapOf("state" to if (suspended) SimCard.STATE_SUSPENDED else SimCard.STATE_ACTIVE))
    }

    private fun change(busyKey: String, successRes: Int, block: suspend () -> Result<Unit>) {
        if (_uiState.value.busy != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = busyKey) }
            val result = block()
            result.onSuccess { _messages.send(context.getString(successRes)) }
                .onFailure { e -> _messages.send(message(e)) }
            // Always re-read: shows what Odorik really did (also after an unknown response).
            repository.getSimCard(number).onSuccess { card ->
                _uiState.update { it.copy(card = card, limit = dataPackageLimitBytes(card.dataPackage)) }
            }
            _uiState.update { it.copy(busy = null) }
        }
    }

    private fun message(e: Throwable): String = ErrorMessageUtil.standardizeError(e, context, localeManager)
}
