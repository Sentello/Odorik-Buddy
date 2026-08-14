package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.CallRepository
import javax.inject.Inject

class CallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend fun execute(callerId: String, recipient: String, line: String): Result<String> =
        callRepository.callback(callerId, recipient, line)
}
