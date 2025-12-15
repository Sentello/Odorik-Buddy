package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.model.PublicNumber
import com.odorik.odorikbuddy.model.SharedPublicNumber
import javax.inject.Inject

class GetSharedPublicNumbersUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<List<SharedPublicNumber>> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.getPublicNumbers(userId, password)
            if (response.isSuccessful) {
                val allPublicNumbers: List<PublicNumber>? = response.body()
                val sharedNumbers = allPublicNumbers?.filter { it.type == "shared" }
                    ?.map { SharedPublicNumber(it.publicNumber, it.type) } ?: emptyList()
                Result.success(sharedNumbers)
            } else {
                // Basic error handling; enhance with specific API error parsing if needed
                val errorMessage = response.errorBody()?.string() ?: "HTTP error: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}