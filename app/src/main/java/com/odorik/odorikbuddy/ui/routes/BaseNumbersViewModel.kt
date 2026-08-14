package com.odorik.odorikbuddy.ui.routes

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.DeleteRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetRoutesForNumberUseCase
import com.odorik.odorikbuddy.model.Route
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


abstract class BaseNumbersViewModel<T : Any>(
    val publicNumbersDelegate: PublicNumbersDelegate,
    private val getRoutesForNumberUseCase: GetRoutesForNumberUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
    private val localeManager: LocaleManager,
    private val context: Context
) : ViewModel() {

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }


    protected abstract suspend fun fetchNumbers(): Result<List<T>>


    protected abstract fun publicNumberOf(item: T): String


    protected open fun dialogReplaceBySourceValue(): Boolean = false

    private val _uiState = MutableStateFlow<UiState<List<T>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<T>>> = _uiState

    private val _routesMap = MutableStateFlow<Map<String, List<Route>>>(emptyMap())
    val routesMap: StateFlow<Map<String, List<Route>>> = _routesMap.asStateFlow()

    val contactsMap: StateFlow<Map<String, String>> = publicNumbersDelegate.contactsMap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _dialogSourceNumber = MutableStateFlow("")
    val dialogSourceNumber: StateFlow<String> = _dialogSourceNumber.asStateFlow()

    private val _dialogRingingNumber = MutableStateFlow("")
    val dialogRingingNumber: StateFlow<String> = _dialogRingingNumber.asStateFlow()

    private val _dialogUseCallerIdPrefix = MutableStateFlow(false)
    val dialogUseCallerIdPrefix: StateFlow<Boolean> = _dialogUseCallerIdPrefix.asStateFlow()

    fun onSourceNumberChange(value: String) { _dialogSourceNumber.value = value }
    fun onRingingNumberChange(value: String) { _dialogRingingNumber.value = value }
    fun onUseCallerIdPrefixChange(value: Boolean) { _dialogUseCallerIdPrefix.value = value }

    open fun resetDialogState() {
        _dialogSourceNumber.value = ""
        _dialogRingingNumber.value = ""
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
                publicNumbersDelegate.loadContacts(contentResolver)
            }

            val numbersResult = publicNumbersDelegate.retryWithExponentialBackoff {
                fetchNumbers()
            }

            numbersResult.onSuccess { numbers ->
                try {
                    val routesMap = numbers.map { number ->
                        async {
                            val routesResult = getRoutesForNumberUseCase.execute(publicNumberOf(number))
                            publicNumberOf(number) to routesResult.getOrNull().orEmpty()
                        }
                    }.awaitAll().toMap()

                    _routesMap.value = routesMap
                    _uiState.value = UiState.Success(numbers)

                } catch (e: Exception) {
                    val localizedContext = localeManager.createLocaleContext(context)
                    val errorMessage = ErrorMessageUtil.standardizeError(e, localizedContext)
                    _uiState.value = UiState.Error(errorMessage)
                    _error.value = errorMessage
                }

            }.onFailure { e ->
                val localizedContext = localeManager.createLocaleContext(context)
                val errorMessage = ErrorMessageUtil.standardizeError(e, localizedContext)
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
                _error.value = ErrorMessageUtil.standardizeError(e, localizedContext)
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
                dialogReplaceBySourceValue(),
                _dialogUseCallerIdPrefix.value
            )
            result
                .onSuccess {
                    refreshRoutesForNumber(publicNumber)
                }
                .onFailure { e ->
                    val localizedContext = localeManager.createLocaleContext(context)
                    _error.value = ErrorMessageUtil.standardizeError(e, localizedContext)
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
                    _error.value = ErrorMessageUtil.standardizeError(e, localizedContext)
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getContactName(number: String): String = publicNumbersDelegate.getContactName(number)

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            publicNumbersDelegate.loadContacts(contentResolver)
        }
    }

    fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> =
        publicNumbersDelegate.getPhoneNumbersFromContact(contentResolver, contactUri)
}
