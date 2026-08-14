package com.odorik.odorikbuddy.ui.routes

import android.content.Context
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.DeleteRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetRoutesForNumberUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.model.SharedPublicNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    publicNumbersDelegate: PublicNumbersDelegate,
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    getRoutesForNumberUseCase: GetRoutesForNumberUseCase,
    createRouteUseCase: CreateRouteUseCase,
    deleteRouteUseCase: DeleteRouteUseCase,
    localeManager: LocaleManager,
    @ApplicationContext context: Context
) : BaseNumbersViewModel<SharedPublicNumber>(
    publicNumbersDelegate,
    getRoutesForNumberUseCase,
    createRouteUseCase,
    deleteRouteUseCase,
    localeManager,
    context
) {

    private val _dialogReplaceBySource = MutableStateFlow(false)
    val dialogReplaceBySource: StateFlow<Boolean> = _dialogReplaceBySource.asStateFlow()

    fun onReplaceBySourceChange(value: Boolean) { _dialogReplaceBySource.value = value }

    override fun resetDialogState() {
        super.resetDialogState()
        _dialogReplaceBySource.value = false
    }

    override suspend fun fetchNumbers(): Result<List<SharedPublicNumber>> =
        getSharedPublicNumbersUseCase.execute()

    override fun publicNumberOf(item: SharedPublicNumber): String = item.publicNumber

    override fun dialogReplaceBySourceValue(): Boolean = _dialogReplaceBySource.value

    init {
        loadData()
    }
}
