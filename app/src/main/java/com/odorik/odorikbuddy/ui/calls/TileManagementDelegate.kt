package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.ui.widget.WidgetUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class TileManagementDelegate @Inject constructor(
    private val tileRepository: TileRepository,
    private val contactNameResolver: ContactNameResolver,
    private val widgetUpdateManager: WidgetUpdateManager
) {
    val contactsMap: StateFlow<Map<String, String>> = contactNameResolver.contactsMap

    fun getTiles(scope: CoroutineScope): StateFlow<List<TileEntity>> = tileRepository.getAllTiles()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadContacts(scope: CoroutineScope, contentResolver: ContentResolver) {
        scope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }

    fun getContactName(number: String): String {
        return contactNameResolver.getContactName(number)
    }

    fun addTile(
        scope: CoroutineScope,
        tiles: List<TileEntity>,
        label: String,
        recipient: String,
        callType: String,
        lineId: String?,
        callerId: String?,
        useLineAsCallerId: Boolean,
        color: Long?,
        textColor: Long?
    ) {
        scope.launch {
            val nextPosition = if (tiles.isEmpty()) 0 else tiles.maxOf { it.position } + 1
            
            val newTile = TileEntity(
                position = nextPosition,
                label = label,
                recipient = recipient,
                callType = callType,
                lineId = lineId,
                callerId = callerId,
                useLineAsCallerId = useLineAsCallerId,
                color = color,
                textColor = textColor,
                widgetStyle = "SQUARE"
            )
            tileRepository.insertTile(newTile)
        }
    }

    fun updateTile(
        scope: CoroutineScope,
        tiles: List<TileEntity>,
        tileId: Int,
        label: String,
        recipient: String,
        callType: String,
        lineId: String?,
        callerId: String?,
        useLineAsCallerId: Boolean,
        color: Long?,
        textColor: Long?
    ) {
        scope.launch {
            val currentTile = tiles.find { it.id == tileId } ?: return@launch
            val updatedTile = currentTile.copy(
                label = label,
                recipient = recipient,
                callType = callType,
                lineId = lineId,
                callerId = callerId,
                useLineAsCallerId = useLineAsCallerId,
                color = color,
                textColor = textColor
            )
            tileRepository.updateTile(updatedTile)

            // Refresh any home screen widgets using this tile (Medium priority improvement)
            widgetUpdateManager.refreshWidgetsUsingTile(tileId)
        }
    }

    fun deleteTile(scope: CoroutineScope, tile: TileEntity) {
        scope.launch {
            tileRepository.deleteTile(tile)
            // Refresh widgets that were using this tile so they can show "Tile not found" state
            widgetUpdateManager.refreshWidgetsUsingTile(tile.id)
        }
    }

    fun onTileReordered(scope: CoroutineScope, tiles: List<TileEntity>, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        
        val currentList = tiles.sortedBy { it.position }.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        
        val updatedList = currentList.mapIndexed { index, tile ->
            tile.copy(position = index)
        }
        
        scope.launch {
            tileRepository.updateTiles(updatedList)
        }
    }

    fun moveTileUp(scope: CoroutineScope, tiles: List<TileEntity>, tile: TileEntity) {
        val currentList = tiles.sortedBy { it.position }
        val index = currentList.indexOfFirst { it.id == tile.id }
        if (index > 0) {
            onTileReordered(scope, tiles, index, index - 1)
        }
    }

    fun moveTileDown(scope: CoroutineScope, tiles: List<TileEntity>, tile: TileEntity) {
        val currentList = tiles.sortedBy { it.position }
        val index = currentList.indexOfFirst { it.id == tile.id }
        if (index >= 0 && index < currentList.size - 1) {
            onTileReordered(scope, tiles, index, index + 1)
        }
    }
}
