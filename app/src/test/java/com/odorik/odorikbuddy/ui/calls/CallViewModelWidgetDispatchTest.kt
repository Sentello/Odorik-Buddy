package com.odorik.odorikbuddy.ui.calls

import android.content.Context
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.domain.usecase.OneShotCallCoordinatorUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class CallViewModelWidgetDispatchTest {

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

    private fun preferences(values: Map<String, String?>): AppPreferences = mock {
        on { getString(any(), anyOrNull()) } doAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            if (values.containsKey(key)) values[key] else invocation.getArgument<String?>(1)
        }
    }

    private fun callbackTile(
        callerId: String?,
        lineId: String?,
        recipient: String = "+420777123456"
    ) = TileEntity(
        id = 1,
        position = 0,
        label = "Test",
        recipient = recipient,
        callType = "CALLBACK",
        lineId = lineId,
        callerId = callerId
    )

    private fun viewModel(
        tileRepository: TileRepository,
        callUseCase: CallUseCase,
        appPreferences: AppPreferences
    ): CallViewModel {
        val getLines: GetLinesUseCase = mock {
            onBlocking { execute() } doReturn Result.success(emptyList())
        }
        return CallViewModel(
            contactNameResolver = contactNameResolver,
            getPhoneNumbersForContactUseCase = mock<GetPhoneNumbersForContactUseCase>(),
            getLinesUseCase = getLines,
            callUseCase = callUseCase,
            oneShotCallCoordinatorUseCase = mock<OneShotCallCoordinatorUseCase>(),
            getSharedPublicNumbersUseCase = mock<GetSharedPublicNumbersUseCase>(),
            tileRepository = tileRepository,
            localeManager = localeManager,
            context = context,
            appPreferences = appPreferences
        )
    }

    @Test
    fun `tile caller id and line win over the global selection`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(1) } doReturn callbackTile(callerId = "+420111", lineId = "7")
        }
        val calls: CallUseCase = mock {
            onBlocking { execute(any(), any(), any()) } doReturn Result.success("ok")
        }
        val vm = viewModel(
            tiles,
            calls,
            preferences(mapOf("caller_id" to "+420999", "selected_line" to "3"))
        )

        vm.dispatchWidgetTileAction(1)
        advanceUntilIdle()

        verifyBlocking(calls) { execute("+420111", "+420777123456", "7") }
    }

    @Test
    fun `blank tile fields fall back to the global caller id and line`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(1) } doReturn callbackTile(callerId = "", lineId = null)
        }
        val calls: CallUseCase = mock {
            onBlocking { execute(any(), any(), any()) } doReturn Result.success("ok")
        }
        val vm = viewModel(
            tiles,
            calls,
            preferences(mapOf("caller_id" to "+420999", "selected_line" to "3"))
        )

        vm.dispatchWidgetTileAction(1)
        advanceUntilIdle()

        verifyBlocking(calls) { execute("+420999", "+420777123456", "3") }
    }

    @Test
    fun `no caller id anywhere fails before reaching the call use case`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(1) } doReturn callbackTile(callerId = null, lineId = null)
        }
        val calls: CallUseCase = mock()
        val vm = viewModel(tiles, calls, preferences(mapOf("caller_id" to "")))

        vm.dispatchWidgetTileAction(1)
        advanceUntilIdle()

        assertNotNull("a missing caller ID must surface an error", vm.callbackError.value)
        verifyBlocking(calls, never()) { execute(any(), any(), any()) }
    }

    @Test
    fun `a missing tile reports an error and places no call`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(99) } doReturn null
        }
        val calls: CallUseCase = mock()
        val vm = viewModel(tiles, calls, preferences(emptyMap()))

        vm.dispatchWidgetTileAction(99)
        advanceUntilIdle()

        assertNotNull(vm.callbackError.value)
        verifyBlocking(calls, never()) { execute(any(), any(), any()) }
    }

    @Test
    fun `dispatching the same tile twice places only one call`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(1) } doReturn callbackTile(callerId = "+420111", lineId = "7")
        }
        val calls: CallUseCase = mock {
            onBlocking { execute(any(), any(), any()) } doReturn Result.success("ok")
        }
        val vm = viewModel(tiles, calls, preferences(emptyMap()))

        vm.dispatchWidgetTileAction(1)
        vm.dispatchWidgetTileAction(1)
        advanceUntilIdle()

        verifyBlocking(calls, times(1)) { execute(any(), any(), any()) }
    }

    @Test
    fun `a successful callback leaves no error behind`() = runTest(dispatcher) {
        val tiles: TileRepository = mock {
            onBlocking { getTileById(1) } doReturn callbackTile(callerId = "+420111", lineId = "7")
        }
        val calls: CallUseCase = mock {
            onBlocking { execute(any(), any(), any()) } doReturn Result.success("ok")
        }
        val vm = viewModel(tiles, calls, preferences(emptyMap()))

        vm.dispatchWidgetTileAction(1)
        advanceUntilIdle()

        assertNull(vm.callbackError.value)
        verify(localeManager, never()).createLocaleContext(eq(context))
    }
}
