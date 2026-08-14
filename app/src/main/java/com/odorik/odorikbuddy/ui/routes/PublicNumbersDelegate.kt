package com.odorik.odorikbuddy.ui.routes

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PublicNumbersDelegate @Inject constructor(
    private val contactNameResolver: ContactNameResolver,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    @ApplicationContext private val context: Context
) {

    val contactsMap: StateFlow<Map<String, String>> = contactNameResolver.contactsMap

    suspend fun loadContacts(contentResolver: ContentResolver) {
        contactNameResolver.loadContacts(contentResolver)
    }


    fun getContactName(number: String): String {
        val resolved = contactNameResolver.getContactName(number)
        if (resolved == number) return number
        return "$resolved (${PhoneNumberUtils.formatForDisplay(number)})"
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
