package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsivePadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import kotlinx.coroutines.delay

private fun mapApiArgumentToStringId(apiArgument: String): Int {
    return when (apiArgument) {
        "caller" -> R.string.argument_caller
        "recipient" -> R.string.argument_recipient
        "line" -> R.string.argument_line
        
        else -> R.string.argument_unknown
    }
}

@Composable
private fun CallApiMessage(response: String) {
    val message = when {
        response == "callback_ordered" -> stringResource(R.string.call_callback_ordered)
        response == "successfully_enqueued" -> stringResource(R.string.call_successfully_enqueued)
        response == "error callback_failed" -> stringResource(R.string.call_error_callback_failed)
        response.startsWith("error missing_argument") -> {
            
            val rawArgsString = response.substringAfter("error missing_argument ").trim()

            
            val rawArgsList = rawArgsString.split(',').map { it.trim() }

            
            val translatedArgs = rawArgsList.map { rawArg ->
                val stringId = mapApiArgumentToStringId(rawArg)
                stringResource(id = stringId)
            }

            
            val finalArgsString = translatedArgs.joinToString(", ")

            
            stringResource(R.string.call_error_missing_argument, finalArgsString)
        }
        response == "error invalid_delay_format" -> stringResource(R.string.call_error_invalid_delay_format)
        response == "error delayed_into_past" -> stringResource(R.string.call_error_delayed_into_past)
        response == "error invalid_line" -> stringResource(R.string.call_error_invalid_line)
        else -> if (response.isNotEmpty()) stringResource(R.string.call_unknown_error) else ""
    }
    if (message.isNotEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (message.startsWith("Error") || message.startsWith("Chyba")) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            }
        ) {
            Text(
                text = message,
                color = if (message.startsWith("Error") || message.startsWith("Chyba")) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


enum class ContactField {
    CALLER_ID, RECIPIENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabOrder by viewModel.tabOrder.collectAsState()
    
    
    val tabItems = remember(tabOrder) {
        tabOrder.map { title ->
            when (title) {
                "callback_title" -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
                "oneshot_call" -> TabItem(
                    titleResId = R.string.oneshot_call,
                    title = "oneshot_call",
                    content = { OneShotCallTab(viewModel) }
                )
                else -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calls)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DraggableTabs(
                tabItems = tabItems,
                selectedTabTitle = selectedTab,
                onTabSelected = { viewModel.updateSelectedTab(it) },
                onTabOrderChanged = { newOrder -> viewModel.updateTabOrder(newOrder) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallbackTab(
    viewModel: CallViewModel = hiltViewModel()
) {
    val callList by viewModel.callList.collectAsState()
    val callResult by viewModel.callResult.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val callerId by viewModel.callerId.collectAsState()
    val recipient by viewModel.recipient.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    var currentContactField by remember { mutableStateOf<ContactField?>(null) }
    var launcherToTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = viewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    val number = numbers.first()
                    when (currentContactField) {
                        ContactField.CALLER_ID -> viewModel.updateCallerId(number)
                        ContactField.RECIPIENT -> viewModel.updateRecipient(number)
                        null -> {}
                    }
                } else if (numbers.size > 1) {
                    phoneNumbers = numbers
                    showPhoneNumberDialog = true
                }
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                
                launcherToTrigger?.invoke()
                launcherToTrigger = null 
            } else {
                
            }
        }
    )

    
    fun pickContact(field: ContactField) {
        currentContactField = field 
        
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            contactPickerLauncher.launch(null)
        } else {
            launcherToTrigger = { contactPickerLauncher.launch(null) }
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    LaunchedEffect(Unit) {
        callViewModel.getLines()
    }

    
    val previousResult = remember { mutableStateOf("") }
    val hasLaunchedDialer = remember { mutableStateOf(false) }
    
    LaunchedEffect(oneShotCallResult) {
        if (oneShotCallResult.isNotEmpty() && previousResult.value != oneShotCallResult) {
            previousResult.value = oneShotCallResult
            hasLaunchedDialer.value = false  
        }
    }
    
    LaunchedEffect(oneShotCallResult, hasLaunchedDialer.value) {
        if (oneShotCallResult.isNotEmpty() && !hasLaunchedDialer.value) {
            try {
                
                val hasCallPermission = ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
                
                val intent = if (directCallsEnabled && hasCallPermission) {
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:$oneShotCallResult"))
                } else {
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$oneShotCallResult"))
                }
                
                context.startActivity(intent)
                hasLaunchedDialer.value = true
                
                callViewModel.resetOneShotCallResult()
            } catch (e: Exception) {
                
                hasLaunchedDialer.value = true  
                callViewModel.resetOneShotCallResult()  
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(getResponsivePadding())
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(getResponsiveCardPadding()),
                verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing())
            ) {

                
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { callViewModel.updateRecipient(it) },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.padding(start = 4.dp))
                            Text(
                                stringResource(R.string.called_number),
                                fontSize = getResponsiveBodyLargeSize()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                            Icon(
                                Icons.Default.Contacts, 
                                contentDescription = stringResource(R.string.pick_recipient)
                            )
                        }
                    }
                )
                
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = lines.find { it.id == selectedLine }?.let { "${it.name} (${it.caller_id})" } ?: stringResource(R.string.select_line),
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                Text(stringResource(R.string.line))
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        lines.forEach { line ->
                            DropdownMenuItem(
                                text = {
                                    Text("${line.name} (${line.caller_id})")
                                },
                                onClick = {
                                    callViewModel.updateSelectedLine(line.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = useCallerIdPrefix,
                        onCheckedChange = { callViewModel.updateUseCallerIdPrefix(it) }
                    )
                    Text(
                        text = stringResource(R.string.use_line_number_as_caller_id),
                        fontSize = getResponsiveBodyLargeSize(),
                        modifier = Modifier.clickable { callViewModel.updateUseCallerIdPrefix(!useCallerIdPrefix) }
                    )
                }
                
                
                FilledTonalButton(
                    onClick = {
                        
                        hasLaunchedDialer.value = false
                        
                        callViewModel.makeOneShotCall(recipient, useCallerIdPrefix)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = !isOneShotCallLoading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (isOneShotCallLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = stringResource(R.string.call),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                
                if (oneShotCallResult.isNotEmpty() && !isOneShotCallLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = stringResource(R.string.call_successfully_enqueued),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (oneShotCallError?.isNotEmpty() == true && !isOneShotCallLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = oneShotCallError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    
    if (showPhoneNumberDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneNumberDialog = false },
            title = {
                Text(
                    stringResource(
                        when (currentContactField) {
                            ContactField.CALLER_ID -> R.string.pick_caller_id_number
                            ContactField.RECIPIENT -> R.string.pick_recipient_number
                            else -> R.string.choose_phone_number
                        }
                    )
                )
            },
            text = {
                LazyColumn {
                    items(phoneNumbers) { number ->
                        TextButton(
                            onClick = {
                                when (currentContactField) {
                                    ContactField.CALLER_ID -> callViewModel.updateCallerId(number)
                                    ContactField.RECIPIENT -> callViewModel.updateRecipient(number)
                                    else -> {}
                                }
                                showPhoneNumberDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(number, textAlign = TextAlign.Start)
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