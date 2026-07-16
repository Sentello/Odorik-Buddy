package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ContactNameResolver @Inject constructor(
    private val loadContactsUseCase: LoadContactsUseCase
) {
    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    private val contactNameCache = mutableMapOf<String, String>()

    @Volatile
    private var lastLoadTimestampMs: Long = 0L


    private val reloadThrottleMs = 60_000L

    suspend fun loadContacts(contentResolver: ContentResolver, force: Boolean = false) {
        if (!force &&
            _contactsMap.value.isNotEmpty() &&
            System.currentTimeMillis() - lastLoadTimestampMs < reloadThrottleMs
        ) {
            return
        }
        val contacts = withContext(Dispatchers.IO) {
            loadContactsUseCase(contentResolver)
        }
        synchronized(contactNameCache) {
            _contactsMap.value = contacts
            contactNameCache.clear()
            lastLoadTimestampMs = System.currentTimeMillis()
        }
    }


    fun getContactName(number: String): String {
        if (number.isBlank()) return number

        synchronized(contactNameCache) {
            contactNameCache[number]?.let { return it }
        }

        val resolved = resolveContactName(number, _contactsMap.value)

        synchronized(contactNameCache) {
            contactNameCache[number] = resolved
        }
        return resolved
    }

    private fun resolveContactName(number: String, contacts: Map<String, String>): String {
        if (contacts.isEmpty()) return number

        val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)


        val exactName = contacts[parsedInput.normalizedNumber]
        if (exactName != null) {
            return if (parsedInput.specialPrefix.isNotEmpty()) {
                "${parsedInput.specialPrefix} $exactName".trim()
            } else {
                exactName
            }
        }


        val n1 = parsedInput.normalizedNumber.replace("+", "")
        if (n1.length <= 8) return number

        for ((contactNumber, contactName) in contacts) {
            val n2 = contactNumber.replace("+", "")
            if (n2.length <= 8) continue
            if (n1 == n2 || n1.endsWith(n2) || n2.endsWith(n1)) {
                return if (parsedInput.specialPrefix.isNotEmpty()) {
                    "${parsedInput.specialPrefix} $contactName".trim()
                } else {
                    contactName
                }
            }
        }

        return number
    }
}
