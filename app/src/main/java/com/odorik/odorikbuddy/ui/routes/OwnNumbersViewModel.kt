package com.odorik.odorikbuddy.ui.routes

import android.content.Context
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.DeleteRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPublicNumbersUseCase
import com.odorik.odorikbuddy.domain.usecase.GetRoutesForNumberUseCase
import com.odorik.odorikbuddy.model.PublicNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class OwnNumbersViewModel @Inject constructor(
    publicNumbersDelegate: PublicNumbersDelegate,
    private val getPublicNumbersUseCase: GetPublicNumbersUseCase,
    getRoutesForNumberUseCase: GetRoutesForNumberUseCase,
    createRouteUseCase: CreateRouteUseCase,
    deleteRouteUseCase: DeleteRouteUseCase,
    localeManager: LocaleManager,
    @ApplicationContext context: Context
) : BaseNumbersViewModel<PublicNumber>(
    publicNumbersDelegate,
    getRoutesForNumberUseCase,
    createRouteUseCase,
    deleteRouteUseCase,
    localeManager,
    context
) {

    override suspend fun fetchNumbers(): Result<List<PublicNumber>> =
        getPublicNumbersUseCase.execute()

    override fun publicNumberOf(item: PublicNumber): String = item.publicNumber

    init {
        loadData()
    }
}
