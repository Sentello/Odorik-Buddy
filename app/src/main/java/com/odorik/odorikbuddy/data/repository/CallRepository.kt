package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikResponseParser
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CallRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {


    suspend fun callback(callerId: String, recipient: String, line: String): Result<String> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.call(
                user = userId,
                password = password,
                caller = callerId,
                recipient = recipient,
                line = line
            )
            if (response.isSuccessful) {
                OdorikResponseParser.parsePlainTextBody(response.body())
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
