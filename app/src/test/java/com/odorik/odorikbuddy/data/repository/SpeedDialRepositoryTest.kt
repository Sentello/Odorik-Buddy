package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikApiException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response

class SpeedDialRepositoryTest {

    private val userRepository: UserRepository = mock {
        on { requireCredentials() } doReturn ("user" to "pass")
    }

    private fun repo(
        list: Response<String> = Response.success("[]"),
        write: Response<String> = Response.success("{}")
    ): SpeedDialRepository {
        val api: OdorikApi = mock {
            onBlocking { getSpeedDials(any(), any()) } doReturn list
            onBlocking { createSpeedDial(any(), any(), any(), any(), anyOrNull()) } doReturn write
            onBlocking { updateSpeedDial(any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn write
            onBlocking { deleteSpeedDial(any(), any(), any()) } doReturn write
        }
        return SpeedDialRepository(api, userRepository)
    }

    @Test
    fun `list is parsed, sorted and numbers are kept as is`() = runBlocking {
        val body = """[{"shortcut":9,"number":"*100","name":"Info"},{"shortcut":1,"number":"00420603123456","name":"Jana"}]"""
        val items = repo(list = Response.success(body)).getSpeedDials().getOrThrow()
        assertEquals(listOf(1, 9), items.map { it.shortcut })
        assertEquals("*100", items[1].number)
    }

    @Test
    fun `writes succeed on empty object`() = runBlocking {
        assertTrue(repo().create("123", "A", null).isSuccess)
        assertTrue(repo().update(1, null, "123", null).isSuccess)
        assertTrue(repo().delete(1).isSuccess)
    }

    @Test
    fun `errors array becomes OdorikApiException`() = runBlocking {
        val r = repo(write = Response.success("""{"errors":["shortcut_already_used"]}""")).create("1", "A", 1)
        assertEquals("shortcut_already_used", (r.exceptionOrNull() as OdorikApiException).code)
    }

    @Test
    fun `errors array on a non 200 status is still read`() = runBlocking {
        val bad = Response.error<String>(422, """{"errors":["invalid_number"]}""".toResponseBody(null))
        val r = repo(write = bad).create("x", "A", 1)
        assertEquals("invalid_number", (r.exceptionOrNull() as OdorikApiException).code)
    }

    /** Real line response (2026-11-22) plus a made-up line-own entry, which only the website can create. */
    @Test
    fun `line speed dials carry owner and overrides`() = runBlocking {
        val body = """[{"shortcut":1,"number":"123456","name":"test","owner":"account","overrides":null},
            {"shortcut":5,"number":"777","name":"own","owner":"line","overrides":"54545654"}]"""
        val api: OdorikApi = mock {
            onBlocking { getLineSpeedDials(any(), any(), any()) } doReturn Response.success(body)
        }
        val items = SpeedDialRepository(api, userRepository).getLineSpeedDials(760490).getOrThrow()
        assertEquals(false, items[0].isLineOwn)
        assertEquals(true, items[1].isLineOwn)
        assertEquals("54545654", items[1].overrides)
    }

    @Test
    fun `free shortcuts start at 1 and skip used ones`() {
        assertEquals(listOf(4, 6, 7, 8), freeShortcuts(listOf(1, 2, 3, 5, 9)))
        assertEquals(listOf(1, 2, 3, 4), freeShortcuts(emptyList()))
    }
}
