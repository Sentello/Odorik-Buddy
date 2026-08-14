package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response

class AccountRepositoryTest {

    private val userRepository: UserRepository = mock {
        on { requireCredentials() } doReturn ("user" to "pass")
    }

    private fun repoReturning(body: String): AccountRepository {
        val api: OdorikApi = mock {
            onBlocking { getCredit("user", "pass") } doReturn Response.success(body)
        }
        return AccountRepository(api, userRepository)
    }

    @Test
    fun `numeric body is parsed as balance`() = runBlocking {
        val result = repoReturning("123.45").getCredit()
        assertEquals(123.45, result.getOrNull()!!, 0.0)
    }

    @Test
    fun `authentication error body maps to AuthenticationException`() = runBlocking {
        val result = repoReturning("error authentication_failed").getCredit()
        assertTrue(result.exceptionOrNull() is AuthenticationException)
    }

    @Test
    fun `other error body is generic failure with original message`() = runBlocking {
        val result = repoReturning("error temporarily_unavailable").getCredit()
        assertTrue(result.isFailure)
        assertEquals("error temporarily_unavailable", result.exceptionOrNull()?.message)
    }

    @Test
    fun `non-numeric body is invalid balance failure`() = runBlocking {
        val result = repoReturning("maintenance").getCredit()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.startsWith("Invalid balance format"))
    }

    @Test
    fun `http error is failure`() = runBlocking {
        val api: OdorikApi = mock {
            onBlocking { getCredit("user", "pass") } doReturn
                Response.error(500, okhttp3.ResponseBody.create(null, "server error"))
        }
        val result = AccountRepository(api, userRepository).getCredit()
        assertTrue(result.isFailure)
        assertEquals("HTTP error: 500", result.exceptionOrNull()?.message)
    }
}
