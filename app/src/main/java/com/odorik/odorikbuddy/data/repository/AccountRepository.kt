package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

class AuthenticationException(message: String) : Exception(message)


@Singleton
class AccountRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {


    suspend fun getCredit(): Result<Double> {
        var responseString = ""
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getCredit(userId, password)
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error: ${response.code()}"))
            }
            responseString = response.body() ?: ""

            if (responseString.startsWith("error")) {
                return if (responseString.contains("authentication_failed")) {
                    Result.failure(AuthenticationException("Authentication failed"))
                } else {
                    Result.failure(Exception(responseString))
                }
            }

            Result.success(responseString.toDouble())
        } catch (e: CancellationException) {
            throw e
        } catch (e: NumberFormatException) {
            Result.failure(Exception("Invalid balance format: $responseString", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLines(): Result<List<Line>> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getLines(userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
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
