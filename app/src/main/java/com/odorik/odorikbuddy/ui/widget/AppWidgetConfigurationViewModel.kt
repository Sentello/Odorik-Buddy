package com.odorik.odorikbuddy.ui.widget

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.ui.calls.TileManagementDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppWidgetConfigurationViewModel @Inject constructor(
    private val tileManagementDelegate: TileManagementDelegate
) : ViewModel() {

    val tiles: StateFlow<List<TileEntity>> = tileManagementDelegate.getTiles(viewModelScope)
    val contactsMap: StateFlow<Map<String, String>> = tileManagementDelegate.contactsMap

    fun loadContacts(contentResolver: ContentResolver) {
        tileManagementDelegate.loadContacts(viewModelScope, contentResolver)
    }

    fun getContactName(number: String): String {
        return tileManagementDelegate.getContactName(number)
    }
}
