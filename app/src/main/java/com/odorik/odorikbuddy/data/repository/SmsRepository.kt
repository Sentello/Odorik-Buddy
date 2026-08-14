package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SmsRepository @Inject constructor(
    private val api: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun getAllowedSenders(): Result<List<String>> {
        return try {
            val credentials = userRepository.requireCredentials()
            val response = api.getAllowedSenders(credentials.first, credentials.second)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.startsWith("error") == true) {
                    Result.failure(Exception(body))
                } else {
                    val allNumbers = body?.split(",") ?: emptyList()
                    val filtered = allNumbers.filter { !it.trim().startsWith("00") }
                    Result.success(filtered)
                }
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendSms(
        recipient: String,
        message: String,
        sender: String?,
        delayed: String?
    ): Result<String> {
        return try {
            val credentials = userRepository.requireCredentials()
            val response = api.sendSms(
                credentials.first,
                credentials.second,
                recipient,
                message,
                sender,
                delayed
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.startsWith("error") == true) {
                    Result.failure(Exception(body))
                } else {
                    Result.success(body ?: "")
                }
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
