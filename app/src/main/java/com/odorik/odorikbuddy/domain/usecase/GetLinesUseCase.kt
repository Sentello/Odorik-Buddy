package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.AccountRepository
import javax.inject.Inject

class GetLinesUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend fun execute(): Result<List<Line>> = accountRepository.getLines()
}
