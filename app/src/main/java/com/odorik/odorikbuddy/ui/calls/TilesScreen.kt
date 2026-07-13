package com.odorik.odorikbuddy.ui.calls


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.ui.components.darkModeBorder
import com.odorik.odorikbuddy.ui.theme.CallAccent
import com.odorik.odorikbuddy.util.getResponsivePadding

@Composable
fun TilesScreen(
    viewModel: TilesViewModel = hiltViewModel(),
    callViewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tiles by viewModel.tiles.collectAsState()
    val lines by callViewModel.lines.collectAsState()
    val contactsMap by viewModel.contactsMap.collectAsState()


    val callResult by callViewModel.callResult.collectAsState()
    val error by callViewModel.error.collectAsState()
    val oneShotCallResult by callViewModel.oneShotCallResult.collectAsState()
    val oneShotCallError by callViewModel.oneShotCallError.collectAsState()
    val isOneShotCallLoading by callViewModel.isOneShotCallLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var tileToEdit by remember { mutableStateOf<TileEntity?>(null) }
    var tileToDelete by remember { mutableStateOf<TileEntity?>(null) }


    val currentCallResult = callResult
    val currentError = error
    val currentOneShotCallResult = oneShotCallResult
    val currentOneShotCallError = oneShotCallError


    var contentVisible by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.loadContacts(context.contentResolver)
            }
        }
    )



    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadContacts(context.contentResolver)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        callViewModel.getLines()
        contentVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400)
                    )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (tiles.isEmpty()) {
                    EmptyTilesState(onAddClick = { showAddDialog = true })
                } else {
                    androidx.compose.runtime.key(contactsMap) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = getResponsivePadding(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {

                            item(span = { GridItemSpan(2) }) {
                                Column {
                                    if (currentCallResult.isNotEmpty()) {
                                        CallApiMessage(response = currentCallResult, visible = true)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }


                                    if (currentOneShotCallResult.isNotEmpty()) {
                                         AnimatedVisibility(
                                            visible = true,
                                            enter = slideInVertically(
                                                initialOffsetY = { -it },
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            ) + fadeIn(animationSpec = tween(300))
                                        ) {
                                            ElevatedCard(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                                    .darkModeBorder(RoundedCornerShape(16.dp)),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.elevatedCardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                ),
                                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = stringResource(R.string.call_successfully_enqueued),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        textAlign = TextAlign.Center,
                                                        fontWeight = FontWeight.Medium,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }


                                    val activeError = if (!currentOneShotCallError.isNullOrEmpty()) {
                                        currentOneShotCallError
                                    } else {
                                        currentError
                                    }

                                    if (!activeError.isNullOrEmpty()) {
                                         ErrorMessage(errorText = activeError, visible = true)
                                         Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                            items(tiles, key = { it.id }) { tile ->
                                TileItem(
                                    tile = tile,
                                    contactName = viewModel.getContactName(tile.recipient),
                                    onClick = {

                                        if (tile.callType == "CALLBACK") {
                                            callViewModel.makeCall(
                                                callerId = tile.callerId ?: "",
                                                recipient = tile.recipient,
                                                line = tile.lineId ?: ""
                                            )
                                        } else {
                                            if (!isOneShotCallLoading) {
                                                callViewModel.makeOneShotCall(
                                                    targetRecipient = tile.recipient,
                                                    useLineAsCallerId = tile.useLineAsCallerId
                                                )
                                                if (tile.lineId != null && tile.lineId.isNotEmpty()) {
                                                    tile.lineId.toIntOrNull()?.let { callViewModel.updateSelectedLine(it) }
                                                }
                                            }
                                        }
                                    },
                                    onEdit = { tileToEdit = tile },
                                    onDelete = { tileToDelete = tile },
                                    onMoveUp = { viewModel.moveTileUp(tile) },
                                    onMoveDown = { viewModel.moveTileDown(tile) }
                                )
                            }


                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = CallAccent,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_tile))
                }
            }
        }


        LaunchedEffect(currentCallResult) {
            if (currentCallResult.isNotEmpty() && !currentCallResult.startsWith("error")) {
                kotlinx.coroutines.delay(5000L)
                callViewModel.resetCallResult()
            }
        }
    }

    if (tileToDelete != null) {
        AlertDialog(
            onDismissRequest = { tileToDelete = null },
            title = { Text(stringResource(R.string.delete_rule)) },
            text = { Text(stringResource(R.string.delete_tile_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        tileToDelete?.let { viewModel.deleteTile(it) }
                        tileToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CallAccent)
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { tileToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = CallAccent)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAddDialog || tileToEdit != null) {
        AddEditTileDialog(
            tile = tileToEdit,
            lines = lines,
            callViewModel = callViewModel,
            onDismiss = {
                showAddDialog = false
                tileToEdit = null
            },
            onSave = { tileData ->
                if (tileToEdit != null) {
                    viewModel.updateTile(
                        tileId = tileToEdit!!.id,
                        label = tileData.label,
                        recipient = tileData.recipient,
                        callType = tileData.callType,
                        lineId = tileData.lineId,
                        callerId = tileData.callerId,
                        useLineAsCallerId = tileData.useLineAsCallerId,
                        color = tileData.color,
                        textColor = tileData.textColor
                    )
                } else {
                    viewModel.addTile(
                        label = tileData.label,
                        recipient = tileData.recipient,
                        callType = tileData.callType,
                        lineId = tileData.lineId,
                        callerId = tileData.callerId,
                        useLineAsCallerId = tileData.useLineAsCallerId,
                        color = tileData.color,
                        textColor = tileData.textColor
                    )
                }
                showAddDialog = false
                tileToEdit = null
            }
        )
    }
}

@Composable
fun EmptyTilesState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_tiles_yet),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.add_tiles_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = CallAccent)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_tile))
        }
    }
}

