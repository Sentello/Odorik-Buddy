package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class RefreshBalanceAction : ActionCallback {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RefreshBalanceEntryPoint {
        fun balanceWidgetRefresher(): BalanceWidgetRefresher
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RefreshBalanceEntryPoint::class.java
        ).balanceWidgetRefresher().refresh(glanceId, showProgress = true)
    }
}
