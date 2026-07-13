package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.model.UserInfo
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<UserInfo> {
        // Stub for compilation; implement actual API call when endpoint is available
        return Result.success(UserInfo("Stub Name", "stub@example.com", "123456789"))
    }
}