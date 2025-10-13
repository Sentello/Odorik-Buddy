package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetCreditUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<Double> {

        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        var responseString = ""
        return try {
            responseString = odorikApi.getCredit(userId, password)

            if (responseString.contains("error authentication_failed")) {
                return Result.failure(AuthenticationException("Authentication failed"))
            }

            val creditValue = responseString.toDouble()
            Result.success(creditValue)
        } catch (e: NumberFormatException) {
            Result.failure(Exception("Invalid balance format: $responseString", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AuthenticationException(message: String) : Exception(message)
