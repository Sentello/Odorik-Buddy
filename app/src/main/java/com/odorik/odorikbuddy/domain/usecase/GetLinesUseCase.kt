package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetLinesUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<List<Line>> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.getLines(userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}