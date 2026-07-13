package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class RefreshBalanceAction : ActionCallback {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RefreshBalanceEntryPoint {
        fun getCreditUseCase(): GetCreditUseCase
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[BalanceWidget.isLoadingKey] = true
            prefs.remove(BalanceWidget.errorKey)
        }
        BalanceWidget().update(context, glanceId)

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            RefreshBalanceEntryPoint::class.java
        )
        val useCase = entryPoint.getCreditUseCase()

        val result = useCase.execute()

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[BalanceWidget.isLoadingKey] = false
            prefs[BalanceWidget.lastUpdatedKey] = System.currentTimeMillis()

            result.onSuccess { balance ->
                prefs[BalanceWidget.balanceKey] = balance
                prefs.remove(BalanceWidget.errorKey)
            }.onFailure { error ->
                prefs[BalanceWidget.errorKey] = error.message ?: context.applicationContext.getString(R.string.error_unknown_generic)
            }
        }
        BalanceWidget().update(context, glanceId)
    }
}
