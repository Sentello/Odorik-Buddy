package com.odorik.odorikbuddy.ui.routes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.darkModeBorder
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import com.odorik.odorikbuddy.ui.theme.SettingsAccentLight
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    internalNavController: NavHostController,
    viewModel: RoutesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val routesMap by viewModel.routesMap.collectAsState()
    val error by viewModel.error.collectAsState()

    // UI-only state
    var selectedPublicNumber by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbersForSelection by remember { mutableStateOf(emptyList<String>()) }
    var fieldBeingPicked by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // --- START: PERMISSION LOGIC FOR DISPLAYING CONTACT NAMES ---

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, load the contacts
                viewModel.loadContacts(context.contentResolver)
            }
        }
    )

    // Check for permission when the screen is first composed
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                // Permission is already granted, load contacts
                viewModel.loadContacts(context.contentResolver)
            }
            else -> {
                // Permission is not granted, request it
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
    // --- END: PERMISSION LOGIC ---


    // --- This section for the contact PICKER remains the same ---
    var launcherToTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = viewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    when (fieldBeingPicked) {
                        "source" -> viewModel.onSourceNumberChange(numbers.first())
                        "ringing" -> viewModel.onRingingNumberChange(numbers.first())
                    }
                } else if (numbers.size > 1) {
                    phoneNumbersForSelection = numbers
                    showPhoneNumberDialog = true
                }
            }
        }
    )

    val requestPermissionLauncherForPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                launcherToTrigger?.invoke()
                launcherToTrigger = null
            }
        }
    )

    fun pickContactFor(field: String) {
        fieldBeingPicked = field
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            launcherToTrigger = { contactPickerLauncher.launch(null) }
            // Use the specific launcher for the picker action
            requestPermissionLauncherForPicker.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val pullRefreshState = rememberPullRefreshState(isLoading && uiState !is RoutesViewModel.UiState.Loading, {
        viewModel.loadData(isRefresh = true, contentResolver = context.contentResolver)
    })

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            GradientHeader(
                title = stringResource(R.string.shared_numbers),
                iconVector = Icons.Default.Contacts,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(SettingsAccent.copy(alpha = 0.35f), Color.Transparent)
                ),
                iconGradientBrush = Brush.linearGradient(
                    colors = listOf(SettingsAccent, SettingsAccentLight)
                ),
                onBackClick = { internalNavController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when (val currentState = uiState) {
                is RoutesViewModel.UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SettingsAccent)
                    }
                }
                is RoutesViewModel.UiState.Error -> {
                    RoutesErrorState(
                        title = stringResource(R.string.error_loading_shared_numbers),
                        error = currentState.message,
                        onRetry = { viewModel.loadData(isRefresh = true, contentResolver = context.contentResolver) }
                    )
                }
                is RoutesViewModel.UiState.Success -> {
                    // Hoist responsive calculations out of the LazyColumn for performance
                    val baseSpacing = getResponsiveSpacing()
                    val cardPadding = getResponsiveCardPadding()
                    val bodyLargeSize = getResponsiveBodyLargeSize()
                    val bodySubtitleSize = bodyLargeSize * 0.85f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = baseSpacing)
                    ) {
                        items(
                            items = currentState.data,
                            key = { it.publicNumber }
                        ) { number ->
                            val routesForThisNumber = routesMap[number.publicNumber].orEmpty()
                            val hasRules = routesForThisNumber.isNotEmpty()
                            
                            // --- NEW: Get contact name for the public number ---
                            val publicNumberDisplayName = viewModel.getContactName(number.publicNumber)

                            SharedNumberItem(
                                publicNumberDisplayName = publicNumberDisplayName,
                                number = number,
                                hasRules = hasRules,
                                routesForThisNumber = routesForThisNumber,
                                selectedPublicNumber = selectedPublicNumber,
                                onSelect = {
                                    selectedPublicNumber = if (selectedPublicNumber == number.publicNumber) null else number.publicNumber
                                },
                                isLoading = isLoading,
                                viewModel = viewModel,
                                cardPadding = cardPadding,
                                baseSpacing = baseSpacing,
                                bodyLargeSize = bodyLargeSize,
                                bodySubtitleSize = bodySubtitleSize,
                                onAddRule = {
                                    viewModel.resetDialogState()
                                    showAddDialog = true
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(baseSpacing))
                        }
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = isLoading && uiState !is RoutesViewModel.UiState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = SettingsAccent
            )
        }
    }

    // ... (dialogs remain unchanged) ...

    LaunchedEffect(error) {
        // Only show snackbar if NOT in full-screen error state
        if (uiState !is RoutesViewModel.UiState.Error && error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    // The rest of your dialogs remain unchanged
    if (showAddDialog && selectedPublicNumber != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            title = { Text(stringResource(R.string.add_rule)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing()/2)) {
                    val dialogSourceNumber by viewModel.dialogSourceNumber.collectAsState()
                    val dialogRingingNumber by viewModel.dialogRingingNumber.collectAsState()
                    val dialogReplaceBySource by viewModel.dialogReplaceBySource.collectAsState()
                    val dialogUseCallerIdPrefix by viewModel.dialogUseCallerIdPrefix.collectAsState()


                    OutlinedTextField(
                        value = dialogSourceNumber,
                        onValueChange = viewModel::onSourceNumberChange,
                        label = {
                            Text(
                                stringResource(R.string.source_number),
                                fontSize = getResponsiveBodyLargeSize()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { pickContactFor("source") }) {
                                Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_contact))
                            }
                        }
                    )
                    OutlinedTextField(
                        value = dialogRingingNumber,
                        onValueChange = viewModel::onRingingNumberChange,
                        label = {
                            Text(
                                stringResource(R.string.ringing_number),
                                fontSize = getResponsiveBodyLargeSize()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { pickContactFor("ringing") }) {
                                Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_contact))
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.onReplaceBySourceChange(!dialogReplaceBySource) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dialogReplaceBySource,
                            onCheckedChange = { viewModel.onReplaceBySourceChange(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SettingsAccent
                            )
                        )
                        Text(
                            stringResource(R.string.replace_by_source_number),
                            fontSize = getResponsiveBodyLargeSize()
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.onUseCallerIdPrefixChange(!dialogUseCallerIdPrefix) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dialogUseCallerIdPrefix,
                            onCheckedChange = { viewModel.onUseCallerIdPrefixChange(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SettingsAccent
                            )
                        )
                        Text(
                            stringResource(R.string.use_line_number_as_caller_id),
                            fontSize = getResponsiveBodyLargeSize()
                        )
                    }


                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedPublicNumber != null,
                    onClick = {
                        selectedPublicNumber?.let { publicNumber ->
                            viewModel.createRoute(publicNumber)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPhoneNumberDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneNumberDialog = false },
            title = { Text(stringResource(R.string.choose_phone_number)) },
            text = {
                LazyColumn {
                    items(phoneNumbersForSelection) { number ->
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                when (fieldBeingPicked) {
                                    "source" -> viewModel.onSourceNumberChange(number)
                                    "ringing" -> viewModel.onRingingNumberChange(number)
                                }
                                showPhoneNumberDialog = false
                            }
                        ) {
                            Text(number)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhoneNumberDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun RoutesErrorState(
    title: String,
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier.darkModeBorder(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SettingsAccent)
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
fun SharedNumberItem(
    publicNumberDisplayName: String,
    number: com.odorik.odorikbuddy.model.SharedPublicNumber,
    hasRules: Boolean,
    routesForThisNumber: List<com.odorik.odorikbuddy.model.Route>,
    selectedPublicNumber: String?,
    onSelect: () -> Unit,
    isLoading: Boolean,
    viewModel: RoutesViewModel,
    cardPadding: androidx.compose.ui.unit.Dp,
    baseSpacing: androidx.compose.ui.unit.Dp,
    bodyLargeSize: androidx.compose.ui.unit.TextUnit,
    bodySubtitleSize: androidx.compose.ui.unit.TextUnit,
    onAddRule: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardPadding, vertical = baseSpacing / 2)
            .darkModeBorder(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ListItem(
                headlineContent = {
                    Text(
                        text = publicNumberDisplayName,
                        fontSize = bodyLargeSize,
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = if (hasRules) {
                    {
                        Text(
                            text = pluralStringResource(
                                R.plurals.route_rules_count,
                                routesForThisNumber.size,
                                routesForThisNumber.size
                            ),
                            fontSize = bodySubtitleSize,
                            fontWeight = FontWeight.Bold,
                            color = SettingsAccent
                        )
                    }
                } else null,
                leadingContent = if (hasRules) {
                    {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Has rules",
                            tint = SettingsAccent
                        )
                    }
                } else null,
                modifier = Modifier.clickable(onClick = onSelect)
            )

            AnimatedVisibility(visible = selectedPublicNumber == number.publicNumber) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading && selectedPublicNumber == number.publicNumber) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SettingsAccent
                            )
                        } else {
                            // Extracted static loop to prevent nested LazyColumn nested scrolling layout bugs
                            Column(modifier = Modifier.heightIn(max = 300.dp)) {
                                routesForThisNumber.forEach { route ->
                                    val sourceName = viewModel.getContactName(route.sourceNumber)
                                    val ringingName = viewModel.getContactName(route.ringingNumber)

                                    ListItem(
                                        headlineContent = { Text(sourceName) },
                                        supportingContent = { Text("→ $ringingName") },
                                        trailingContent = {
                                            IconButton(onClick = {
                                                viewModel.deleteRoute(
                                                    number.publicNumber,
                                                    route.id
                                                )
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete_rule),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = onAddRule,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = cardPadding,
                                vertical = baseSpacing / 2
                            ),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SettingsAccent
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(baseSpacing / 2))
                        Text(stringResource(R.string.add_rule))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}