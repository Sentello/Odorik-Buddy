package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ContactNameResolverTest {

    private val number = "+420777123456"
    private val key = PhoneNumberUtils.normalizeForStorage(number)

    private fun resolverWith(
        contacts: Map<String, String>,
        dispatcher: CoroutineDispatcher
    ): ContactNameResolver {
        val useCase = mock<LoadContactsUseCase> { on { invoke(any()) } doReturn contacts }
        return ContactNameResolver(useCase).apply { ioDispatcher = dispatcher }
    }

    /**
     * Cold start: the number is known before the contacts are loaded (same as CallViewModel).
     * The unconfined dispatcher runs collectors inline on a StateFlow assignment, like
     * Dispatchers.Main.immediate does on the main thread.
     */
    @Test
    fun nameAppearsWhenContactsLoadAfterTheFieldIsObserved() = runTest(UnconfinedTestDispatcher()) {
        val resolver = resolverWith(mapOf(key to "Babička"), coroutineContext[CoroutineDispatcher]!!)
        val field = MutableStateFlow(number)
        val name = combine(field, resolver.contactsMap) { n, _ ->
            resolver.getContactName(n).takeIf { it != n }
        }.stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), null)
        name.launchIn(backgroundScope)

        resolver.loadContacts(mock<ContentResolver>())

        assertEquals("Babička", name.value)
    }

    @Test
    fun lookupBeforeLoadIsNotCached() = runTest {
        val resolver = resolverWith(mapOf(key to "Babička"), coroutineContext[CoroutineDispatcher]!!)
        assertEquals(number, resolver.getContactName(number))

        resolver.loadContacts(mock<ContentResolver>())

        assertEquals("Babička", resolver.getContactName(number))
    }
}
