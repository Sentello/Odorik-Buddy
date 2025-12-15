package com.odorik.odorikbuddy.domain.usecase

import android.util.Log 
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetCreditUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<Double> {
        Log.d("GetCreditUseCase", "execute() called")

        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        Log.d("GetCreditUseCase", "User ID: $userId, Password: ${password?.take(3)}...") 

        if (userId == null || password == null) {
            Log.e("GetCreditUseCase", "User not logged in or credentials missing.")
            return Result.failure(Exception("User not logged in"))
        }

        var responseString = ""
        return try {
            responseString = odorikApi.getCredit(userId, password)
            Log.d("GetCreditUseCase", "Raw API response: \"$responseString\"")

            if (responseString.contains("error authentication_failed")) {
                return Result.failure(AuthenticationException("Authentication failed"))
            }

            val creditValue = responseString.toDouble()
            Log.d("GetCreditUseCase", "Parsed credit value: $creditValue")
            Result.success(creditValue)
        } catch (e: NumberFormatException) {
            Log.e("GetCreditUseCase", "NumberFormatException: Invalid balance format: \"$responseString\"", e)
            Result.failure(Exception("Invalid balance format: $responseString", e))
        } catch (e: Exception) {
            Log.e("GetCreditUseCase", "Exception during API call or processing: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class AuthenticationException(message: String) : Exception(message)
