package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.model.ActiveCall
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.ActiveCallsRepository
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.domain.usecase.NoSharedNumbersException
import com.odorik.odorikbuddy.domain.usecase.NoSourceNumberException
import com.odorik.odorikbuddy.domain.usecase.OneShotCallCoordinatorUseCase
import com.odorik.odorikbuddy.domain.usecase.SharedNumberNotFoundException
import com.odorik.odorikbuddy.util.BackoffPolicy
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val contactNameResolver: ContactNameResolver,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val callUseCase: CallUseCase,
    private val oneShotCallCoordinatorUseCase: OneShotCallCoordinatorUseCase,
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val tileRepository: TileRepository,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val activeCallsRepository: ActiveCallsRepository
) : ViewModel() {

    private val _callResult = MutableStateFlow<String>("")
    val callResult: StateFlow<String> = _callResult

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    /**
     * Failures of the lines/connection fetch. This is the only error that drives the
     * automatic retry loop, so retries can never wipe a user-facing action error.
     */
    private val _linesError = MutableStateFlow<String?>(null)
    val linesError: StateFlow<String?> = _linesError

    /** Failures of the user-initiated callback. Cleared only by the next callback attempt. */
    private val _callbackError = MutableStateFlow<String?>(null)
    val callbackError: StateFlow<String?> = _callbackError

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying

    private val retryBackoff = BackoffPolicy()

    private val _isCallbackLoading = MutableStateFlow(false)
    val isCallbackLoading: StateFlow<Boolean> = _isCallbackLoading

    private val _callerId = MutableStateFlow("")
    val callerId: StateFlow<String> = _callerId

    private val _callbackRecipient = MutableStateFlow("")
    val callbackRecipient: StateFlow<String> = _callbackRecipient

    private val _oneShotRecipient = MutableStateFlow("")
    val oneShotRecipient: StateFlow<String> = _oneShotRecipient

    val directCallsEnabled: Boolean
        get() = appPreferences.directCallsEnabled

    // --- Contacts Logic ---
    val callerContactName: StateFlow<String?> = combine(_callerId, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val callbackRecipientContactName: StateFlow<String?> = combine(_callbackRecipient, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val oneShotRecipientContactName: StateFlow<String?> = combine(_oneShotRecipient, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedLine = MutableStateFlow<Int?>(null)
    val selectedLine: StateFlow<Int?> = _selectedLine

    private val _oneShotCallResult = MutableStateFlow<String>("")
    val oneShotCallResult: StateFlow<String> = _oneShotCallResult

    // One-shot event for launching the dialer (better than watching result in UI)
    private val _dialerLaunchRequest = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val dialerLaunchRequest = _dialerLaunchRequest.asSharedFlow()

    private val _oneShotCallError = MutableStateFlow<String?>("")
    val oneShotCallError: StateFlow<String?> = _oneShotCallError

    private val _isOneShotCallLoading = MutableStateFlow(false)
    val isOneShotCallLoading: StateFlow<Boolean> = _isOneShotCallLoading

    private val _useCallerIdPrefix = MutableStateFlow(getUseCallerIdPrefix())
    val useCallerIdPrefix: StateFlow<Boolean> = _useCallerIdPrefix

    private fun getUseCallerIdPrefix(): Boolean {
        return appPreferences.getString("use_caller_id_prefix", "false")?.toBoolean() ?: false
    }

    private val _selectedTab = MutableStateFlow(getSelectedTab())
    val selectedTab: StateFlow<String> = _selectedTab

    private fun getSelectedTab(): String {
        val savedString = appPreferences.getString("calls_selected_tab", null)
        val defaultTitle = "callback_title" // Fallback to first tab
        val tabOrder = getTabOrder()
        return if (savedString?.toIntOrNull() != null) {
            // Migrate legacy index to title
            val oldIndex = savedString.toInt()
            val migratedTitle = if (oldIndex in tabOrder.indices) tabOrder[oldIndex] else defaultTitle
            // Save the migrated title for future use
            appPreferences.saveString("calls_selected_tab", migratedTitle)
            migratedTitle
        } else {
            // Already a title, validate it exists in order
            savedString?.takeIf { it in tabOrder } ?: defaultTitle
        }
    }

    private val _tabOrder = MutableStateFlow(getTabOrder())
    val tabOrder: StateFlow<List<String>> = _tabOrder

    private fun getTabOrder(): List<String> {
        val savedOrder = appPreferences.getString("calls_tab_order", null)
        val defaultOrder = DEFAULT_TAB_ORDER
        
        if (savedOrder == null) return defaultOrder
        
        val currentList = savedOrder.split(",").filter { it.isNotBlank() }.toMutableList()
        // Ensure new tabs are added if missing (for existing users)
        if (!currentList.contains("tiles_title")) {
            currentList.add("tiles_title")
        }
        
        return currentList
    }

    private val _phoneNumber = MutableStateFlow(getPhoneNumber())
    val phoneNumber: StateFlow<String> = _phoneNumber

    private fun getPhoneNumber(): String {
        return appPreferences.getString("phone_number", "") ?: ""
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
                getLinesInternal()
                if (_linesError.value == null) break
            }
            _isRetrying.value = false
        }
    }

    fun stopErrorRetry() {
        _isRetrying.value = false
    }

    init {
        // Automatically manage error retry when error state changes
        viewModelScope.launch {
            linesError.collect { currentError ->
                if (!currentError.isNullOrEmpty()) {
                    startErrorRetry()
                } else {
                    stopErrorRetry()
                }
            }
        }

        reloadSavedFields()
        // _selectedTab is already initialized with getSelectedTab() in the property declaration

        // Perform initial data load
        getLines()
    }

    /**
     * Re-reads the fields that are saved in preferences. Called on init and each time the
     * Calls screen is shown, so values written elsewhere (settings import) appear without a
     * restart. Every setter below saves immediately, so nothing typed is lost.
     */
    fun reloadSavedFields() {
        _callerId.value = appPreferences.getString("caller_id", "") ?: ""
        _callbackRecipient.value = appPreferences.getString("recipient", "") ?: ""
        _oneShotRecipient.value = appPreferences.getString("oneshot_recipient", "") ?: ""
        _selectedLine.value = appPreferences.getString("selected_line", null)?.toIntOrNull()
        _useCallerIdPrefix.value = getUseCallerIdPrefix()
        _tabOrder.value = getTabOrder()
    }

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }

    fun updateTabOrder(newOrder: List<String>) {
        _tabOrder.value = newOrder
        appPreferences.saveString("calls_tab_order", newOrder.joinToString(","))
    }

    fun getTabIndexByTitle(title: String): Int {
        return _tabOrder.value.indexOf(title).takeIf { it >= 0 } ?: 0
    }

    fun updateSelectedTab(tabTitle: String) {
        if (tabTitle in _tabOrder.value) {
            _selectedTab.value = tabTitle
            appPreferences.saveString("calls_selected_tab", tabTitle)
        }
    }

    fun updateCallerId(newCallerId: String) {
        _callerId.value = newCallerId
        appPreferences.saveString("caller_id", newCallerId)
    }

    fun updateCallbackRecipient(newRecipient: String) {
        _callbackRecipient.value = newRecipient
        appPreferences.saveString("recipient", newRecipient)
    }

    fun updateOneShotRecipient(newRecipient: String) {
        _oneShotRecipient.value = newRecipient
        appPreferences.saveString("oneshot_recipient", newRecipient)
    }

    fun updateSelectedLine(newLine: Int?) {
        _selectedLine.value = newLine
        appPreferences.saveString("selected_line", newLine?.toString() ?: "")
    }

    fun updateUseCallerIdPrefix(useCallerIdPrefix: Boolean) {
        _useCallerIdPrefix.value = useCallerIdPrefix
        appPreferences.saveString("use_caller_id_prefix", useCallerIdPrefix.toString())
    }

    fun updateSelectedTabByIndex(tabIndex: Int) {
        val tabTitles = _tabOrder.value
        if (tabIndex in tabTitles.indices) {
            val tabTitle = tabTitles[tabIndex]
            updateSelectedTab(tabTitle)
        }
    }

    fun getLines() {
        viewModelScope.launch { getLinesInternal() }
    }

    private suspend fun getLinesInternal() {
        val result = getLinesUseCase.execute()
        result.onSuccess {
            _lines.value = it
            // Clear only this operation's error — a background refresh must not
            // erase the result or error of a billable action the user just triggered.
            _linesError.value = null
            if (_selectedLine.value == null && it.isNotEmpty()) {
                _selectedLine.value = it.first().id
            }
        }.onFailure {
            val localizedContext = localeManager.createLocaleContext(context)
            _linesError.value = ErrorMessageUtil.standardizeError(it, localizedContext)
        }
    }

    fun makeCall(callerId: String, recipient: String, line: String) {
        if (_isCallbackLoading.value) return
        _isCallbackLoading.value = true
        viewModelScope.launch {
            makeCallInternal(callerId, recipient, line)
            val result = _callResult.value
            if (result.startsWith("callback_ordered") || result.startsWith("successfully_enqueued")) {
                watchCallback(callerId, recipient)
            }
        }
    }

    /** Live status of the last callback ordered from the Callback tab (null = no card). */
    sealed interface CallbackStatus {
        object Waiting : CallbackStatus
        data class Ringing(val call: ActiveCall) : CallbackStatus
        data class Connected(val call: ActiveCall) : CallbackStatus
        object Ended : CallbackStatus
    }

    private val _callbackStatus = MutableStateFlow<CallbackStatus?>(null)
    val callbackStatus: StateFlow<CallbackStatus?> = _callbackStatus
    private var callbackWatchJob: Job? = null

    /**
     * Watches /active_calls.json every 3 s for a call involving [caller] or [recipient].
     * Gives up quietly after ~60 s without a match; after a match, runs until the call is gone.
     * Iteration counts (not wall time) bound the loop so virtual-time tests can't spin forever.
     */
    private fun watchCallback(caller: String, recipient: String) {
        callbackWatchJob?.cancel()
        val keys = listOf(caller, recipient).map(::matchKey).filter { it.length >= 6 }
        if (keys.isEmpty()) return
        callbackWatchJob = viewModelScope.launch {
            _callbackStatus.value = CallbackStatus.Waiting
            var seen = false
            var waitingRounds = 0
            var totalRounds = 0
            while (isActive && totalRounds < MAX_WATCH_ROUNDS) {
                totalRounds++
                val calls = runCatching { activeCallsRepository.getActiveCalls().getOrNull() }.getOrNull().orEmpty()
                val match = calls.firstOrNull { c ->
                    listOfNotNull(c.sourceNumber, c.destinationNumber).any { matchKey(it) in keys }
                }
                if (match != null) {
                    seen = true
                    _callbackStatus.value =
                        if (match.isAnswered) CallbackStatus.Connected(match) else CallbackStatus.Ringing(match)
                } else if (seen) {
                    _callbackStatus.value = CallbackStatus.Ended
                    delay(5_000)
                    break
                } else if (++waitingRounds >= MAX_WAITING_ROUNDS) {
                    break
                }
                delay(WATCH_INTERVAL_MS)
            }
            _callbackStatus.value = null
        }
    }

    /** Ends the watched callback call; the watch loop then notices it is gone. */
    fun hangUpCallback() {
        val call = when (val s = _callbackStatus.value) {
            is CallbackStatus.Ringing -> s.call
            is CallbackStatus.Connected -> s.call
            else -> return
        }
        viewModelScope.launch { activeCallsRepository.hangUp(call.id) }
    }

    /** Last 9 digits: matches 00420…, +420… and 9-digit national forms. */
    private fun matchKey(n: String) = n.filter(Char::isDigit).takeLast(9)

    /** Caller must have set [_isCallbackLoading] to true first — that flag is the double-submit guard. */
    private suspend fun makeCallInternal(callerId: String, recipient: String, line: String) {
        try {
            _callbackError.value = null // Clear previous errors
            _callResult.value = "" // Clear previous results
            val result = callUseCase.execute(callerId, recipient, line)
            result.onSuccess {
                _callResult.value = it
            }.onFailure {
                val localizedContext = localeManager.createLocaleContext(context)
                _callbackError.value = ErrorMessageUtil.standardizeError(it, localizedContext)
            }
        } finally {
            _isCallbackLoading.value = false
        }
    }

    /**
     * Reads phone numbers for a picked contact off the main thread. A ContentResolver query
     * against the contacts provider is disk I/O and visibly janks the picker on large
     * address books.
     */
    suspend fun getPhoneNumbersFromContact(
        contentResolver: ContentResolver,
        contactUri: Uri
    ): List<String> = withContext(Dispatchers.IO) {
        getPhoneNumbersForContactUseCase(contentResolver, contactUri)
    }

    /**
     * Initiates a one-shot call.
     *
     * @param targetRecipient The number to call.
     * @param useLineAsCallerId Whether to use the selected line's caller ID.
     * @param selectedLineId Optional specific line ID (used by widgets that have per-tile line configuration).
     *                       If null, falls back to the globally selected line.
     */
    fun makeOneShotCall(
        targetRecipient: String,
        useLineAsCallerId: Boolean,
        selectedLineId: Int? = null
    ) {
        if (_isOneShotCallLoading.value) return
        viewModelScope.launch {
            _oneShotCallError.value = null
            _oneShotCallResult.value = ""  // Clear previous result
            _isOneShotCallLoading.value = true

            try {
                // Use provided line ID (for widgets) or fall back to global selection
                val lineIdToUse = selectedLineId ?: _selectedLine.value

                val result = oneShotCallCoordinatorUseCase.execute(
                    targetRecipient = targetRecipient,
                    useLineAsCallerId = useLineAsCallerId,
                    selectedLineId = lineIdToUse
                )

                result.onSuccess { lastSharedNumber ->
                    _oneShotCallResult.value = lastSharedNumber
                    _dialerLaunchRequest.tryEmit(lastSharedNumber) // Notify UI to launch dialer
                }.onFailure { e ->
                    val localizedContext = localeManager.createLocaleContext(context)
                    _oneShotCallError.value = when (e) {
                        is NoSourceNumberException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_source_number_configured)
                        is NoSharedNumbersException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_available)
                        is SharedNumberNotFoundException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_found)
                        else -> ErrorMessageUtil.standardizeError(e, localizedContext)
                    }
                }
            } catch (e: Exception) {
                val localizedContext = localeManager.createLocaleContext(context)
                _oneShotCallError.value = ErrorMessageUtil.standardizeError(e, localizedContext)
            } finally {
                _isOneShotCallLoading.value = false
            }
        }
    }
    
    // --- Widget-launched tile actions ---

    private var widgetDispatchClaimed = false

    private val _widgetCallbackSucceeded = MutableSharedFlow<String>(extraBufferCapacity = 1)
    /** Emits the recipient once a widget-launched callback has been placed successfully. */
    val widgetCallbackSucceeded = _widgetCallbackSucceeded.asSharedFlow()

    /**
     * Runs a widget tile's action at most once per ViewModel instance.
     *
     * The guard and the work both live here rather than in the launching Activity: the
     * ViewModel survives Activity recreation (rotation, theme/locale change), so a recreation
     * cannot place a second billable call, and [viewModelScope] keeps an in-flight request
     * alive even if the Activity is destroyed mid-call.
     */
    fun dispatchWidgetTileAction(tileId: Int) {
        if (widgetDispatchClaimed) return
        widgetDispatchClaimed = true

        viewModelScope.launch {
            val tile = tileRepository.getTileById(tileId)
            if (tile == null) {
                _callbackError.value =
                    context.getString(com.odorik.odorikbuddy.R.string.widget_error_tile_not_found)
                return@launch
            }

            val globalLine = appPreferences.getString("selected_line", null)
            val lineId = tile.lineId?.takeIf { it.isNotBlank() } ?: globalLine

            if (tile.callType == com.odorik.odorikbuddy.data.local.entity.TileCallType.CALLBACK) {
                val globalCallerId = appPreferences.getString("caller_id", "") ?: ""
                val callerId = tile.callerId?.takeIf { it.isNotBlank() } ?: globalCallerId
                if (callerId.isBlank()) {
                    _callbackError.value =
                        context.getString(com.odorik.odorikbuddy.R.string.callback_error_no_caller_id)
                    return@launch
                }

                _isCallbackLoading.value = true
                makeCallInternal(callerId, tile.recipient, lineId ?: "")
                if (_callbackError.value == null) {
                    _widgetCallbackSucceeded.tryEmit(tile.recipient)
                }
            } else {
                makeOneShotCall(
                    targetRecipient = tile.recipient,
                    useLineAsCallerId = tile.useLineAsCallerId,
                    selectedLineId = lineId?.toIntOrNull()
                )
            }
        }
    }

    fun resetCallResult() {
        _callResult.value = ""
    }
    
    fun resetOneShotCallResult() {
        _oneShotCallResult.value = ""
    }

    fun resetOneShotCallError() {
        _oneShotCallError.value = null
    }

    fun resetCallbackError() {
        _callbackError.value = null
    }

    companion object {
        /** Tab ids of the Calls screen, in the default order. Also validates imported orders. */
        val DEFAULT_TAB_ORDER = listOf("callback_title", "oneshot_call", "tiles_title")

        private const val WATCH_INTERVAL_MS = 3_000L
        private const val MAX_WAITING_ROUNDS = 20          // ~60 s without any matching call
        private const val MAX_WATCH_ROUNDS = 2_400         // hard stop after ~2 h
    }
}
