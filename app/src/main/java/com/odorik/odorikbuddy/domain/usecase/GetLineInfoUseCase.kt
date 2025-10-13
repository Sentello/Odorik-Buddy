package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.model.LineInfo
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetLineInfoUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(lineId: String): Result<LineInfo> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.getLineInfo(userId, password, lineId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}