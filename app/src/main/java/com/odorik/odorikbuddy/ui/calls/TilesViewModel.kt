package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TilesViewModel @Inject constructor(
    private val tileManagementDelegate: TileManagementDelegate,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val tiles: StateFlow<List<TileEntity>> = tileManagementDelegate.getTiles(viewModelScope)
    val contactsMap: StateFlow<Map<String, String>> = tileManagementDelegate.contactsMap

    val directCallsEnabled: Boolean
        get() = appPreferences.directCallsEnabled

    /** Settings > Tile size. Read on each composition, so a change shows on return. */
    val tileSize: TileSize
        get() = TileSize.fromKey(appPreferences.getString(AppPreferences.KEY_TILE_SIZE))

    fun loadContacts(contentResolver: ContentResolver) {
        tileManagementDelegate.loadContacts(viewModelScope, contentResolver)
    }

    fun getContactName(number: String): String {
        return tileManagementDelegate.getContactName(number)
    }

    fun addTile(data: TileData) {
        tileManagementDelegate.addTile(viewModelScope, tiles.value, data)
    }

    fun updateTile(tileId: Int, data: TileData) {
        tileManagementDelegate.updateTile(viewModelScope, tiles.value, tileId, data)
    }

    fun deleteTile(tile: TileEntity) {
        tileManagementDelegate.deleteTile(viewModelScope, tile)
    }

    fun onTileReordered(fromIndex: Int, toIndex: Int) {
        tileManagementDelegate.onTileReordered(viewModelScope, tiles.value, fromIndex, toIndex)
    }

    fun moveTileUp(tile: TileEntity) {
        tileManagementDelegate.moveTileUp(viewModelScope, tiles.value, tile)
    }

    fun moveTileDown(tile: TileEntity) {
        tileManagementDelegate.moveTileDown(viewModelScope, tiles.value, tile)
    }
}
