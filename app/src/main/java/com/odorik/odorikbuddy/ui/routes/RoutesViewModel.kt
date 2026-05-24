package com.odorik.odorikbuddy.ui.routes

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.DeleteRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetRoutesForNumberUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.model.Route
import com.odorik.odorikbuddy.model.SharedPublicNumber
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val getRoutesForNumberUseCase: GetRoutesForNumberUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _uiState = MutableStateFlow<UiState<List<SharedPublicNumber>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SharedPublicNumber>>> = _uiState

    private val _routesMap = MutableStateFlow<Map<String, List<Route>>>(emptyMap())
    val routesMap: StateFlow<Map<String, List<Route>>> = _routesMap.asStateFlow()


    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    private val contactNameCache = java.util.concurrent.ConcurrentHashMap<String, String>()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                    val localizedContext = localeManager.createLocaleContext(context)
                    val errorMessage = ErrorMessageUtil.standardizeError(e.message, localizedContext)
                    _uiState.value = UiState.Error(errorMessage)
                    _error.value = errorMessage
                }

            }.onFailure { e ->
                val localizedContext = localeManager.createLocaleContext(context)
                val errorMessage = ErrorMessageUtil.standardizeError(e.message, localizedContext)
                _uiState.value = UiState.Error(errorMessage)
                _error.value = errorMessage
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
            }.onFailure { e ->
                val localizedContext = localeManager.createLocaleContext(context)
                _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
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
                .onFailure { e ->
                    val localizedContext = localeManager.createLocaleContext(context)
                    _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
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
                .onFailure { e ->
                    val localizedContext = localeManager.createLocaleContext(context)
                    _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }





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

                            val normalizedNumber = PhoneNumberUtils.normalizeForStorage(number)

            var foundMatch = number
            val n1 = parsedInput.normalizedNumber.replace("+", "")
            for ((contactNumber, contactName) in _contactsMap.value) {
                val n2 = contactNumber.replace("+", "")
                if (n1 == n2 || (n1.length > 8 && n2.length > 8 && (n1.endsWith(n2) || n2.endsWith(n1)))) {
                    val numberPart = if (parsedInput.specialPrefix.isNotEmpty()) {
                        "${parsedInput.specialPrefix} ${PhoneNumberUtils.formatForDisplay(parsedInput.normalizedNumber)}"
                    } else {
                        PhoneNumberUtils.formatForDisplay(parsedInput.normalizedNumber)
                    }
                    foundMatch = "$contactName ($numberPart)"
                    break
                }
            }


            foundMatch
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


                        number?.let { numbers.add(it) }
                    }
                }
            }
        }
        return numbers.distinct()
    }


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
        return Result.failure(Exception(context.resources.getQuantityString(R.plurals.error_retry_failed, times, times)))
    }
}
