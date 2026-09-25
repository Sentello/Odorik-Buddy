package com.odorik.odorikbuddy.ui.sms

import android.content.Context
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.SmsRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.util.SmsDraftHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import java.io.IOException


@OptIn(ExperimentalCoroutinesApi::class)
class SmsViewModelErrorStateTest {

    private val dispatcher = StandardTestDispatcher()

    private val context: Context = mock {
        on { getString(any()) } doReturn "error-message"
    }

    private val localeManager: LocaleManager = mock {
        on { createLocaleContext(any()) } doReturn context
    }

    private val contactNameResolver: ContactNameResolver = mock {
        on { contactsMap } doReturn MutableStateFlow(emptyMap())
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(smsRepository: SmsRepository) = SmsViewModel(
        contactNameResolver = contactNameResolver,
        getPhoneNumbersForContactUseCase = mock<GetPhoneNumbersForContactUseCase>(),
        getLinesUseCase = mock<GetLinesUseCase>(),
        smsRepository = smsRepository,
        smsDraftHelper = mock<SmsDraftHelper>(),
        localeManager = localeManager,
        context = context
    )

    @Test
    fun `send failure survives the allowed-senders retry cycle`() = runTest(dispatcher) {
        val repository: SmsRepository = mock {
            onBlocking { getAllowedSenders() } doReturn Result.failure(IOException("boom"))
            onBlocking { sendSms(any(), any(), anyOrNull(), anyOrNull()) } doReturn
                Result.failure(IOException("send failed"))
        }
        val vm = viewModel(repository)



        vm.fetchAllowedSenders()
        runCurrent()
        assertNotNull("the first senders fetch should have failed", vm.sendersError.value)

        vm.sendSms("+420777123456", "hello", sender = null)
        runCurrent()

        val sendError = vm.sendError.value
        assertNotNull("the send failure must be reported", sendError)



        advanceUntilIdle()

        verifyBlocking(repository, atLeast(2)) { getAllowedSenders() }
        assertEquals(
            "the send error must survive the allowed-senders retry loop",
            sendError,
            vm.sendError.value
        )
    }

    @Test
    fun `a send failure does not trigger the senders retry loop`() = runTest(dispatcher) {
        val repository: SmsRepository = mock {
            onBlocking { sendSms(any(), any(), anyOrNull(), anyOrNull()) } doReturn
                Result.failure(IOException("send failed"))
        }
        val vm = viewModel(repository)

        vm.sendSms("+420777123456", "hello", sender = null)
        advanceUntilIdle()

        assertNotNull(vm.sendError.value)
        assertNull("a send failure is not a connection problem", vm.sendersError.value)
        verifyBlocking(repository, never()) { getAllowedSenders() }
    }

    @Test
    fun `a successful send reports a result and no error`() = runTest(dispatcher) {
        val repository: SmsRepository = mock {
            onBlocking { sendSms(any(), any(), anyOrNull(), anyOrNull()) } doReturn
                Result.success("sent")
        }
        val vm = viewModel(repository)

        vm.sendSms("+420777123456", "hello", sender = null)
        advanceUntilIdle()

        assertNull(vm.sendError.value)
        assertEquals("sent", vm.sendResult.value)
    }
}
