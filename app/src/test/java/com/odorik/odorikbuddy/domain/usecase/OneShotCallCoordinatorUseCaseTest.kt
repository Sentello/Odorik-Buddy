package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.model.SharedPublicNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class OneShotCallCoordinatorUseCaseTest {

    private val sharedNumber = SharedPublicNumber(publicNumber = "910100100", type = "shared")

    private fun prefsWithPhone(phone: String): AppPreferences = mock {
        on { getString("phone_number", "") } doReturn phone
    }

    private val sharedNumbersOk: GetSharedPublicNumbersUseCase = mock {
        onBlocking { execute() } doReturn Result.success(listOf(sharedNumber))
    }

    private val linesUnused: GetLinesUseCase = mock()

    private fun createRouteOk(): CreateRouteUseCase = mock {
        onBlocking {
            execute(any(), any(), any(), any(), any())
        } doReturn Result.success("ok")
        onBlocking {
            executeWithLineCredentials(any(), any(), any(), any(), any(), any(), any())
        } doReturn Result.success("ok")
    }

    @Test
    fun `missing source number fails with NoSourceNumberException`() = runBlocking {
        val useCase = OneShotCallCoordinatorUseCase(
            prefsWithPhone(""), sharedNumbersOk, linesUnused, createRouteOk()
        )
        val result = useCase.execute("777123456", useLineAsCallerId = false, selectedLineId = null)
        assertTrue(result.exceptionOrNull() is NoSourceNumberException)
    }

    @Test
    fun `no shared numbers fails with NoSharedNumbersException`() = runBlocking {
        val emptyShared: GetSharedPublicNumbersUseCase = mock {
            onBlocking { execute() } doReturn Result.success(emptyList())
        }
        val useCase = OneShotCallCoordinatorUseCase(
            prefsWithPhone("00420777123456"), emptyShared, linesUnused, createRouteOk()
        )
        val result = useCase.execute("777123456", useLineAsCallerId = false, selectedLineId = null)
        assertTrue(result.exceptionOrNull() is NoSharedNumbersException)
    }

    @Test
    fun `happy path without line returns shared number and creates route`() = runBlocking {
        val createRoute = createRouteOk()
        val useCase = OneShotCallCoordinatorUseCase(
            prefsWithPhone("00420777123456"), sharedNumbersOk, linesUnused, createRoute
        )
        val result = useCase.execute("777123456", useLineAsCallerId = false, selectedLineId = null)
        assertEquals("910100100", result.getOrNull())
        verifyBlocking(createRoute) { execute(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `selected line uses line credentials route`() = runBlocking {
        val line = Line(id = 101, sipPassword = "sip-secret")
        val lines: GetLinesUseCase = mock {
            onBlocking { execute() } doReturn Result.success(listOf(line))
        }
        val createRoute = createRouteOk()
        val useCase = OneShotCallCoordinatorUseCase(
            prefsWithPhone("00420777123456"), sharedNumbersOk, lines, createRoute
        )
        val result = useCase.execute("777123456", useLineAsCallerId = true, selectedLineId = 101)
        assertEquals("910100100", result.getOrNull())
        verifyBlocking(createRoute) {
            executeWithLineCredentials(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `route failure propagates as failure`() = runBlocking {
        val createRoute: CreateRouteUseCase = mock {
            onBlocking { execute(any(), any(), any(), any(), any()) } doReturn
                Result.failure(Exception("error routing_failed"))
        }
        val useCase = OneShotCallCoordinatorUseCase(
            prefsWithPhone("00420777123456"), sharedNumbersOk, linesUnused, createRoute
        )
        val result = useCase.execute("777123456", useLineAsCallerId = false, selectedLineId = null)
        assertTrue(result.isFailure)
    }
}
