package com.odorik.odorikbuddy.ui.routes

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.domain.usecase.LoadContactsUseCase
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PublicNumbersDelegate @Inject constructor(
    private val loadContactsUseCase: LoadContactsUseCase,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    @ApplicationContext private val context: Context
) {

    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    private val contactNameCache = mutableMapOf<String, String>()

    suspend fun loadContacts(contentResolver: ContentResolver) {
        _contactsMap.value = loadContactsUseCase(contentResolver)
        contactNameCache.clear()
    }

    fun getContactName(number: String): String {
        return contactNameCache.getOrPut(number) {
            val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)

            val exactName = _contactsMap.value[parsedInput.normalizedNumber]
            if (exactName != null) {
                return@getOrPut if (parsedInput.specialPrefix.isNotEmpty()) {
                    "${parsedInput.specialPrefix} ${PhoneNumberUtils.formatForDisplay(parsedInput.normalizedNumber)}"
                } else {
                    "$exactName (${PhoneNumberUtils.formatForDisplay(parsedInput.normalizedNumber)})"
                }
            }

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
        return getPhoneNumbersForContactUseCase(contentResolver, contactUri)
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
