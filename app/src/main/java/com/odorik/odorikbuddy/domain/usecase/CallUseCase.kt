package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class CallUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(callerId: String, recipient: String, line: String): Result<String> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.call(
                user = userId,
                password = password,
                caller = callerId,
                recipient = recipient,
                line = line
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}