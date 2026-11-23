package com.odorik.odorikbuddy.ui.livecalls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.ActiveCall
import com.odorik.odorikbuddy.data.model.collapseLegs
import com.odorik.odorikbuddy.data.repository.ActiveCallsRepository
import com.odorik.odorikbuddy.data.repository.RoutingRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.ui.livecalls.LiveCallsViewModel.Companion.HIDE_AFTER_HANG_UP_MS
import com.odorik.odorikbuddy.util.OdorikApiException
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Calls in progress for the Dashboard card and the "Calls in progress" screen.
 *
 * Polls only between [start] and [stop], which the screens tie to their lifecycle
 * (ON_START / ON_STOP), so nothing runs in the background. Errors never surface: the card
 * simply isn't shown. After `unauthorized` polling stays off for this process.
 */
@HiltViewModel
class LiveCallsViewModel @Inject constructor(
    private val repository: ActiveCallsRepository,
    private val contactNameResolver: ContactNameResolver,
    private val routingRepository: RoutingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _calls = MutableStateFlow<List<ActiveCall>>(emptyList())
    val calls: StateFlow<List<ActiveCall>> = _calls.asStateFlow()

    /** true = hang up succeeded, false = failed. One snackbar per event. */
    private val _hangUpEvent = Channel<Boolean>(Channel.BUFFERED)
    val hangUpEvent: Flow<Boolean> = _hangUpEvent.receiveAsFlow()

    private var pollJob: Job? = null

    /** Flips to true once own numbers are known; screens read it so titles recompose. */
    private val _ownNumbersLoaded = MutableStateFlow(ownNumberKeys != null)
    val ownNumbersLoaded: StateFlow<Boolean> = _ownNumbersLoaded.asStateFlow()

    fun start() {
        if (pollJob?.isActive == true || disabledForProcess) return
        loadContactsIfAllowed()
        loadOwnNumbersOnce()
        pollJob = viewModelScope.launch {
            while (isActive) {
                refreshOnce()
                delay(POLL_MS)
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
    }

    /**
     * Calls we hung up → time of the hang-up. Odorik keeps listing a call for a few seconds
     * after a successful DELETE (seen on 2026-11-22: ~6 s), so hide them for [HIDE_AFTER_HANG_UP_MS].
     */
    private val hungUp = mutableMapOf<Long, Long>()

    /** Last raw response (all legs), to hide every leg of a call we hang up. */
    private var lastRaw: List<ActiveCall> = emptyList()

    private fun visible(calls: List<ActiveCall>): List<ActiveCall> {
        val now = System.currentTimeMillis()
        hungUp.entries.removeAll { now - it.value > HIDE_AFTER_HANG_UP_MS }
        return calls.filter { it.id !in hungUp }.collapseLegs()
    }

    private suspend fun refreshOnce() {
        repository.getActiveCalls()
            .onSuccess {
                lastRaw = it
                _calls.value = visible(it)
            }
            .onFailure { e ->
                if (e is OdorikApiException && e.code == "unauthorized") {
                    disabledForProcess = true
                    stop()
                }
                _calls.value = emptyList()
            }
    }

    fun hangUp(id: Long) {
        viewModelScope.launch {
            val ok = repository.hangUp(id).isSuccess
            if (ok) {
                val now = System.currentTimeMillis()
                val key = lastRaw.firstOrNull { it.id == id }?.legKey
                lastRaw.filter { it.id == id || (key != null && it.legKey == key) }
                    .forEach { hungUp[it.id] = now }
                hungUp[id] = now
                _calls.value = visible(lastRaw)
            }
            _hangUpEvent.send(ok)
            refreshOnce()
        }
    }

    /**
     * Odorik reports no direction. A call whose destination is one of the account's own public
     * numbers is incoming (checked on a real call 2026-11-22: destination = own 800 number,
     * destination_name = "Česká rep. 800"), so the interesting party is the caller.
     */
    fun isIncoming(call: ActiveCall): Boolean {
        val keys = ownNumberKeys ?: return false
        return call.destinationNumber?.let { matchKey(it) in keys } == true
    }

    /**
     * The other party: contact name if known, else the number itself.
     * `destination_name` is NOT a name — it is Odorik's tariff zone (real outgoing call to
     * 800123456 on 2026-11-22 gave "Česká rep. 800"), so it only goes to the subtitle.
     */
    fun displayName(call: ActiveCall): String {
        val number = if (isIncoming(call)) call.sourceNumber else call.destinationNumber
        return nameOrNumber(number) ?: ""
    }

    /**
     * Outgoing: "from <caller>", incoming: "to <own number>". The number is shown as a contact
     * name when it is in the contacts. The tariff zone (`destination_name`) is never shown —
     * the user doesn't want it (2026-11-22).
     */
    fun subtitle(call: ActiveCall): String? {
        return if (isIncoming(call)) {
            nameOrNumber(call.destinationNumber)?.let { context.getString(R.string.live_call_to, it) }
        } else {
            nameOrNumber(call.sourceNumber)?.let { context.getString(R.string.live_call_from, it) }
        }
    }

    /** Contact name if [number] is in the contacts, else the formatted number; null if blank. */
    private fun nameOrNumber(number: String?): String? {
        if (number.isNullOrBlank()) return null
        val contact = contactNameResolver.getContactName(number)
        return if (contact != number) contact else PhoneNumberUtils.formatForDisplay(number)
    }

    private fun loadOwnNumbersOnce() {
        if (ownNumberKeys != null) return
        viewModelScope.launch {
            // Own numbers + shared numbers: a real incoming call (2026-11-22) went to a shared
            // 800 number that plain /public_numbers.json doesn't list.
            val own = routingRepository.getPublicNumbers().getOrNull()?.map { it.publicNumber }
            val shared = routingRepository.getSharedPublicNumbers().getOrNull()?.map { it.publicNumber }
            if (own == null && shared == null) return@launch   // try again on the next start()
            ownNumberKeys = (own.orEmpty() + shared.orEmpty())
                .map(::matchKey).filter { it.length >= 6 }.toSet()
            _ownNumbersLoaded.value = true   // screens recompose so titles switch to the caller
        }
    }

    /** Last 9 digits: matches 00420…, +420… and national forms. */
    private fun matchKey(n: String) = n.filter(Char::isDigit).takeLast(9)

    private fun loadContactsIfAllowed() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return
        viewModelScope.launch {
            runCatching { contactNameResolver.loadContacts(context.contentResolver) }
        }
    }

    companion object {
        const val POLL_MS = 5_000L
        private const val HIDE_AFTER_HANG_UP_MS = 15_000L

        /** Set after `unauthorized`; shared by all instances until the process dies. */
        @Volatile
        private var disabledForProcess = false

        /** Last 9 digits of the account's public numbers; null = not loaded yet. Per process. */
        @Volatile
        private var ownNumberKeys: Set<String>? = null
    }
}
