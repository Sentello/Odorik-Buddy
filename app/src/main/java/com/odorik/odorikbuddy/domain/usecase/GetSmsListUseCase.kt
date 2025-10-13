package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.model.SmsInfo
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class GetSmsListUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
}