data class TileData(
    val label: String,
    val recipient: String,
    val callType: String,
    val lineId: String?,
    val callerId: String?,
    val useLineAsCallerId: Boolean,
    val color: Long?,
    val textColor: Long?
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileItem(
    tile: TileEntity,
    contactName: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val containerColor = TileColorHelper.resolveColor(tile.color, isSystemDark) ?: MaterialTheme.colorScheme.surface

    val isCustomColor = tile.color != null


    val titleColor = if (tile.textColor != null) {
        Color(tile.textColor)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val bodyColor = if (tile.textColor != null) {
        Color(tile.textColor).copy(alpha = 0.7f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val primaryColor = if (tile.textColor != null) {
        Color(tile.textColor).copy(alpha = 0.9f)
    } else if (isCustomColor) {
         if (isSystemDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
    } else {
         MaterialTheme.colorScheme.primary
    }

    val iconBgColor = if (tile.textColor != null) {
         Color(tile.textColor).copy(alpha = 0.1f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    }

    val iconTint = if (tile.textColor != null) {
        Color(tile.textColor)
    } else if (isCustomColor) {
         if (isSystemDark) Color.White else Color.Black.copy(alpha = 0.7f)
    } else {
         MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .darkModeBorder(RoundedCornerShape(20.dp))
            .height(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEdit
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                 Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tile.callType == "CALLBACK") Icons.Default.Call else Icons.AutoMirrored.Filled.PhoneForwarded,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconTint
                    )
                }


                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_tile),
                            modifier = Modifier.size(18.dp),
                            tint = bodyColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = if (isCustomColor && isSystemDark) 0.9f else 0.75f)
                        )
                    }
                }
            }


            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val callTypeText = if (tile.callType == "CALLBACK") stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot)

                if (tile.label.isNotEmpty()) {
                    Text(
                        text = tile.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = callTypeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = callTypeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor
                    )
                    if (contactName != tile.recipient) {
                        Text(
                            text = tile.recipient,
                            style = MaterialTheme.typography.bodySmall,
                            color = bodyColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.move_up),
                        modifier = Modifier.size(18.dp),
                        tint = bodyColor
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.move_down),
                        modifier = Modifier.size(18.dp),
                        tint = bodyColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTileDialog(
    tile: TileEntity?,
    lines: List<Line>,
    onDismiss: () -> Unit,
    onSave: (TileData) -> Unit,
    callViewModel: CallViewModel
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf(tile?.label ?: "") }
    var recipient by remember { mutableStateOf(tile?.recipient ?: "") }

    val tabOrder by callViewModel.tabOrder.collectAsState()


    val callTypeOptions = remember(tabOrder) {
        val callbackIndex = tabOrder.indexOf("callback_title").takeIf { it >= 0 } ?: Int.MAX_VALUE
        val oneshotIndex = tabOrder.indexOf("oneshot_call").takeIf { it >= 0 } ?: Int.MAX_VALUE

        if (oneshotIndex < callbackIndex) {
            listOf("ONESHOT" to R.string.call_type_oneshot, "CALLBACK" to R.string.call_type_callback)
        } else {
            listOf("CALLBACK" to R.string.call_type_callback, "ONESHOT" to R.string.call_type_oneshot)
        }
    }

    var callType by remember { mutableStateOf(tile?.callType ?: callTypeOptions.first().first) }
    var selectedLineId by remember { mutableStateOf(tile?.lineId) }
    var callerId by remember { mutableStateOf(tile?.callerId ?: "") }
    var useLineAsCallerId by remember { mutableStateOf(tile?.useLineAsCallerId ?: false) }
    var selectedColor by remember { mutableStateOf(tile?.color) }
    var selectedTextColor by remember { mutableStateOf(tile?.textColor) }

    var contactFieldToUpdate by remember { mutableStateOf<ContactField?>(null) }

    var lineDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = callViewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.isNotEmpty()) {
                    if (numbers.size == 1) {
                        val number = numbers.first()
                        if (contactFieldToUpdate == ContactField.RECIPIENT) {
                            recipient = number
                        } else if (contactFieldToUpdate == ContactField.CALLER_ID) {
                            callerId = number
                        }
                    } else {
                        phoneNumbers = numbers
                        showPhoneNumberDialog = true
                    }
                }
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactPickerLauncher.launch(null)
            }
        }
    )

    fun pickContact(field: ContactField) {
        contactFieldToUpdate = field
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            contactPickerLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val colors = TileColorHelper.allBaseColors
    val textColors = TileColorHelper.textColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tile == null) stringResource(R.string.add_tile) else stringResource(R.string.edit_tile)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (callType == "CALLBACK") stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.call_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        callTypeOptions.forEach { (typeKey, labelRes) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(labelRes)) },
                                onClick = { callType = typeKey; typeDropdownExpanded = false }
                            )
                        }
                    }
                }


                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val recipientField = @Composable {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text(stringResource(R.string.called_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = stringResource(R.string.pick_contact),
                                    tint = CallAccent
                                )
                            }
                        }
                    )
                }

                val lineField = @Composable {
                    ExposedDropdownMenuBox(
                        expanded = lineDropdownExpanded,
                        onExpandedChange = { lineDropdownExpanded = !lineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                             value = lines.find { it.id.toString() == selectedLineId }?.let { "${it.name} (${it.callerId})" } ?: stringResource(R.string.select_line),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.line)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = lineDropdownExpanded,
                            onDismissRequest = { lineDropdownExpanded = false }
                        ) {
                            lines.forEach { line ->
                                DropdownMenuItem(
                                    text = { Text("${line.name} (${line.callerId})") },
                                    onClick = { selectedLineId = line.id.toString(); lineDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                if (callType == "CALLBACK") {
                    OutlinedTextField(
                        value = callerId,
                        onValueChange = { callerId = it },
                        label = { Text(stringResource(R.string.caller_id)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            IconButton(onClick = { pickContact(ContactField.CALLER_ID) }) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = stringResource(R.string.pick_contact),
                                    tint = CallAccent
                                )
                            }
                        }
                    )
                    recipientField()
                    lineField()
                } else {
                    recipientField()
                    lineField()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { useLineAsCallerId = !useLineAsCallerId }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useLineAsCallerId,
                            onCheckedChange = { useLineAsCallerId = it }
                        )
                        Text(stringResource(R.string.use_line_number_as_caller_id))
                    }
                }


                Text(stringResource(R.string.color), style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {

                         Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (selectedColor == null) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedColor = null }
                        )
                    }

                    items(colors) { color ->
                        val displayColor = TileColorHelper.resolveColor(color, isSystemInDarkTheme())!!
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(displayColor)
                                .border(2.dp, if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }


                Text(stringResource(R.string.text_color), style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {

                         Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (selectedTextColor == null) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedTextColor = null }
                        )
                    }

                    items(textColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(2.dp, if (selectedTextColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedTextColor = color }
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isLineSelected = selectedLineId != null
                    val isRecipientValid = recipient.isNotBlank()
                    val isCallerIdValid = if (callType == "CALLBACK") callerId.isNotBlank() else true

                    if (isLineSelected && isRecipientValid && isCallerIdValid) {
                         onSave(
                            TileData(
                                label = label,
                                recipient = recipient,
                                callType = callType,
                                lineId = selectedLineId,
                                callerId = callerId.ifEmpty { null },
                                useLineAsCallerId = useLineAsCallerId,
                                color = selectedColor,
                                textColor = selectedTextColor
                            )
                        )
                        validationError = null
                    } else {
                        validationError = context.getString(R.string.error_fill_required_fields)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CallAccent)
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = CallAccent)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showPhoneNumberDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneNumberDialog = false },
            title = { Text(stringResource(R.string.choose_phone_number)) },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(phoneNumbers) { number ->
                        TextButton(
                            onClick = {
                                if (contactFieldToUpdate == ContactField.RECIPIENT) {
                                    recipient = number
                                } else if (contactFieldToUpdate == ContactField.CALLER_ID) {
                                    callerId = number
                                }
                                showPhoneNumberDialog = false
                                contactFieldToUpdate = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = CallAccent)
                        ) {
                            Text(number, textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPhoneNumberDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = CallAccent)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}