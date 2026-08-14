package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.AccountRepository
import javax.inject.Inject

class GetCreditUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend fun execute(): Result<Double> = accountRepository.getCredit()
}
