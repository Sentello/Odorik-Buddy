package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response

class CallRepositoryTest {

    private val userRepository: UserRepository = mock {
        on { requireCredentials() } doReturn ("user" to "pass")
    }

    private fun repoReturning(body: String): CallRepository {
        val api: OdorikApi = mock {
            onBlocking { call(any(), any(), any(), any(), any()) } doReturn Response.success(body)
        }
        return CallRepository(api, userRepository)
    }

    @Test
    fun `successful callback returns body`() = runBlocking {
        val result = repoReturning("callback_ordered").callback("111", "222", "1")
        assertEquals("callback_ordered", result.getOrNull())
    }

    @Test
    fun `error body is failure`() = runBlocking {
        val result = repoReturning("error invalid_caller").callback("111", "222", "1")
        assertTrue(result.isFailure)
        assertEquals("error invalid_caller", result.exceptionOrNull()?.message)
    }
}
