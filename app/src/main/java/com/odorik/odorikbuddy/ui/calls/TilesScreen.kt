package com.odorik.odorikbuddy.ui.calls

// Imports for Contact Picker and other functionalities
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.ui.components.AppFab
import com.odorik.odorikbuddy.ui.components.FabEdgePadding
import com.odorik.odorikbuddy.ui.components.FabListBottomSpacing
import com.odorik.odorikbuddy.ui.components.FabSpacing
import com.odorik.odorikbuddy.ui.theme.ExpandedDimens
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.MediumDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents

@Composable
fun TilesScreen(
    viewModel: TilesViewModel = hiltViewModel(),
    callViewModel: CallViewModel, // Injected from CallScreen
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tiles by viewModel.tiles.collectAsState()
    val lines by callViewModel.lines.collectAsState()
    val contactsMap by viewModel.contactsMap.collectAsState()
    
    // Call Status States from CallViewModel
    val callResult by callViewModel.callResult.collectAsState()
    val callbackError by callViewModel.callbackError.collectAsState()
    val linesError by callViewModel.linesError.collectAsState()
    val oneShotCallResult by callViewModel.oneShotCallResult.collectAsState()
    val oneShotCallError by callViewModel.oneShotCallError.collectAsState()
    val isOneShotCallLoading by callViewModel.isOneShotCallLoading.collectAsState()
    val isCallbackLoading by callViewModel.isCallbackLoading.collectAsState()
    
    // rememberSaveable (and the tile's id, not the tile): an open add/edit dialog
    // survives a rotation instead of closing and losing what was typed.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var tileToEditId by rememberSaveable { mutableStateOf<Int?>(null) }
    val tileToEdit = tiles.find { it.id == tileToEditId }
    var tileToDelete by remember { mutableStateOf<TileEntity?>(null) }
    var isEditMode by rememberSaveable { mutableStateOf(false) }

    // The tile whose call is being placed; it shows a spinner while the request runs.
    var callingTileId by remember { mutableStateOf<Int?>(null) }
    
    // Explicitly destructure state values to use in Composable context safely
    val currentCallResult = callResult
    val currentCallbackError = callbackError
    val currentLinesError = linesError
    val currentOneShotCallResult = oneShotCallResult
    val currentOneShotCallError = oneShotCallError

    // Animation state
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
                    run {
                        val dimens = LocalAppDimens.current
                        val tileSize = viewModel.tileSize
                        val columnCount = TileGridLayout.columns(
                            standardColumns = when (dimens) {
                                ExpandedDimens -> 4
                                MediumDimens -> 3
                                else -> 2
                            },
                            size = tileSize
                        )
                        // Feedback messages. Pinned above the grid (not inside it), so the result of a
                        // call is visible even when the tapped tile is far down the list.
                        val activeError = when {
                            // Action errors win over the background lines-fetch error.
                            !currentOneShotCallError.isNullOrEmpty() -> currentOneShotCallError
                            !currentCallbackError.isNullOrEmpty() -> currentCallbackError
                            else -> currentLinesError
                        }
                        val hasMessage = currentCallResult.isNotEmpty() ||
                            currentOneShotCallResult.isNotEmpty() ||
                            !activeError.isNullOrEmpty()

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (hasMessage) {
                                Column(
                                    modifier = Modifier.padding(
                                        start = dimens.screenPaddingHorizontal,
                                        end = dimens.screenPaddingHorizontal,
                                        top = dimens.screenPaddingVertical
                                    )
                                ) {
                                    if (currentCallResult.isNotEmpty()) {
                                        CallApiMessage(response = currentCallResult, visible = true)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                     // OneShot Call response messages
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
                                                    .padding(vertical = 8.dp),
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

                                    if (!activeError.isNullOrEmpty()) {
                                         ErrorMessage(errorText = activeError, visible = true)
                                         Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columnCount),
                                contentPadding = dimens.screenPadding,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                items(tiles, key = { it.id }) { tile ->
                                    TileItem(
                                        tile = tile,
                                        // Keyed on contactsMap: when the contacts finish loading,
                                        // the names update in place and the grid keeps its scroll
                                        // position (it used to be rebuilt from scratch).
                                        contactName = remember(contactsMap, tile.recipient) {
                                            viewModel.getContactName(tile.recipient)
                                        },
                                        isEditMode = isEditMode,
                                        size = tileSize,
                                        singleColumn = columnCount == 1,
                                        isCalling = callingTileId == tile.id &&
                                            (isCallbackLoading || isOneShotCallLoading),
                                        onClick = {
                                            if (isEditMode) {
                                                // While editing, a tap edits the tile. It must never
                                                // place a (paid) call by accident.
                                                tileToEditId = tile.id
                                            } else if (tile.callType == TileCallType.CALLBACK) {
                                                callingTileId = tile.id
                                                callViewModel.makeCall(
                                                    callerId = tile.callerId ?: "", // Pass empty if not set
                                                    recipient = tile.recipient,
                                                    line = tile.lineId ?: ""
                                                )
                                            } else {
                                                if (!isOneShotCallLoading) {
                                                    callingTileId = tile.id
                                                    callViewModel.makeOneShotCall(
                                                        targetRecipient = tile.recipient,
                                                        useLineAsCallerId = tile.useLineAsCallerId,
                                                        selectedLineId = tile.lineId?.toIntOrNull()
                                                    )
                                                }
                                            }
                                        },
                                        onEdit = { tileToEditId = tile.id },
                                        onDelete = { tileToDelete = tile },
                                        onMoveUp = { viewModel.moveTileUp(tile) },
                                        onMoveDown = { viewModel.moveTileDown(tile) }
                                    )
                                }
                            
                                // Spacer for FAB
                                item {
                                    Spacer(modifier = Modifier.height(FabListBottomSpacing))
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(FabEdgePadding),
                    horizontalArrangement = Arrangement.spacedBy(FabSpacing)
                ) {
                    AppFab(
                        icon = if (isEditMode) Icons.Default.CheckCircle else Icons.Default.Edit,
                        contentDescription = stringResource(
                            if (isEditMode) R.string.done else R.string.edit_tiles
                        ),
                        onClick = { isEditMode = !isEditMode },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    AppFab(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_tile),
                        onClick = { showAddDialog = true },
                        containerColor = ScreenAccents.Calls.main(),
                        contentColor = Color.White
                    )
                }
            }
        }
        
        // Auto-dismiss Call result after delay
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
            title = { Text(stringResource(R.string.delete_tile_title)) },
            text = { Text(stringResource(R.string.delete_tile_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        tileToDelete?.let { viewModel.deleteTile(it) }
                        tileToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { tileToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
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
                tileToEditId = null
            },
            onSave = { tileData ->
                val editId = tileToEditId
                if (editId != null) {
                    viewModel.updateTile(editId, tileData)
                } else {
                    viewModel.addTile(tileData)
                }
                showAddDialog = false
                tileToEditId = null
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
            text = stringResource(R.string.no_tiles_yet), // Needs to be added to strings.xml or use generic text
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
            colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.Calls.main())
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_tile))
        }
    }
}
