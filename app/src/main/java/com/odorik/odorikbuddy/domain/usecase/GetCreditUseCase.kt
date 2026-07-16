package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.CredentialsNotSetException
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetCreditUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<Double> {
        var responseString = ""
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getCredit(userId, password)
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error: ${response.code()}"))
            }
            responseString = response.body() ?: ""


            if (responseString.startsWith("error") ||
                responseString.contains("error authentication_failed")
            ) {
                return if (responseString.contains("authentication_failed")) {
                    Result.failure(AuthenticationException("Authentication failed"))
                } else {
                    Result.failure(Exception(responseString))
                }
            }

            val creditValue = responseString.toDouble()
            Result.success(creditValue)
        } catch (e: CredentialsNotSetException) {
            Result.failure(e)
        } catch (e: NumberFormatException) {
            Result.failure(Exception("Invalid balance format: $responseString", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AuthenticationException(message: String) : Exception(message)
