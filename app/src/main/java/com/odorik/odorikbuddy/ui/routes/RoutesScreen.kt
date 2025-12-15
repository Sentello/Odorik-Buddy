package com.odorik.odorikbuddy.ui.routes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: RoutesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val routesMap by viewModel.routesMap.collectAsState()
    val error by viewModel.error.collectAsState()

    
    var selectedPublicNumber by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbersForSelection by remember { mutableStateOf(emptyList<String>()) }
    var fieldBeingPicked by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                
                viewModel.loadContacts(context.contentResolver)
            }
        }
    )

    
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                
                viewModel.loadContacts(context.contentResolver)
            }
            else -> {
                
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
    


    
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
            
            requestPermissionLauncherForPicker.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val pullRefreshState = rememberPullRefreshState(isLoading, {
        viewModel.loadData(isRefresh = true, contentResolver = context.contentResolver)
    })

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                when (val currentState = uiState) {
                    is RoutesViewModel.UiState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is RoutesViewModel.UiState.Error -> {
                        item {
                            Text(
                                text = stringResource(id = currentState.messageResId),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is RoutesViewModel.UiState.Success -> {
                        items(currentState.data) { number ->
                            val routesForThisNumber = routesMap[number.publicNumber].orEmpty()
                            val hasRules = routesForThisNumber.isNotEmpty()
                            
                            
                            val publicNumberDisplayName = viewModel.getContactName(number.publicNumber)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .animateContentSize()
                                    .clickable {
                                        selectedPublicNumber = if (selectedPublicNumber == number.publicNumber) null else number.publicNumber
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            
                                            text = publicNumberDisplayName,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        if (hasRules) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Has rules",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = pluralStringResource(R.plurals.route_rules_count, routesForThisNumber.size, routesForThisNumber.size),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = selectedPublicNumber == number.publicNumber) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 48.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isLoading && selectedPublicNumber == number.publicNumber) {
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                                } else {
                                                    val routes = routesMap[number.publicNumber] ?: emptyList()
                                                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                                        items(routes) { route ->
                                                            
                                                            val sourceName = viewModel.getContactName(route.sourceNumber)
                                                            val ringingName = viewModel.getContactName(route.ringingNumber)

                                                            ListItem(
                                                                headlineContent = { Text(sourceName) },
                                                                supportingContent = { Text("→ $ringingName") },
                                                                trailingContent = {
                                                                    IconButton(onClick = {
                                                                        viewModel.deleteRoute(number.publicNumber, route.id)
                                                                    }) {
                                                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_rule))
                                                                    }
                                                                }
                                                            )
                                                            HorizontalDivider()
                                                        }
                                                    }
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.resetDialogState()
                                                    showAddDialog = true
                                                },
                                                enabled = !isLoading,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(stringResource(R.string.add_rule))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            PullRefreshIndicator(isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }

    
    if (showAddDialog && selectedPublicNumber != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            title = { Text(stringResource(R.string.add_rule)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val dialogSourceNumber by viewModel.dialogSourceNumber.collectAsState()
                    val dialogRingingNumber by viewModel.dialogRingingNumber.collectAsState()
                    val dialogReplaceBySource by viewModel.dialogReplaceBySource.collectAsState()
                    val dialogUseCallerIdPrefix by viewModel.dialogUseCallerIdPrefix.collectAsState()


                    OutlinedTextField(
                        value = dialogSourceNumber,
                        onValueChange = viewModel::onSourceNumberChange,
                        label = { Text(stringResource(R.string.source_number)) },
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
                        label = { Text(stringResource(R.string.ringing_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { pickContactFor("ringing") }) {
                                Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_contact))
                            }
                        }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = dialogReplaceBySource, onCheckedChange = viewModel::onReplaceBySourceChange)
                        Text(stringResource(R.string.replace_by_source_number))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = dialogUseCallerIdPrefix, onCheckedChange = viewModel::onUseCallerIdPrefixChange)
                        Text(stringResource(R.string.use_line_number_as_caller_id))
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

    LaunchedEffect(error) {
        error?.let { messageResId ->
            snackbarHostState.showSnackbar(context.getString(messageResId))
            viewModel.clearError()
        }
    }
}