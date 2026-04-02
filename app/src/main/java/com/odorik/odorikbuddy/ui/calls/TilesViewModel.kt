package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TilesViewModel @Inject constructor(
    private val tileRepository: TileRepository
) : ViewModel() {

    val tiles: StateFlow<List<TileEntity>> = tileRepository.getAllTiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val contacts = mutableMapOf<String, String>()

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                if (numberIndex >= 0 && nameIndex >= 0) {
                    while (cursor.moveToNext()) {
                        val number = cursor.getString(numberIndex)
                        val name = cursor.getString(nameIndex)
                        if (!number.isNullOrBlank() && !name.isNullOrBlank()) {
                            val normalizedNumber = PhoneNumberUtils.normalizeForStorage(number)
                            if (!contacts.containsKey(normalizedNumber)) {
                                contacts[normalizedNumber] = name
                            }
                        }
                    }
                }
            }
            _contactsMap.value = contacts
        }
    }

    fun getContactName(number: String): String {
        val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)
        for ((contactNumber, contactName) in _contactsMap.value) {
            if (PhoneNumberUtils.areNumbersEqual(parsedInput.normalizedNumber, contactNumber)) {
                return if (parsedInput.specialPrefix.isNotEmpty()) {
                    "${parsedInput.specialPrefix} $contactName".trim()
                } else {
                    contactName
                }
            }
        }
        return number
    }

    fun addTile(
        label: String,
        recipient: String,
        callType: String,
        lineId: String?,
        callerId: String?,
        useLineAsCallerId: Boolean,
        color: Long?,
        textColor: Long?
    ) {
        viewModelScope.launch {
            val currentTiles = tiles.value
            val nextPosition = if (currentTiles.isEmpty()) 0 else currentTiles.maxOf { it.position } + 1

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
        viewModelScope.launch {
            val currentTile = tiles.value.find { it.id == tileId } ?: return@launch
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
        }
    }

    fun deleteTile(tile: TileEntity) {
        viewModelScope.launch {
            tileRepository.deleteTile(tile)
        }
    }

    fun onTileReordered(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val currentList = tiles.value.sortedBy { it.position }.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return

        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)


        val updatedList = currentList.mapIndexed { index, tile ->
            tile.copy(position = index)
        }

        viewModelScope.launch {
            tileRepository.updateTiles(updatedList)
        }
    }

    fun moveTileUp(tile: TileEntity) {
        val currentList = tiles.value.sortedBy { it.position }
        val index = currentList.indexOfFirst { it.id == tile.id }
        if (index > 0) {
            onTileReordered(index, index - 1)
        }
    }

    fun moveTileDown(tile: TileEntity) {
        val currentList = tiles.value.sortedBy { it.position }
        val index = currentList.indexOfFirst { it.id == tile.id }
        if (index >= 0 && index < currentList.size - 1) {
            onTileReordered(index, index + 1)
        }
    }
}
