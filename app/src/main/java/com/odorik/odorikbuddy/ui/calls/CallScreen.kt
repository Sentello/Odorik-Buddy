package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R

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
        Text(text = message, color = if (message.startsWith("Error") || message.startsWith("Chyba")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
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
    val callList = viewModel.callList.collectAsState()
    val callResult = viewModel.callResult.collectAsState()
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
                Log.w("CallScreen", "Permission denied for contacts")
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
        viewModel.getCallList()
        viewModel.getLines()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.callback_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = callerId,
                onValueChange = { viewModel.updateCallerId(it) },
                label = { Text(stringResource(R.string.caller_id)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { pickContact(ContactField.CALLER_ID) }) {
                        Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_caller_id))
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = recipient,
                onValueChange = { viewModel.updateRecipient(it) },
                label = { Text(stringResource(R.string.called_number)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                        Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_recipient))
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = lines.find { it.id == selectedLine }?.caller_id ?: stringResource(R.string.select_line),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.line)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    lines.forEach { line ->
                        DropdownMenuItem(text = { Text(line.caller_id) }, onClick = {
                            viewModel.updateSelectedLine(line.id)
                            expanded = false
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    selectedLine?.let { lineId ->
                        viewModel.makeCall(callerId, recipient, lineId.toString())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.call))
            }
            Spacer(modifier = Modifier.height(16.dp))
            CallApiMessage(response = callResult.value)
            error?.let { errorText ->
                Log.e("CallScreen", "Displaying error: $errorText")
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(callList.value) { call ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "${stringResource(R.string.from_label)} ${call.callerId}")
                            Text(text = "${stringResource(R.string.to_label)} ${call.calledNumber}")
                            Text(text = "${stringResource(R.string.duration_label)} ${call.duration}s")
                            Text(text = "${stringResource(R.string.cost_label)} ${call.cost}")
                            Text(text = "${stringResource(R.string.time_label)} ${call.startTime}")
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
                            TextButton(onClick = {
                                when (currentContactField) {
                                    ContactField.CALLER_ID -> viewModel.updateCallerId(number)
                                    ContactField.RECIPIENT -> viewModel.updateRecipient(number)
                                    else -> {}
                                }
                                showPhoneNumberDialog = false
                            }) {
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
}