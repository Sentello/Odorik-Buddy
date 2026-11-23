package com.odorik.odorikbuddy.ui.speeddials

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.SpeedDial
import com.odorik.odorikbuddy.ui.components.AccentIconChip
import com.odorik.odorikbuddy.ui.components.AppFab
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.SkeletonCard
import com.odorik.odorikbuddy.ui.components.constrainedContentWidth
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import kotlinx.coroutines.launch

/** Sheet target: null = closed, [SheetTarget.New] = add, [SheetTarget.Edit] = edit. */
private sealed interface SheetTarget {
    object New : SheetTarget
    data class Edit(val item: SpeedDial) : SheetTarget
}

@Composable
fun SpeedDialsScreen(
    internalNavController: NavHostController,
    viewModel: SpeedDialsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var sheet by remember { mutableStateOf<SheetTarget?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val accent = ScreenAccents.Settings.main()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SpeedDialsEvent.Saved -> snackbarHostState.showSnackbar(context.getString(R.string.speed_dial_saved))
                SpeedDialsEvent.Deleted -> snackbarHostState.showSnackbar(context.getString(R.string.speed_dial_deleted))
                SpeedDialsEvent.CloseSheet -> sheet = null
            }
        }
    }

    Scaffold(
        topBar = {
            GradientHeader(
                title = stringResource(R.string.speed_dials_title),
                iconVector = Icons.Default.Dialpad,
                accent = ScreenAccents.Settings,
                onBackClick = { internalNavController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.readOnly && !state.isLoading && state.loadError == null) {
                AppFab(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.speed_dial_add),
                    onClick = {
                        viewModel.clearSheetError()
                        sheet = SheetTarget.New
                    },
                    containerColor = accent,
                    contentColor = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> Column(
                    modifier = Modifier.padding(LocalAppDimens.current.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonCard(height = 64.dp)
                    SkeletonCard(height = 240.dp)
                }

                state.loadError != null -> LoadError(state.loadError!!, onRetry = viewModel::load)

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .constrainedContentWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.readOnly) {
                        item {
                            InfoBox(Icons.Default.Lock, stringResource(R.string.speed_dials_read_only), MaterialTheme.colorScheme.error)
                        }
                    }
                    if (state.items.isEmpty()) {
                        item { EmptySpeedDials() }
                    } else {
                        if (state.lineSpeedDials.isNotEmpty()) {
                            item {
                                SectionLabel(
                                    stringResource(
                                        R.string.speed_dials_all_lines,
                                        state.lineSpeedDials.joinToString(", ") { (line, _) -> line.name.ifBlank { line.id.toString() } }
                                    )
                                )
                            }
                        }
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    state.items.forEachIndexed { index, item ->
                                        SpeedDialRow(
                                            item = item,
                                            clickable = !state.readOnly,
                                            onClick = {
                                                viewModel.clearSheetError()
                                                sheet = SheetTarget.Edit(item)
                                            }
                                        )
                                        if (index < state.items.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // A line's own speed dials (set on the Odorik website; the API can't change them).
                    state.lineSpeedDials.forEach { (line, dials) ->
                        val own = dials.filter { it.isLineOwn }
                        if (own.isNotEmpty()) {
                            item {
                                SectionLabel(stringResource(R.string.speed_dials_line_only, line.name.ifBlank { line.id.toString() }))
                            }
                            item {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column {
                                        own.forEachIndexed { index, item ->
                                            SpeedDialRow(
                                                item = item,
                                                clickable = false,
                                                onClick = {},
                                                extra = item.overrides?.let { stringResource(R.string.speed_dial_overrides) }
                                            )
                                            if (index < own.lastIndex) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    color = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Text(
                                    stringResource(R.string.speed_dials_line_only_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    sheet?.let { target ->
        SpeedDialEditSheet(
            original = (target as? SheetTarget.Edit)?.item,
            freeShortcuts = state.freeShortcuts,
            isSaving = state.isSaving,
            error = state.sheetError,
            viewModel = viewModel,
            onDismiss = { sheet = null }
        )
    }
}

@Composable
private fun InfoBox(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Surface(
        color = tint.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp)
    )
}

@Composable
private fun SpeedDialRow(item: SpeedDial, clickable: Boolean, onClick: () -> Unit, extra: String? = null) {
    val main = ScreenAccents.Settings.main()
    val secondary = ScreenAccents.Settings.secondary()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(main, secondary))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                item.shortcut.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name?.takeIf { it.isNotBlank() } ?: item.number,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                // Unparseable targets (*100, skype:…) come back unchanged.
                com.odorik.odorikbuddy.util.PhoneNumberUtils.formatForDisplay(item.number),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            extra?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = ScreenAccents.Settings.main())
            }
        }
        if (clickable) Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun EmptySpeedDials() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccentIconChip(icon = Icons.Default.Dialpad, accent = ScreenAccents.Settings.main(), size = 72.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.speed_dials_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.speed_dials_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        // How speed dials work — shown only here; users who already have some know it.
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.speed_dials_info),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedDialEditSheet(
    original: SpeedDial?,
    freeShortcuts: List<Int>,
    isSaving: Boolean,
    error: String?,
    viewModel: SpeedDialsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var shortcut by remember { mutableStateOf((original?.shortcut ?: freeShortcuts.firstOrNull() ?: 1).toString()) }
    var name by remember { mutableStateOf(original?.name.orEmpty()) }
    var number by remember { mutableStateOf(original?.number.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var numbersToChoose by remember { mutableStateOf<List<String>>(emptyList()) }

    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val numbers = viewModel.phoneNumbersOf(context.contentResolver, uri)
            when {
                numbers.size == 1 -> number = numbers.first().trim()
                numbers.size > 1 -> numbersToChoose = numbers
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) contactPicker.launch(null)
    }
    fun pickContact() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPicker.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(if (original == null) R.string.speed_dial_add_title else R.string.speed_dial_edit_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = shortcut,
                    onValueChange = { v -> shortcut = v.filter(Char::isDigit).take(3) },
                    label = { Text(stringResource(R.string.speed_dial_shortcut)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { v -> name = v.take(150) },
                    label = { Text(stringResource(R.string.speed_dial_name)) },
                    singleLine = true,
                    supportingText = if (name.length > 120) {
                        { Text("${name.length}/150") }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            // Free text on purpose: Odorik accepts *100, skype:… etc. (never filter this field).
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text(stringResource(R.string.speed_dial_number)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    IconButton(onClick = ::pickContact) {
                        Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.speed_dial_pick_contact))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (!error.isNullOrEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (original != null) {
                    TextButton(
                        onClick = { confirmDelete = true },
                        enabled = !isSaving,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.save(original, shortcut.toIntOrNull(), name, number) },
                    enabled = !isSaving && name.isNotBlank() && number.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.Settings.main(), contentColor = Color.White)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (confirmDelete && original != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.speed_dial_delete_title)) },
            text = {
                Text(stringResource(R.string.speed_dial_delete_text, original.shortcut, original.name?.takeIf { it.isNotBlank() } ?: original.number))
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(original)
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (numbersToChoose.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { numbersToChoose = emptyList() },
            title = { Text(stringResource(R.string.choose_phone_number)) },
            text = {
                Column {
                    numbersToChoose.forEach { n ->
                        TextButton(
                            onClick = {
                                number = n.trim()
                                numbersToChoose = emptyList()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(n, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { numbersToChoose = emptyList() }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
