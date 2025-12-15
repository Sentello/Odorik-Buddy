package com.odorik.odorikbuddy.ui.routes

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.DeleteRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetRoutesForNumberUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.model.Route
import com.odorik.odorikbuddy.model.SharedPublicNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.delay

import com.odorik.odorikbuddy.R

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val getRoutesForNumberUseCase: GetRoutesForNumberUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase
) : ViewModel() {

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val messageResId: Int) : UiState<Nothing>()
    }

    private val _uiState = MutableStateFlow<UiState<List<SharedPublicNumber>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SharedPublicNumber>>> = _uiState

    private val _routesMap = MutableStateFlow<Map<String, List<Route>>>(emptyMap())
    val routesMap: StateFlow<Map<String, List<Route>>> = _routesMap.asStateFlow()

    // --- NEW: STATE FOR CONTACTS MAP ---
    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error.asStateFlow()

    private val _dialogSourceNumber = MutableStateFlow("")
    val dialogSourceNumber: StateFlow<String> = _dialogSourceNumber.asStateFlow()

    private val _dialogRingingNumber = MutableStateFlow("")
    val dialogRingingNumber: StateFlow<String> = _dialogRingingNumber.asStateFlow()

    private val _dialogReplaceBySource = MutableStateFlow(false)
    val dialogReplaceBySource: StateFlow<Boolean> = _dialogReplaceBySource.asStateFlow()

    private val _dialogUseCallerIdPrefix = MutableStateFlow(false)
    val dialogUseCallerIdPrefix: StateFlow<Boolean> = _dialogUseCallerIdPrefix.asStateFlow()

    init {
        loadData()
    }

    fun onSourceNumberChange(value: String) { _dialogSourceNumber.value = value }
    fun onRingingNumberChange(value: String) { _dialogRingingNumber.value = value }
    fun onReplaceBySourceChange(value: Boolean) { _dialogReplaceBySource.value = value }
    fun onUseCallerIdPrefixChange(value: Boolean) { _dialogUseCallerIdPrefix.value = value }

    fun resetDialogState() {
        _dialogSourceNumber.value = ""
        _dialogRingingNumber.value = ""
        _dialogReplaceBySource.value = false
        _dialogUseCallerIdPrefix.value = false
    }

    fun loadData(isRefresh: Boolean = false, contentResolver: ContentResolver? = null) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = UiState.Loading
            }
            _isLoading.value = true
            _error.value = null

            if (isRefresh && contentResolver != null) {
                loadContacts(contentResolver)
            }

            val numbersResult = retryWithExponentialBackoff {
                getSharedPublicNumbersUseCase.execute()
            }

            numbersResult.onSuccess { numbers ->
                try {
                    val routesMap = numbers.map { number ->
                        async {
                            val routesResult = getRoutesForNumberUseCase.execute(number.publicNumber)
                            number.publicNumber to routesResult.getOrNull().orEmpty()
                        }
                    }.awaitAll().toMap()

                    _routesMap.value = routesMap
                    _uiState.value = UiState.Success(numbers)

                } catch (e: Exception) {
                    _uiState.value = UiState.Error(R.string.error_loading_routes_for_numbers)
                    _error.value = R.string.error_loading_routes_for_numbers
                }

            }.onFailure { _ ->
                _uiState.value = UiState.Error(R.string.error_loading_shared_numbers)
                _error.value = R.string.error_loading_shared_numbers
            }
            _isLoading.value = false
        }
    }

    private fun refreshRoutesForNumber(publicNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getRoutesForNumberUseCase.execute(publicNumber)
            result.onSuccess { routes ->
                _routesMap.value = _routesMap.value + (publicNumber to routes)
            }.onFailure { _ ->
                _error.value = R.string.error_loading_routes_for_numbers
            }
            _isLoading.value = false
        }
    }

    fun createRoute(publicNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = createRouteUseCase.execute(
                publicNumber,
                _dialogSourceNumber.value,
                _dialogRingingNumber.value,
                _dialogReplaceBySource.value,
                _dialogUseCallerIdPrefix.value
            )
            result
                .onSuccess {
                    refreshRoutesForNumber(publicNumber)
                }
                .onFailure { _ ->
                    _error.value = R.string.error_creating_route
                }
            _isLoading.value = false
        }
    }

    fun deleteRoute(publicNumber: String, routeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = deleteRouteUseCase.execute(publicNumber, routeId)
            result
                .onSuccess {
                    refreshRoutesForNumber(publicNumber)
                }
                .onFailure { _ ->
                    _error.value = R.string.error_deleting_route
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // --- START: NEW AND MODIFIED CONTACTS FUNCTIONS ---

    /**
     * Normalizes a phone number to a consistent format (+E.164) for reliable matching.
     * - Replaces leading "00" with "+".
     * - Ensures a single "+" at the start if it's an international number.
     * - Removes all non-digit characters except the leading "+".
     */
    private fun normalizePhoneNumber(number: String): String {
        var normalized = number.trim()
        // Replace leading 00 with +
        if (normalized.startsWith("00")) {
            normalized = "+${normalized.substring(2)}"
        }
        // Remove all non-numeric characters except the leading '+'
        return normalized.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Fetches all contacts with phone numbers from the device and populates the contactsMap.
     * This should be called after READ_CONTACTS permission is granted.
     */
    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val contacts = mutableMapOf<String, String>()

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                if (numberIndex >= 0 && nameIndex >= 0) {
                    while (cursor.moveToNext()) {
                        val number = cursor.getString(numberIndex)
                        val name = cursor.getString(nameIndex)
                        if (!number.isNullOrBlank() && !name.isNullOrBlank()) {
                            // Use the normalized number as the key for reliable matching
                            val normalizedNumber = normalizePhoneNumber(number)
                            // We only add it if it's not already there to prefer the primary display name
                            if (!contacts.containsKey(normalizedNumber)) {
                                contacts[normalizedNumber] = name
                            }
                        }
                    }
                }
            }
            _contactsMap.value = contacts
        }
    }
    
    /**
     * Public function for the UI to get a contact name for a given number.
     * It is now "prefix-aware" for special codes like "*087".
     *
     * It normalizes the number before looking it up in the map.
     * Returns the original number if no contact is found.
     */
    fun getContactName(number: String): String {
        val prefix = "*087"
        var numberToLookup = number
        var detectedPrefix = ""

        // Step 1: Check for and separate our special prefix
        if (number.startsWith(prefix)) {
            detectedPrefix = prefix
            numberToLookup = number.substring(prefix.length)
        }

        // Step 2: Normalize and look up the *actual* phone number part
        val normalizedNumber = normalizePhoneNumber(numberToLookup)
        val contactName = _contactsMap.value[normalizedNumber]

        // Step 3: Reconstruct the final display string
        return if (contactName != null) {
            // If a name was found, prepend the prefix to the name
            // Add a space for better readability
            "$detectedPrefix $contactName".trim()
        } else {
            // If no name was found, return the original, full string
            // so the user still sees the prefix information.
            number
        }
    }


    fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> {
        val numbers = mutableListOf<String>()
        var contactId: String? = null

        contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            }
        }

        contactId?.let { id ->
            val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
            val selectionArgs = arrayOf(id)

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection,
                selectionArgs,
                null
            )?.use { phoneCursor ->
                val numberColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberColumnIndex >= 0) {
                    while (phoneCursor.moveToNext()) {
                        val number = phoneCursor.getString(numberColumnIndex)
                        // Return the raw number here, the dialog will show it as is.
                        // The normalization happens when setting it as a value or comparing.
                        number?.let { numbers.add(it) }
                    }
                }
            }
        }
        return numbers.distinct()
    }
    // --- END: NEW AND MODIFIED CONTACTS FUNCTIONS ---

    suspend fun <T> retryWithExponentialBackoff(
        times: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 16000,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(times) {
            val result = block()
            if (result.isSuccess) return result
            if (it < times - 1) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return Result.failure(Exception("Failed after $times attempts"))
    }
}
