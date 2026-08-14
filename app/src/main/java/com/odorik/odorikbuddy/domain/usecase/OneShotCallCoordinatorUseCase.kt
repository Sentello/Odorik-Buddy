package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.model.Line
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class NoSourceNumberException : Exception("No source number configured")
class NoSharedNumbersException : Exception("No shared numbers available")
class SharedNumberNotFoundException : Exception("No shared numbers found")

class OneShotCallCoordinatorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val createRouteUseCase: CreateRouteUseCase
) {
    suspend fun execute(
        targetRecipient: String,
        useLineAsCallerId: Boolean,
        selectedLineId: Int?
    ): Result<String> {
        return try {
            val currentPhoneNumber = appPreferences.getString("phone_number", "") ?: ""
            if (currentPhoneNumber.isEmpty()) {
                return Result.failure(NoSourceNumberException())
            }

            val publicNumbersResult = getSharedPublicNumbersUseCase.execute()
            if (publicNumbersResult.isFailure) {
                return Result.failure(Exception("Error getting public numbers: ${publicNumbersResult.exceptionOrNull()?.message}"))
            }

            val publicNumbers = publicNumbersResult.getOrNull()
            if (publicNumbers.isNullOrEmpty()) {
                return Result.failure(NoSharedNumbersException())
            }

            val lastSharedNumber = publicNumbers.lastOrNull { it.type == "shared" }?.publicNumber
                ?: return Result.failure(SharedNumberNotFoundException())


            var selectedLineInfo: Line? = null
            if (selectedLineId != null) {
                val linesResult = getLinesUseCase.execute()
                if (linesResult.isSuccess) {
                    selectedLineInfo = linesResult.getOrNull()?.find { it.id == selectedLineId }
                }
            }

            val routeResult = if (selectedLineInfo != null) {
                createRouteUseCase.executeWithLineCredentials(
                    publicNumber = lastSharedNumber,
                    sourceNumber = currentPhoneNumber,
                    ringingNumber = targetRecipient,
                    replaceBySource = true,
                    useCallerIdPrefix = useLineAsCallerId,
                    lineId = selectedLineInfo.id.toString(),
                    sipPassword = selectedLineInfo.sipPassword
                )
            } else {
                createRouteUseCase.execute(
                    publicNumber = lastSharedNumber,
                    sourceNumber = currentPhoneNumber,
                    ringingNumber = targetRecipient,
                    replaceBySource = true,
                    useCallerIdPrefix = useLineAsCallerId
                )
            }

            if (routeResult.isFailure) {
                return Result.failure(Exception("Error creating route: ${routeResult.exceptionOrNull()?.message}"))
            }

            Result.success(lastSharedNumber)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
