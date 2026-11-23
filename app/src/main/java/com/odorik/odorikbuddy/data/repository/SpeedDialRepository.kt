package com.odorik.odorikbuddy.data.repository

import com.google.gson.reflect.TypeToken
import com.odorik.odorikbuddy.data.model.SpeedDial
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikJsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Account-wide speed dials. Writes need the account API password (else `unauthorized`). */
@Singleton
class SpeedDialRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {

    suspend fun getSpeedDials(): Result<List<SpeedDial>> = call(
        request = { user, pass -> odorikApi.getSpeedDials(user, pass) },
        parse = { body ->
            OdorikJsonParser.parse<List<SpeedDial>>(body, SPEED_DIALS_TYPE).map { list -> list.sortedBy { it.shortcut } }
        }
    )

    /** What [line] dials: account speed dials plus the line's own (owner = "line"). */
    suspend fun getLineSpeedDials(line: Int): Result<List<SpeedDial>> = call(
        request = { user, pass -> odorikApi.getLineSpeedDials(line, user, pass) },
        parse = { body ->
            OdorikJsonParser.parse<List<SpeedDial>>(body, SPEED_DIALS_TYPE).map { list -> list.sortedBy { it.shortcut } }
        }
    )

    suspend fun create(number: String, name: String, shortcut: Int?): Result<Unit> = call(
        request = { user, pass -> odorikApi.createSpeedDial(user, pass, number, name, shortcut) },
        parse = OdorikJsonParser::parseAck
    )

    /** Sends only the changed fields; `null` = unchanged. */
    suspend fun update(original: Int, newShortcut: Int?, number: String?, name: String?): Result<Unit> = call(
        request = { user, pass -> odorikApi.updateSpeedDial(original, user, pass, newShortcut, number, name) },
        parse = OdorikJsonParser::parseAck
    )

    suspend fun delete(shortcut: Int): Result<Unit> = call(
        request = { user, pass -> odorikApi.deleteSpeedDial(shortcut, user, pass) },
        parse = OdorikJsonParser::parseAck
    )

    private suspend fun <T> call(
        request: suspend (String, String) -> Response<String>,
        parse: (String?) -> Result<T>
    ): Result<T> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = request(userId, password)
            if (!response.isSuccessful) {
                // Odorik may send the {"errors":[…]} body with a non-200 status too.
                val code = OdorikJsonParser.errorCodeOf(response.errorBody()?.string())
                return Result.failure(
                    code?.let { com.odorik.odorikbuddy.util.OdorikApiException(it) }
                        ?: Exception("HTTP error: ${response.code()}")
                )
            }
            parse(response.body())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        val SPEED_DIALS_TYPE = object : TypeToken<List<SpeedDial>>() {}.type
    }
}

/** Lowest free shortcuts starting at 1. */
fun freeShortcuts(used: Collection<Int>, count: Int = 4): List<Int> =
    generateSequence(1) { it + 1 }.filter { it !in used }.take(count).toList()
