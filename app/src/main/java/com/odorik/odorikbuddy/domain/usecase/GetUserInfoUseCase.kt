package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.model.UserInfo
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
}