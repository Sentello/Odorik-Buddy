package com.odorik.odorikbuddy.data.repository

import com.google.gson.reflect.TypeToken
import com.odorik.odorikbuddy.data.model.ActiveCall
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikJsonParser
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

/** Calls in progress: list and hang up. */
@Singleton
class ActiveCallsRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {

    suspend fun getActiveCalls(): Result<List<ActiveCall>> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getActiveCalls(userId, password)
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error: ${response.code()}"))
            }
            OdorikJsonParser.parse(response.body(), ACTIVE_CALLS_TYPE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hangUp(id: Long): Result<Unit> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.hangUpActiveCall(id, userId, password)
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error: ${response.code()}"))
            }
            OdorikJsonParser.parseAck(response.body())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        val ACTIVE_CALLS_TYPE = object : TypeToken<List<ActiveCall>>() {}.type
    }
}
